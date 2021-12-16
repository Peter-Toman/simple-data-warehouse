package simple.data.warehouse

import java.math.RoundingMode

class DailyPerformance {

    String dataSourceName
    String campaignName
    Date date
    Long impressions
    Long clicks
    BigDecimal ctr

    /**
     * Because the are not many unique data source or campaign names when compared
     * to the number of data we will separate these into their own tables where
     * lookup on strings should be much faster given the much lower number of rows
     *
     * */

    DataSource dataSource
    Campaign campaign

    static constraints = {
        ctr scale: 10
    }

    /**
     * Calculate click through rate (ctr) from clicks and impressions before saving
     * */

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
