package vontrostorff.de.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import vontrostorff.de.database.CourseHappening
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.database.User
import vontrostorff.de.mail.sendHappeningEmail
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Scheduler {
    private val logger: Logger = LoggerFactory.getLogger(Scheduler.javaClass)
    private val executor = Executors.newScheduledThreadPool(1)

    fun start(){
        executor.scheduleAtFixedRate({ internalRun() },0, 1, TimeUnit.HOURS)
    }

    private fun internalRun(){
        logger.info("Running scheduled tasks")
        try {
            createMissingHappenings()
        } catch (t:Throwable) {
            logger.error("createMissingHappenings() failed:", t)
        }
        try {
            sentEmails()
        } catch (t:Throwable) {
            logger.error("createMissingHappenings() failed:", t)
        }
    }

    private fun sentEmails() {
        val emailsToSent = DatabaseService.happeningsWithoutEmailSent()
        val users = DatabaseService.allUsers()
        for(happening in emailsToSent){
            for(user in users){
                sendHappeningEmailForUser(user, happening)
            }
            happening.sentEmail = true
            DatabaseService.updateHappening(happening)
        }
    }

    private fun sendHappeningEmailForUser(user: User, happening: CourseHappening) {
        sendHappeningEmail(user, happening)
    }

    private fun createMissingHappenings() {
        val currentSemester = DatabaseService.getSemesterByDate(LocalDate.now())
        val coursesBySemesterId = DatabaseService.coursesBySemesterId(currentSemester.id)
        for(course in coursesBySemesterId){
            if(!course.weekly){
                val courseHappening = CourseHappening {
                    this.course = course
                    date = course.firstDate
                }
                DatabaseService.insertHappeningIfNotExist(
                    courseHappening
                )
                continue
            }
            var tempDate = course.firstDate
            while (LocalDate.ofInstant(tempDate, ZoneId.systemDefault()).isBefore(currentSemester.end) || LocalDate.ofInstant(tempDate, ZoneId.systemDefault()).equals(currentSemester.end)){
                val courseHappening = CourseHappening {
                    this.course = course
                    date = tempDate
                }
                DatabaseService.insertHappeningIfNotExist(
                    courseHappening
                )
                tempDate = tempDate.plus(7, ChronoUnit.DAYS)
            }
        }
    }

    fun stop(){
        executor.shutdown()
    }
}