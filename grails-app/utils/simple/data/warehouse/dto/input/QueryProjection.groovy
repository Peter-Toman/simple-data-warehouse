package simple.data.warehouse.dto.input

import grails.validation.Validateable
import simple.data.warehouse.ApiQueryValidationService

class QueryProjection extends CustomValidationErrorCodesDto implements Validateable {

    ApiQueryValidationService apiQueryValidationService

    String type
    String attributeName
    String alias

    static constraints = {
        type nullable: true, validator: { String value, QueryProjection obj ->
            return obj.apiQueryValidationService.validateProjectionTypeName(value, obj)
        }
        attributeName nullable: true, validator: { String value, QueryProjection obj ->
            return obj.apiQueryValidationService.validateAttributeName(value, obj) && obj.apiQueryValidationService.validateProjectionCombination(obj)
        }
        alias nullable: true, validator: { String value, QueryProjection obj ->
            return obj.apiQueryValidationService.validateAliasName(value, obj)
        }
    }

    QueryProjection() {}

    QueryProjection(ApiQueryValidationService apiQueryValidationService) {
        this.apiQueryValidationService = apiQueryValidationService
    }

}
