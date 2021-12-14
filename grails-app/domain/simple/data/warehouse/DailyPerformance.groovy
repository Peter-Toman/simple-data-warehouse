package simple.data.warehouse

import java.math.RoundingMode

class DailyPerformance {

    String dataSourceName
    String campaignName
    Date date
    Long impressions
    Long clicks
    BigDecimal ctr

    DataSource dataSource
    Campaign campaign

    static constraints = {
        ctr scale: 10
    }

    def beforeInsert() {
        if (clicks && impressions && impressions != 0) {
            ctr = calculateCtr(clicks, impressions)
        } else {
            ctr = 0
        }
    }

    void setCampaign(Campaign campaign) {
        this.campaign = campaign
        if (campaign) {
            this.campaignName = campaign.name
        }
    }

    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource
        if (dataSource) {
            this.dataSourceName = dataSource.name
        }
    }

    static BigDecimal calculateCtr(Long clicks, Long impressions) {
        BigDecimal.valueOf(clicks).divide(BigDecimal.valueOf(impressions), 10, RoundingMode.HALF_UP)
    }
}
