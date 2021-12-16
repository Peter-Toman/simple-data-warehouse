package simple.data.warehouse

import grails.gorm.PagedResultList
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.type.StandardBasicTypes
import simple.data.warehouse.dto.QueryResult
import simple.data.warehouse.dto.input.ApiQuery
import simple.data.warehouse.enums.ConditionType
import simple.data.warehouse.enums.ProjectionType

class StatisticsService {

    def grailsApplication

    QueryResult getResult(ApiQuery apiQuery) {
        BuildableCriteria dpCriteria = DailyPerformance.createCriteria()

        Long maxBatchSize = grailsApplication.config.get("warehouse.maxBatchSize") as Long
        if (!apiQuery.batchSize || apiQuery.batchSize >= maxBatchSize) {
            apiQuery.batchSize = maxBatchSize
        }

        List<String> missingRequiredGroupBy = provideRequiredMissingGroupBy(apiQuery)

        boolean hasProjections = apiQuery.projections?.size() > 0

        boolean hasAggregateProjections = apiQuery.projections?.findAll { it.type != ProjectionType.ATTRIBUTE.name() }?.size() > 0
        boolean hasAttributeProjections = apiQuery.projections?.findAll { it.type == ProjectionType.ATTRIBUTE.name() }?.size() > 0
        String attributeProjectionsForDistinct = "id"
        if (hasAttributeProjections) {
            attributeProjectionsForDistinct = apiQuery.projections.findAll { it.type == ProjectionType.ATTRIBUTE.name() }.collect {
                camelCaseToSnakeCase(it.attributeName)
            }.join(", ")
        }
        String sqlCountProjection = "sum( count( distinct (${attributeProjectionsForDistinct}) ) ) OVER() as totalRows"

        PagedResultList result = dpCriteria.list (max: apiQuery.batchSize ?: maxBatchSize, offset: apiQuery.offset ?: 0) {
            if (hasProjections) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            }

            apiQuery.conditions.each {
                switch (it.type) {
                    case ConditionType.EQ.name():
                        if (it.attributeName == "dataSourceName") {
                            "in"("dataSource", DataSource.findAllByName(it.value))
                            return
                        }
                        if (it.attributeName == "campaignName") {
                            "in"("campaign", Campaign.findAllByName(it.value))
                            return
                        }
                        eq(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.NE.name():
                        String value = it.value
                        if (it.attributeName == "dataSourceName") {
                            not {
                                "in"("dataSource", DataSource.findAllByName(value))
                            }
                            return
                        }
                        if (it.attributeName == "campaignName") {
                            not {
                                "in"("campaign", Campaign.findAllByName(value))
                            }
                            return
                        }
                        ne(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.LT.name():
                        lt(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.GT.name():
                        gt(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.LE.name():
                        le(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.GE.name():
                        ge(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                    case ConditionType.LIKE.name():
                        if (it.attributeName == "dataSourceName") {
                            "in"("dataSource", DataSource.findAllByNameLike(it.value))
                            return
                        }
                        if (it.attributeName == "campaignName") {
                            "in"("campaignName", Campaign.findAllByNameLike(it.value))
                            return
                        }
                        like(it.attributeName, it.conditionValueAsCorrectDataType())
                        return
                }
            }

            apiQuery.orderBy.each {
                order(it.attributeName, it.direction?.toLowerCase() ?: "asc")
            }

            if ((apiQuery.projections && apiQuery.projections.size() > 0) || apiQuery.groupBy && apiQuery.groupBy.size() > 0) {
                projections {
                    /**
                     *  Workaround for faulty value of total count for gorm's PagedResultList when aggregation functions are used in projections
                     *  Puts totalRows as another column into result rows so we can extract it later
                     * */
                    if ( hasAggregateProjections && hasAttributeProjections ) {
                        sqlProjection sqlCountProjection, ['totalRows'], [StandardBasicTypes.INTEGER]
                    }
                    apiQuery.projections.each {
                        switch (it.type) {
                            case ProjectionType.ATTRIBUTE.name():
                                property(it.attributeName, it.alias ?: it.attributeName)
                                return
                            case ProjectionType.SUM.name():
                                sum(it.attributeName, it.alias ?: it.attributeName)
                                return
                            case ProjectionType.AVG.name():
                                avg(it.attributeName, it.alias ?: it.attributeName)
                                return
                            case ProjectionType.MAX.name():
                                max(it.attributeName, it.alias ?: it.attributeName)
                                return
                            case ProjectionType.MIN.name():
                                min(it.attributeName, it.alias ?: it.attributeName)
                                return
                            case ProjectionType.COUNT.name():
                                count(it.attributeName, it.alias ?: it.attributeName)
                        }

                    }
                    apiQuery.groupBy.each {
                        groupProperty(it)
                    }
                    if(missingRequiredGroupBy.size() > 0) {
                        missingRequiredGroupBy.each {
                            groupProperty(it)
                        }
                    }
                }
            }
            cache(true)

        } as List

        QueryResult queryResult = new QueryResult()
        queryResult.result = result
        queryResult.totalRows = result.getTotalCount()
        /**
         *  Workaround for faulty value of total count for gorm's PagedResultList
         *  Retrieves totalRows from result, then remove this property from all rows
         * */
        if (hasAggregateProjections && hasAttributeProjections && queryResult.result && queryResult.result.size() > 0
            && queryResult.result.first()["totalRows"] != null && (queryResult.result.first()["totalRows"] as Long) < queryResult.totalRows) {
            queryResult.totalRows = queryResult.result.first()["totalRows"] as Long
            queryResult.result.each { Map it ->
                it.remove("totalRows")
            }
        }
        return queryResult
    }

    List<String> provideRequiredMissingGroupBy(ApiQuery apiQuery) {
        List<String> requiredGroupBy = apiQuery.projections.find {
            it.type == ProjectionType.ATTRIBUTE.name()
        }.collect {
            it.attributeName
        }
        if (requiredGroupBy.size() == 0) {
            return []
        }
        return requiredGroupBy - apiQuery.groupBy
    }

    private static String camelCaseToSnakeCase(String camelCaseStr) {
        String ret = camelCaseStr.replaceAll('([A-Z]+)([A-Z][a-z])', '$1_$2').replaceAll('([a-z])([A-Z])', '$1_$2');
        return ret.toLowerCase();
    }

}
