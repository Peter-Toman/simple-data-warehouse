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

    def grailsApplication
    SessionFactory sessionFactory

    def init = { servletContext ->
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        JSON.registerObjectMarshaller(Date) {
            DateFormat dateFormat = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT)
            return dateFormat.format(it)
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

        Date date = new SimpleDateFormat(GlobalStrings.DATE_ONLY_FORMAT).parse(day)

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
