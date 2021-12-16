package simple.data.warehouse

import java.text.SimpleDateFormat

/**
 * Component to create data for integration tests
 * */

class TestDataCreator {

    def createTestData() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT)

        DataSource webAds = new DataSource(name: "WebAds")
        DataSource androidAds = new DataSource(name: "AndroidAds")

        DataSource.saveAll(webAds, androidAds)

        Campaign cocaCola = new Campaign(name: "CocaCola")
        Campaign pepsi = new Campaign(name: "Pepsi")
        Campaign redBull = new Campaign(name: "RedBull")

        Campaign.saveAll(cocaCola, pepsi, redBull)

        DailyPerformance.saveAll(
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/01/21"), clicks: 10, impressions: 2500),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/02/21"), clicks: 14, impressions: 2800),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/03/21"), clicks: 4, impressions: 2000),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/04/21"), clicks: 44, impressions: 9000),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/05/21"), clicks: 17, impressions: 4200),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/06/21"), clicks: 55, impressions: 17000),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/07/21"), clicks: 3, impressions: 4000),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/08/21"), clicks: 21, impressions: 15200),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/09/21"), clicks: 66, impressions: 41000),
                new DailyPerformance(dataSource: webAds, campaign: cocaCola, date: simpleDateFormat.parse("12/10/21"), clicks: 16, impressions: 9700),

                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/01/21"), clicks: 65, impressions: 12500),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/02/21"), clicks: 67, impressions: 11400),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/03/21"), clicks: 89, impressions: 18900),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/04/21"), clicks: 3, impressions: 5000),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/05/21"), clicks: 66, impressions: 48000),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/06/21"), clicks: 46, impressions: 67000),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/07/21"), clicks: 19, impressions: 33000),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/08/21"), clicks: 8, impressions: 12300),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/09/21"), clicks: 53, impressions: 189000),
                new DailyPerformance(dataSource: androidAds, campaign: cocaCola, date: simpleDateFormat.parse("12/10/21"), clicks: 98, impressions: 335000),


                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/01/21"), clicks: 6, impressions: 1500),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/02/21"), clicks: 3, impressions: 1800),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/03/21"), clicks: 5, impressions: 1000),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/04/21"), clicks: 13, impressions: 6000),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/05/21"), clicks: 19, impressions: 3200),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/06/21"), clicks: 3, impressions: 12000),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/07/21"), clicks: 7, impressions: 1000),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/08/21"), clicks: 6, impressions: 11100),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/09/21"), clicks: 22, impressions: 17000),
                new DailyPerformance(dataSource: webAds, campaign: pepsi, date: simpleDateFormat.parse("12/10/21"), clicks: 8, impressions: 5500),

                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/01/21"), clicks: 42, impressions: 10500),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/02/21"), clicks: 33, impressions: 10400),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/03/21"), clicks: 45, impressions: 10900),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/04/21"), clicks: 9, impressions: 1000),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/05/21"), clicks: 55, impressions: 32000),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/06/21"), clicks: 34, impressions: 34000),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/07/21"), clicks: 12, impressions: 22000),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/08/21"), clicks: 11, impressions: 6300),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/09/21"), clicks: 66, impressions: 119000),
                new DailyPerformance(dataSource: androidAds, campaign: pepsi, date: simpleDateFormat.parse("12/10/21"), clicks: 79, impressions: 420000),


                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/01/21"), clicks: 612, impressions: 43222),
                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/02/21"), clicks: 321, impressions: 27800),
                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/03/21"), clicks: 533, impressions: 123500),
                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/04/21"), clicks: 130, impressions: 15000),
                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/05/21"), clicks: 328, impressions: 32000),
                new DailyPerformance(dataSource: webAds, campaign: redBull, date: simpleDateFormat.parse("12/06/21"), clicks: 780, impressions: 502000),

                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/01/21"), clicks: 420, impressions: 210500),
                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/02/21"), clicks: 330, impressions: 110400),
                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/03/21"), clicks: 450, impressions: 210900),
                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/04/21"), clicks: 90, impressions: 11000),
                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/05/21"), clicks: 550, impressions: 52000),
                new DailyPerformance(dataSource: androidAds, campaign: redBull, date: simpleDateFormat.parse("12/06/21"), clicks: 340, impressions: 94000),
        )
    }

}
