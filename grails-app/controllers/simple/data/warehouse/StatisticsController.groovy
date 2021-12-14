package simple.data.warehouse

import grails.converters.JSON
import simple.data.warehouse.dto.QueryResult
import simple.data.warehouse.dto.input.ApiQuery

class StatisticsController {

    def statisticsService
    def apiQueryValidationService

    static allowedMethods = ['POST']

    def retrieve(ApiQuery apiQuery) {
        apiQuery.apiQueryValidationService = apiQueryValidationService
        if ( apiQuery.getAllValidationErrorMessages().size() > 0) {
            response.status = 400
            QueryResult queryResult = new QueryResult()
            queryResult.errorMessages = apiQuery.getAllValidationErrorMessages()
            render queryResult as JSON
            return
        }
        render statisticsService.getResult(apiQuery) as JSON
    }

    def listCampaigns() {
        return Campaign.all.collect { it.name } as JSON
    }

    def listDataSources() {
        return DataSource.all.collect { it.name } as JSON
    }
}
