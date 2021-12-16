package simple.data.warehouse

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import simple.data.warehouse.dto.QueryResult
import simple.data.warehouse.dto.input.ApiQuery
import simple.data.warehouse.dto.input.QueryCondition
import simple.data.warehouse.dto.input.QueryOrderBy
import simple.data.warehouse.dto.input.QueryProjection
import spock.lang.Specification

import java.text.SimpleDateFormat

@Integration
@Rollback
class StatisticsServiceSpec extends Specification {

    StatisticsService statisticsService
    TestDataCreator testDataCreator

    SimpleDateFormat dateFormat = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT)

    void setup() {
        testDataCreator.createTestData()
    }

    def 'testCriteriaQueryForMax'() {
        when:

        ApiQuery apiQuery = new ApiQuery(
                projections: [
                        new QueryProjection(type: "MAX", attributeName: "clicks", alias: "cl"),
                ],
                conditions: [
                        new QueryCondition(type: "EQ", attributeName: "dataSourceName", value: "WebAds"),
                ]
        )

        QueryResult queryResult = statisticsService.getResult(apiQuery)

        then:
        queryResult.result != null
        queryResult.errorMessages == null
        queryResult.result[0]["cl"] == 780

    }

    def 'testCriteriaQueryForGroupBy'() {
        when:

        ApiQuery apiQuery = new ApiQuery(
                projections: [
                        new QueryProjection(type: "MAX", attributeName: "clicks", alias: "cl"),
                        new QueryProjection(type: "ATTRIBUTE", attributeName: "date", alias: "dt"),
                ],
                conditions: [
                        new QueryCondition(type: "EQ", attributeName: "dataSourceName", value: "AndroidAds"),
                ],
                groupBy: [ "date" ],
                orderBy: [
                        new QueryOrderBy(attributeName: "date", direction: "DESC")
                ]
        )

        QueryResult queryResult = statisticsService.getResult(apiQuery)

        then:
        queryResult.result != null
        queryResult.result.size() == 10

    }

    def 'testCriteriaQueryForBatchSize'() {
        when:

        ApiQuery apiQuery = new ApiQuery(
                projections: [
                        new QueryProjection(type: "MAX", attributeName: "clicks", alias: "cl"),
                        new QueryProjection(type: "ATTRIBUTE", attributeName: "date", alias: "dt"),
                ],
                conditions: [
                        new QueryCondition(type: "EQ", attributeName: "dataSourceName", value: "AndroidAds"),
                ],
                groupBy: [ "date" ],
                orderBy: [
                        new QueryOrderBy(attributeName: "date", direction: "DESC")
                ],
                batchSize: 5
        )

        QueryResult queryResult = statisticsService.getResult(apiQuery)

        then:
        queryResult.result != null
        queryResult.result.size() == 5

    }

}
