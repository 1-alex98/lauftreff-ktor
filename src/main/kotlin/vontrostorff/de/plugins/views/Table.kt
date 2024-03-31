package vontrostorff.de.plugins.views

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.templates.LayoutTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

public suspend fun PipelineContext<Unit, ApplicationCall>.tableView() {
    val semester = DatabaseService.getSemesterByDate(LocalDate.now())
    val userParticipationCount =
        DatabaseService.getUserParticipationCount(semester)

    call.respondHtmlTemplate(LayoutTemplate(call)) {
        content {
            h3("m2") {
                +"Semester: ${semester.name} (${semester.beginning.format(DateTimeFormatter.ofPattern("dd.MM.YY"))}-${semester.end.format(
                    DateTimeFormatter.ofPattern("dd.MM.YY"))})"
            }
            table("table m-2") {
                thead {
                    tr {
                        th { +"#" }
                        th { +"Name" }
                        th { +"Score" }
                    }
                }

                tbody {
                    var index = 0
                    var lastCount = Integer.MAX_VALUE
                    for (userAndCount in userParticipationCount.sortedBy { -it.count }) {
                        if (userAndCount.count < lastCount) {
                            if (!userAndCount.user.trainer) {
                                lastCount = userAndCount.count
                                index++
                            }
                        }
                        tr (classes = classesForTableRow(userAndCount)){
                            th {
                                if(!userAndCount.user.trainer) +((index).toString() + " ")
                                when (index) {
                                    1 -> {
                                        img {
                                            style = "height:1em"
                                            src = "/static/yellow-shirt.svg"
                                        }
                                    }
                                }
                            }
                            td { +userAndCount.user.name.escapeHTML() }
                            td { +userAndCount.count.toString() }
                        }

                    }
                }
            }
        }
    }
}

fun classesForTableRow(userAndCount: DatabaseService.UserAndCount): String? {
    if(userAndCount.user.trainer){
        return "trainer-row"
    }
    return null;
}