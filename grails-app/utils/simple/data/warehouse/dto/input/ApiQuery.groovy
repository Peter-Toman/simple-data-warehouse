package simple.data.warehouse.dto.input

import grails.validation.Validateable
import simple.data.warehouse.ApiQueryValidationService

class ApiQuery extends CustomValidationErrorCodesDto implements Validateable {

    ApiQueryValidationService apiQueryValidationService

    List<QueryProjection> projections
    List<QueryCondition> conditions
    List<String> groupBy
    List<QueryOrderBy> orderBy
    Long batchSize = 100
    Long offset = 0

    static constraints = {
        projections nullable: true, validator: { List<QueryProjection> projections, ApiQuery obj ->
            return obj.apiQueryValidationService.validateAllProjections(projections, obj)
        }
        conditions nullable: true, validator: { List<QueryCondition> conditions, ApiQuery obj ->
            return obj.apiQueryValidationService.validateAllConditions(conditions, obj)
        }
        groupBy nullable: true, validator: { List<String> groupBy, ApiQuery obj ->
            return obj.apiQueryValidationService.validateAllGroupBy(groupBy, obj)
        }
        orderBy nullable: true, validator: { List<QueryOrderBy> orderBy, ApiQuery obj ->
            return obj.apiQueryValidationService.validateAllOrderBy(orderBy, obj)
        }
        batchSize nullable: true
        offset nullable: true
    }

    boolean validate() {
        if (!apiQueryValidationService) {
            return true
        }
        return validate(null, null, null)
    }

    Set<String> getAllValidationErrorMessages() {
        Set<String> allErrorMessages = []
        allErrorMessages.addAll(this.errorMessages)
        this.projections.each {
            allErrorMessages.addAll(it.errorMessages)
        }
        this.conditions.each {
            allErrorMessages.addAll(it.errorMessages)
        }
        this.orderBy.each {
            allErrorMessages.addAll(it.errorMessages)
        }
        return allErrorMessages
    }

}
