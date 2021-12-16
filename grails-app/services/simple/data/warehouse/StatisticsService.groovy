package simple.data.warehouse

import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.hibernate.criterion.CriteriaSpecification
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

        List result = dpCriteria.list (max: apiQuery.batchSize ?: maxBatchSize, offset: apiQuery.offset ?: 0) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)

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

}
