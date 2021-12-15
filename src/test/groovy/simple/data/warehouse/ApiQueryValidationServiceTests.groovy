package simple.data.warehouse


import grails.testing.services.ServiceUnitTest
import simple.data.warehouse.dto.input.ApiQuery
import simple.data.warehouse.dto.input.QueryCondition
import simple.data.warehouse.dto.input.QueryOrderBy
import simple.data.warehouse.dto.input.QueryProjection
import simple.data.warehouse.enums.ConditionType
import simple.data.warehouse.enums.ProjectionType
import spock.lang.Specification

class ApiQueryValidationServiceTests extends Specification implements ServiceUnitTest<ApiQueryValidationService> {

    void 'testValidateConditionCombinations'() {
        expect:
        service.validateConditionCombination(new QueryCondition(attributeName: "date", type: ConditionType.EQ.name()))
        !service.validateConditionCombination(new QueryCondition(attributeName: "date", type: ConditionType.LIKE.name()))
    }

    void 'testValidateProjectionCombinations'() {
        expect:
        service.validateProjectionCombination(new QueryProjection(attributeName: "date", type: ProjectionType.MAX))
        !service.validateProjectionCombination(new QueryProjection(attributeName: "date", type: ProjectionType.AVG))
    }

    void 'testValidateGroupBy'() {
        expect:
        service.validateAllGroupBy(["date", "impressions"], getApiQuery())
        !service.validateAllGroupBy(["date", "ctr"], getApiQuery())
        !service.validateAllGroupBy(["date", "impressionss"], getApiQuery())

    }

    void 'testValidateProjectionTypeNames'() {
        expect:
        service.validateProjectionTypeName("MAX", new QueryProjection())
        !service.validateProjectionTypeName("NAX", new QueryProjection())
    }

    void 'testValidateConditionTypeNames'() {
        expect:
        service.validateConditionTypeName("EQ", new QueryCondition())
        !service.validateConditionTypeName("MQ", new QueryCondition())
    }

    void 'testValidateAttributeNames'() {
        expect:
        service.validateAttributeName("date", new QueryCondition())
        !service.validateAttributeName("dates", new QueryCondition())
    }

    void 'testValidateOrderByName'() {
        expect:
        service.validateOrderByName("date", new QueryOrderBy(apiQuery: apiQuery))
        !service.validateOrderByName("data", new QueryOrderBy(apiQuery: apiQuery))
    }

    void 'testValidateAliasNames'() {
        expect:
        service.validateAliasName("some_alias", new QueryProjection())
        !service.validateAliasName("some_alias", new QueryProjection())

    }

    private static ApiQuery getApiQuery() {
        return new ApiQuery(projections: [
                new QueryProjection(type: ProjectionType.ATTRIBUTE, attributeName: "date"),
                new QueryProjection(type: ProjectionType.ATTRIBUTE, attributeName: "impressions"),
                new QueryProjection(type: ProjectionType.MAX, attributeName: "clicks")
        ]
        )
    }

}
