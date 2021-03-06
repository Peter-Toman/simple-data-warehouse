package simple.data.warehouse

import grails.gorm.transactions.Transactional
import grails.util.Environment
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction

import java.text.DateFormat
import java.text.SimpleDateFormat
import grails.converters.JSON

class BootStrap {

    DateFormat dateFormat

    def grailsApplication
    SessionFactory sessionFactory

    def init = { servletContext ->
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        dateFormat = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT)

        JSON.registerObjectMarshaller(Date) {
            return dateFormat.format(it)
        }

        JSON.registerObjectMarshaller(DailyPerformance) {
            def output = [:]
            output['dataSourceName'] = it.dataSourceName
            output['campaignName'] = it.campaignName
            output['date'] = it.date
            output['impressions'] = it.impressions
            output['clicks'] = it.clicks
            output['ctr'] = it.ctr
            return output
        }

        boolean refreshData = grailsApplication.config.get("warehouse.refreshDataOnStartup") as boolean
        if (Environment.current == Environment.TEST) {
            return
        }
        if (refreshData) {
            doRefreshData()
        }
    }

    def destroy = {
    }

    def doRefreshData() {
        InputStream data = this.class.classLoader.getResourceAsStream('data/data.csv')
        Integer lineNr = 1
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(data))) {
            reader.readLine()   // skip first line with column names
            while (reader.ready()) {
                String line = reader.readLine()
                String[] fields = line.split(GlobalStrings.CSV_COLUMN_DELIMITER)
                createNewRecord(fields, session)
                lineNr++
                if (lineNr % 100 == 0) {
                    session.flush()
                    session.clear()
                }
            }
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
            transaction.commit()
            session.close()
        }
    }

    @Transactional
    def createNewRecord(String[] fields, Session session) {
        String dataSourceName = fields[0]
        String campaignName = fields[1]
        String day = fields[2]
        String clicks = fields[3]
        String impressions = fields[4]

        Date date = dateFormat.parse(day)

        DataSource dataSource = DataSource.findByName(dataSourceName)
        if (!dataSource) {
            dataSource = new DataSource(name: dataSourceName).save()
        }

        Campaign campaign = Campaign.findByName(campaignName)
        if (!campaign) {
            campaign = new Campaign(name: campaignName).save()
        }

        DailyPerformance dailyPerformance = new DailyPerformance(
                dataSource: dataSource,
                campaign: campaign,
                date: date,
                clicks: clicks as Long,
                impressions: impressions as Long,
        )
        session.save(dailyPerformance)
    }
}
