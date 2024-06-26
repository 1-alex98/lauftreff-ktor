package vontrostorff.de.database

import io.ktor.server.plugins.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.*

object DatabaseService{
    private val database = Database.connect(System.getenv("DB_URL"))

    private val Database.users get() = this.sequenceOf(Users)
    private val Database.courseHappenings get() = this.sequenceOf(CourseHappenings)
    private val Database.globalValues get() = this.sequenceOf(GlobalValues)
    private val Database.courseParticipation get() = this.sequenceOf(CourseParticipations)

    fun createUser(email:String, name:String){
        val re = Regex("[^A-Za-z0-9 ]")
        val nameReplaced = re.replace(name, "")
        val user = User {
            this.name = nameReplaced
            this.email = email
        }
        database.users.add(user)
    }

    fun getSemesterByDate(date: LocalDate): Semster {
        val semesters = database
            .from(Semesters)
            .select()
            .whereWithConditions {
                it += Semesters.beginning lessEq date
                it += Semesters.end greaterEq date
            }
        return semesters.map { Semesters.createEntity(it, withReferences = false) }[0]
    }
    fun getSemestersNowAndInTheFuture(date: LocalDate): List<Semster> {
        val semesters = database
            .from(Semesters)
            .select()
            .whereWithConditions {
                it += Semesters.end greaterEq date
            }
        return semesters.map { Semesters.createEntity(it, withReferences = false)}
    }

    fun getSemesterByID(id: Int): Semster {
        val semesters = database
            .from(Semesters)
            .select()
            .whereWithConditions {
                it += Semesters.id eq id
            }
        return semesters.map { Semesters.createEntity(it, withReferences = false) }[0]
    }
    fun coursesBySemesterId(semesterId: Int): List<Course> {
        val query = database
            .from(Courses)
            .select()
            .whereWithConditions {
                it += Courses.semester eq semesterId
            }
        return query.map { Courses.createEntity(it, withReferences = false) }
    }

    fun insertHappeningIfNotExist(courseHappening: CourseHappening) {
        val query = database
            .from(CourseHappenings)
            .select()
            .whereWithConditions {
                it += CourseHappenings.course eq courseHappening.course.id
                it += CourseHappenings.date eq courseHappening.date
            }
        if(query.iterator().hasNext())
        {
            return
        }
        database.courseHappenings.add(courseHappening)
    }

    fun stop() {
        // Nothing to do
    }

    fun happeningsWithoutEmailSent(): List<CourseHappening> {
        val query = database
            .from(CourseHappenings)
            .leftJoin(Courses, on = CourseHappenings.course eq Courses.id)
            .select()
            .whereWithConditions {
                it += CourseHappenings.sentEmail eq false
                it += CourseHappenings.date lessEq LocalDateTime.now().toInstant(ZoneOffset.UTC)
            }
        return query.map { CourseHappenings.createEntity(it, withReferences = true) }
    }

    fun allUsers(): List<User> {
        return database.users.toList()
    }

    fun updateHappening(happening: CourseHappening) {
        database.courseHappenings.update(happening)
    }

    fun createJWTKeyIfNotExistsOrGet(key: String): String {
        val query = database
            .from(GlobalValues)
            .select()
            .whereWithConditions {
                it += GlobalValues.key eq "jwt-json-key"
            }
        val result = query.map { GlobalValues.createEntity(it, withReferences = true) }
        if(result.isNotEmpty())
        {
            return result[0].value
        }
        database.globalValues.add(
            GlobalValue {
                this.key = "jwt-json-key"
                value = key
            }
        )
        return key
    }

    fun deleteUserByEmail(email: String) {
        val id = database.users.find { it.email eq email }?.id ?: throw NotFoundException()
        database
            .delete(CourseParticipations) { it.user eq id }
        database
            .delete(Users) { it.email eq email}
    }

    fun registerParticipation(data: CourseParticipation): Boolean {
        if(database.courseParticipation.find {
                (it.courseHappening eq data.courseHappening.id).and(it.user eq data.user.id)
        } != null) {
            return false
        }
        database.courseParticipation.add(data)
        return true
    }
    data class UserAndCount(val user: User, val count: Int)
    fun getUserParticipationCount(semester:Semster ): List<UserAndCount> {
        val query = database
            .from(CourseParticipations)
            .leftJoin(CourseHappenings, on = CourseParticipations.courseHappening eq CourseHappenings.id)
            .leftJoin(Courses, on = CourseHappenings.course eq Courses.id)
            .select(CourseParticipations.user, count())
            .whereWithConditions{
                it += Courses.semester eq semester.id
            }
            .groupBy(CourseParticipations.user)
        return query.map {
            val userId = it.getInt(1)
            val count = it.getInt(2)
            UserAndCount(
                database.users.find { it.id eq userId }!!,
                count
            )
        }
    }
    data class CourseParticipationDateAndCourseName(val date:LocalDateTime, val name:String )
    fun getMyParticipations(userId:Int): List<CourseParticipationDateAndCourseName> {
        val query = database
            .from(CourseParticipations)
            .leftJoin(CourseHappenings, on = CourseParticipations.courseHappening eq CourseHappenings.id)
            .leftJoin(Courses, on = CourseHappenings.course eq Courses.id)
            .select(CourseHappenings.date, Courses.name)
            .whereWithConditions{
                it += CourseParticipations.user eq userId
            }
        return query.map {
            val date = it.getLocalDateTime(1)
            val name = it.getString(2)
            CourseParticipationDateAndCourseName(
                date!!,
                name!!
            )
        }
    }
}