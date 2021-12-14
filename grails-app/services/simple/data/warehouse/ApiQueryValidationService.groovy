package simple.data.warehouse

import grails.validation.Validateable
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.springframework.context.MessageSource
import simple.data.warehouse.dto.input.*
import simple.data.warehouse.enums.ConditionType
import simple.data.warehouse.enums.ProjectionType

import java.text.SimpleDateFormat

class ApiQueryValidationService {

    MessageSource messageSource

    private static final Map<String, List<String>> forbiddenConditionCombinations = [
            // conditionType : attributeName
            "LIKE": ["clicks", "impressions", "ctr", "date"],
            "LE"  : ["campaignName", "dataSourceName"],
            "GE"  : ["campaignName", "dataSourceName"]
    ]

    private static final Map<String, List<String>> forbiddenProjectionCombinations = [
            // projectionType : attributeName
            "AVG": ["campaignName", "dataSourceName"],
            "SUM": ["campaignName", "dataSourceName", "ctr"],
            "MIN": ["campaignName", "dataSourceName"],
            "MAX": ["campaignName", "dataSourceName"]
    ]

    private static final List<String> directionTypes = ["ASC", "DESC"]

    MappingContext grailsDomainClassMappingContext

    boolean validateAllProjections(List<QueryProjection> projections, ApiQuery obj) {
        projections.each { it.apiQueryValidationService = obj.apiQueryValidationService }
        return validateAll(projections)
    }

    boolean validateAllConditions(List<QueryCondition> conditions, ApiQuery obj) {
        conditions.each { it.apiQueryValidationService = obj.apiQueryValidationService }
        return validateAll(conditions)
    }

    boolean validateConditionCombination(QueryCondition queryCondition) {
        List<String> forbiddenForType = forbiddenConditionCombinations.get(queryCondition.type)
        if (forbiddenForType && forbiddenForType.contains(queryCondition.getAttributeName())) {
            addErrorMessage("api.condition.forbiddenCombination", [queryCondition.type, queryCondition.attributeName], queryCondition.errorMessages)
            return false
        }
        return true
    }

    boolean validateProjectionCombination(QueryProjection queryProjection) {
        List<String> forbiddenForType = forbiddenProjectionCombinations.get(queryProjection.type)
        if (forbiddenForType && forbiddenForType.contains(queryProjection.getAttributeName())) {
            addErrorMessage("api.projection.forbiddenCombination", [queryProjection.type, queryProjection.attributeName], queryProjection.errorMessages)
            return false
        }
        return true
    }

    boolean validateAllGroupBy(List<String> groupBy, ApiQuery obj) {
        List<String> persistentProperties = getAllPersistentPropertyNames(DailyPerformance.class)
        List<String> attributeProjections = obj.projections.findAll {
            it.type.toLowerCase() == ProjectionType.ATTRIBUTE.name().toLowerCase()
        }.collect { it.attributeName }
        boolean isValid = true
        groupBy.each {
            if (!persistentProperties.contains(it)) {
                addErrorMessage("api.groupBy.notInProperties", [it], obj.errorMessages)
                isValid = false
            }
            if (attributeProjections && attributeProjections.size() > 0 && !attributeProjections.contains(it)) {
                addErrorMessage("api.groupBy.notInProjectionsAsAttribute", [it], obj.errorMessages)
                isValid = false
            }
        }
        return isValid
    }

    boolean validateAllOrderBy(List<QueryOrderBy> orderBy, ApiQuery obj) {
        orderBy.each {
            it.apiQueryValidationService = obj.apiQueryValidationService
            it.apiQuery = obj
        }
        return validateAll(orderBy)
    }

    boolean validateProjectionTypeName(String projectionTypeName, QueryProjection obj) {
        try {
            ProjectionType projectionType = ProjectionType.valueOf(projectionTypeName)
        } catch (IllegalArgumentException e) {
            addErrorMessage("api.projectionType.notSupported", [projectionTypeName], obj.errorMessages)
            return false
        }
        return true
    }

    boolean validateConditionTypeName(String conditionTypeName, QueryCondition obj) {
        try {
            ConditionType conditionType = ConditionType.valueOf(conditionTypeName)
        } catch (IllegalArgumentException e) {
            addErrorMessage("api.conditionType.notSupported", [conditionTypeName], obj.errorMessages)
            return false
        }
        return true
    }

    boolean validateAttributeName(String attributeName, CustomValidationErrorCodesDto obj) {
        List<String> persistentProperties = getAllPersistentPropertyNames(DailyPerformance.class)
        if (!persistentProperties.contains(attributeName)) {
            addErrorMessage("api.attributeWithName.notPresent", [attributeName], obj.errorMessages)
            return false
        }
        return true
    }

    boolean validateOrderByName(String orderByName, QueryOrderBy obj) {
        List<String> persistentProperties = getAllPersistentPropertyNames(DailyPerformance.class)
        List<String> usedAliases = obj.apiQuery.projections.findAll { it.alias != null }.collect { it.alias }
        List<String> acceptable = []
        acceptable.addAll(persistentProperties)
        acceptable.addAll(usedAliases)
        if (!acceptable.contains(orderByName)) {
            addErrorMessage("api.orderByMember.notPresent", [orderByName], obj.errorMessages)
            return false
        }
        return true
    }

    boolean validateAliasName(String aliasName, CustomValidationErrorCodesDto obj) {
        boolean isValid = aliasName.matches("[0-9a-zA-Z_\\-]+")
        if (!isValid) {
            addErrorMessage("api.attributeAlias.invalidCharacters", [aliasName], obj.errorMessages)
        }
        return isValid
    }

    boolean validateSortDirection(String direction, CustomValidationErrorCodesDto obj) {
        boolean isValid = directionTypes.contains(direction)
        if (!isValid) {
            addErrorMessage("api.sortDirection.invalid", [direction], obj.errorMessages)
        }
        return isValid
    }

    boolean validateConditionValueDataType(String value, QueryCondition queryCondition) {
        List<PersistentProperty> persistentProperties = getAllPersistentProperties(DailyPerformance.class)
        Class<?> requiredDataType = persistentProperties.find { (it.getName() == queryCondition.attributeName) }.getType()
        try {
            if (requiredDataType == Long.class) {
                Long num = Long.valueOf(value)
            }
            if (requiredDataType == BigDecimal.class) {
                BigDecimal num = new BigDecimal(value)
            }
            if (requiredDataType == Date.class) {
                Date date = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT).parse(value)
            }
            if (requiredDataType == String.class && value != null) {
                return true
            }
        } catch (Exception e) {
            addErrorMessage("api.conditionValue.cannotCast", [value, requiredDataType], queryCondition.errorMessages)
            return false
        }
        return true
    }

    private static boolean validateAll(List<Validateable> validateables) {
        boolean result = true
        validateables.each {
            if (!it.validate()) {
                result = false
                return
            }
        }
        return result
    }

    private List<String> getAllPersistentPropertyNames(Class clazz) {
        return getAllPersistentProperties(clazz).collect {
            it.name
        }
    }

    private List<PersistentProperty> getAllPersistentProperties(Class clazz) {
        PersistentEntity persistentEntity = grailsDomainClassMappingContext.getPersistentEntity(clazz.name)
        return persistentEntity.getPersistentProperties()
    }

    private void addErrorMessage(String code, List args, List<String> errorMessages) {
        String error = messageSource.getMessage(code, args as Object[], new Locale("en", "GB"))
        errorMessages.add(error)
    }
}
