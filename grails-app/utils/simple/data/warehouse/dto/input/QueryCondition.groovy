package simple.data.warehouse.dto.input

import grails.validation.Validateable
import simple.data.warehouse.ApiQueryValidationService
import simple.data.warehouse.GlobalStrings

import java.text.SimpleDateFormat

class QueryCondition extends CustomValidationErrorCodesDto implements Validateable {

    ApiQueryValidationService apiQueryValidationService

    String type
    String attributeName
    String value

    static constraints = {
        type nullable: true, validator: { String value, QueryCondition obj ->
            return obj.apiQueryValidationService.validateConditionTypeName(value, obj)
        }
        attributeName nullable: true, validator: { String value, QueryCondition obj ->
            return obj.apiQueryValidationService.validateAttributeName(value, obj) && obj.apiQueryValidationService.validateConditionCombination(obj)
        }
        value nullable: true, validator: { String value, QueryCondition obj ->
            return obj.apiQueryValidationService.validateConditionValueDataType(value, obj)
        }
    }

    QueryCondition() {}

    QueryCondition(ApiQueryValidationService apiQueryValidationService) {
        this.apiQueryValidationService = apiQueryValidationService
    }

    def conditionValueAsCorrectDataType() {
        if (attributeName == "date") {
            return new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT).parse(value)
        }
        if (["impressions", "clicks"].contains(attributeName)) {
            return Long.valueOf(value)
        }
        if (attributeName == "ctr") {
            return new BigDecimal(value)
        }
        return value
    }
}
