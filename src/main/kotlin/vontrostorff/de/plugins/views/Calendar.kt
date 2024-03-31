package vontrostorff.de.plugins.views

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.templates.LayoutTemplate
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


public suspend fun PipelineContext<Unit, ApplicationCall>.calendarView() {
    val semester = DatabaseService.getSemesterByDate(LocalDate.now())
    val coursesThisSemester =
        DatabaseService.coursesBySemesterId(semester.id)
            .sortedBy { it.firstDate }

    call.respondHtmlTemplate(LayoutTemplate(call)) {
        content {
            h3("m2") {
                +"Semester: ${semester.name} (${semester.beginning.format(DateTimeFormatter.ofPattern("dd.MM.YY"))}-${semester.end.format(
                    DateTimeFormatter.ofPattern("dd.MM.YY"))})"
            }
            table("table m-2") {
                thead {
                    tr {
                        th { +"Name" }
                        th { +"Datum ab" }
                        th { +"Modus" }
                    }
                }

                tbody {
                    for (course in coursesThisSemester) {
                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            .withZone(ZoneId.systemDefault())
                        tr {
                            td { +course.name }
                            td { + formatter.format(course.firstDate) }
                            td { +(if(course.weekly)"Wöchentlich" else "Einmalig") }
                        }

                    }
                }
            }
        }
    }
}
