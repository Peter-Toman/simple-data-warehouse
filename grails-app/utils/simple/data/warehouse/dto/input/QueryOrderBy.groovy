package simple.data.warehouse.dto.input

import grails.validation.Validateable
import simple.data.warehouse.ApiQueryValidationService

class QueryOrderBy extends CustomValidationErrorCodesDto implements Validateable {

    ApiQuery apiQuery
    ApiQueryValidationService apiQueryValidationService

    String attributeName
    String direction

    static constraints = {
        attributeName nullable: true, validator: { String value, QueryOrderBy obj ->
            return obj.apiQueryValidationService.validateOrderByName(value, obj)
        }
        direction nullable: true, validator: { String value, QueryOrderBy obj ->
            return obj.apiQueryValidationService.validateSortDirection(value, obj)
        }
    }

    QueryOrderBy() {}

    QueryOrderBy(ApiQueryValidationService apiQueryValidationService) {
        this.apiQueryValidationService = apiQueryValidationService
    }
}
