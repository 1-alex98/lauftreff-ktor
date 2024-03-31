package vontrostorff.de.database

import org.ktorm.dsl.isNotNull
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import vontrostorff.de.database.CourseParticipations.bindTo
import java.time.Instant
import java.time.LocalDate

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    var id: Int
    var name: String
    var email: String
    var trainer: Boolean
}
interface Semster : Entity<Semster> {
    companion object : Entity.Factory<Semster>()
    val id: Int
    var name: String
    var beginning: LocalDate
    var end: LocalDate
}
interface Course : Entity<Course> {
    companion object : Entity.Factory<Course>()
    val id: Int
    var name: String
    var firstDate: Instant
    var weekly: Boolean
    var semster: Semster
}
interface CourseHappening : Entity<CourseHappening> {
    companion object : Entity.Factory<CourseHappening>()
    var id: Int
    var date: Instant
    var sentEmail: Boolean
    var course: Course
}
interface CourseParticipation : Entity<CourseParticipation> {
    companion object : Entity.Factory<CourseParticipation>()
    val id: Int
    var user: User
    var courseHappening: CourseHappening
}
interface GlobalValue : Entity<GlobalValue> {
    companion object : Entity.Factory<GlobalValue>()
    val id: Int
    var key: String
    var value: String
}

object Users : Table<User>("user") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val email = varchar("email").bindTo { it.email }
    val name = varchar("name").bindTo { it.name }
    val trainer = boolean("trainer").bindTo { it.trainer }.isNotNull()
}
object Semesters : Table<Semster>("semester") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val name = varchar("name").bindTo { it.name }
    val beginning = date("beginning").bindTo { it.beginning }
    val end = date("end").bindTo { it.end }
}
object Courses : Table<Course>("course") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val name = varchar("name").bindTo { it.name }
    val firstDate = timestamp("first_date").bindTo { it.firstDate }
    val weekly = boolean("weekly").bindTo { it.weekly }
    val semester = int("semester").references(Semesters) {
        it.semster
    }
}
object CourseHappenings : Table<CourseHappening>("course_happening") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val date = timestamp("date").bindTo { it.date }
    val sentEmail = boolean("sent_email").bindTo { it.sentEmail }
    val course = int("course").references(Courses) {
        it.course
    }
}
object CourseParticipations : Table<CourseParticipation>("course_participation") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val courseHappening = int("course_happening").references(CourseHappenings) {
        it.courseHappening
    }
    val user = int("user").references(Users) {
        it.user
    }
}
object GlobalValues : Table<GlobalValue>("global_value") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val key = varchar("key").bindTo { it.key }
    val value = varchar("value").bindTo { it.value }

}