package vontrostorff.de.plugins

import io.ktor.server.html.*
import kotlinx.html.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import org.postgresql.util.PSQLException
import vontrostorff.de.JwtService
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.mail.sendWelcomeEmail
import vontrostorff.de.templates.LayoutTemplate
import java.time.LocalDate

fun Application.configureTemplating() {

    routing {
        get("/") {
            home()
        }
        registerGet()
        registerPost()
        get("table") {
            table()
        }
        get("/impressum") {
            impressum()
        }
        get("dsgvo"){
            dsgvo()
        }
        get("post-register") {
            postRegister()
        }
        get("unsubscribe"){
            unsubscribe()
        }
        get("participate"){
            participate()
        }
        get("/error") {
            throw Exception()
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.home() {
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1{
                +"Lauftreff Tracker"
            }
            p {
                +"Hier kannst du dich registrieren, um an der Challenge des Lauftreffs der Uni Bonn teilzunehmen. Wer am häufigsten anwesend ist, trägt das gelbe Trikot."
            }
            p {
                +"Die Anwesenheit wird über eine E-Mail getrackt, die dir jede Woche zugeschickt wird."
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.table() {
    val semester = DatabaseService.getSemesterByDate(LocalDate.now())
    val userParticipationCount =
        DatabaseService.getUserParticipationCount(semester)
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h3("m2") {
                +"Semester: ${semester.name}"
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
                    for((index, userAndCount) in userParticipationCount.withIndex()){
                        tr {
                            if(index == 0){
                                th {
                                    +((index+1).toString() + " ")
                                    img {
                                        style = "height:1em"
                                        src = "/static/yellow-shirt.svg"
                                    }
                                }
                            } else if (index == 1){
                                th { +(index+1).toString() }
                            } else {
                                th { +(index+1).toString() }
                            }
                            td { + userAndCount.user.name.escapeHTML() }
                            td { + userAndCount.count.toString() }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.impressum() {
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1 { +"Impressum" }
            p {
                +"Verantwortlich für den Inhalt dieser Webseite:"
            }
            p { +"Alexander von Trostorff" }
            p { +"Am Köppekreuz 11" }
            p { +"53225 Bonn" }
            p { +"Die Webseite ist ein rein privates Projekt." }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.dsgvo() {
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1 { +"Datenschutzvereinbarung für die Webseite \"Lauftreff Tracker\"" }
            p { +"Datum der letzten Aktualisierung: 01.08.2023" }

            p {
                +"Wir bei Lauftreff Tracker nehmen den Schutz Ihrer persönlichen Daten sehr ernst. Diese Datenschutzvereinbarung erläutert, wie wir Ihre personenbezogenen Daten sammeln, verwenden, offenlegen und schützen, wenn Sie unsere Webseite nutzen. Indem Sie sich für die Nutzung der Webseite \"Lauftreff Tracker\" registrieren und unsere Dienste nutzen, stimmen Sie den in dieser Datenschutzvereinbarung beschriebenen Praktiken zu."
            }

            h2 { +"1. Erhebung von personenbezogenen Daten:" }
            p { +"Bei der Registrierung auf Lauftreff Tracker erfassen wir von Ihnen folgende personenbezogene Daten:" }
            ul {
                li { +"Alias-Name (der zur Anonymisierung dient)" }
                li { +"E-Mail-Adresse" }
            }

            h2 { +"2. Nutzung Ihrer personenbezogenen Daten:" }
            p {
                +"Die von Ihnen bereitgestellten personenbezogenen Daten werden für folgende Zwecke genutzt:"
            }
            ul {
                li { +"Zur Verwaltung und Bereitstellung der Dienste von Lauftreff Tracker, einschließlich der wöchentlichen E-Mail-Benachrichtigungen über Ihre Anwesenheit bei Lauftreff-Veranstaltungen." }
                li { +"Um Sie über wichtige Änderungen oder Aktualisierungen bezüglich unserer Dienste zu informieren." }
                li { +"Zur Verbesserung unserer Dienste und der Benutzererfahrung auf der Webseite." }
            }

            h2 { +"3. Speicherung Ihrer Daten:" }
            p {
                +"Ihre personenbezogenen Daten werden auf sicheren Servern gespeichert und verarbeitet. Wir bewahren Ihre Daten so lange auf, wie dies für die Zwecke, für die sie erhoben wurden, erforderlich ist, oder so lange dies gesetzlich vorgeschrieben ist."
            }

            h2 { +"4. Weitergabe von Daten an Dritte:" }
            p {
                +"Wir geben Ihre personenbezogenen Daten nicht an Dritte weiter, es sei denn, dies ist gesetzlich vorgeschrieben oder zur Bereitstellung der Dienste von Lauftreff Tracker erforderlich. In jedem Fall achten wir darauf, dass Ihre Daten angemessen geschützt bleiben."
            }

            h2 { +"5. Ihre Rechte:" }
            p {
                +"Sie haben das Recht, Auskunft über die von uns gespeicherten personenbezogenen Daten zu verlangen und diese gegebenenfalls zu korrigieren oder zu löschen. Sie können auch der Verwendung Ihrer Daten für die Zukunft widersprechen. Bitte kontaktieren Sie uns unter den unten genannten Kontaktdaten, um von Ihren Rechten Gebrauch zu machen."
            }

            h2 { +"6. Sicherheit:" }
            p {
                +"Wir setzen angemessene technische und organisatorische Sicherheitsmaßnahmen ein, um Ihre personenbezogenen Daten vor unbefugtem Zugriff, Verlust oder Missbrauch zu schützen."
            }

            h2 { +"7. Kontakt:" }
            p {
                +"Bei Fragen, Anliegen oder Anfragen bezüglich dieser Datenschutzvereinbarung oder unserer Datenschutzpraktiken können Sie sich an folgende Kontaktperson wenden:"
            }
            p { +"Alexander von Trostorff" }
            p { +"E-Mail: s6alvont(at)uni-bonn.com" }

            h2 { +"8. Änderungen der Datenschutzvereinbarung:" }
            p {
                +"Wir behalten uns das Recht vor, diese Datenschutzvereinbarung jederzeit zu ändern oder zu aktualisieren. Jegliche Änderungen werden auf dieser Webseite veröffentlicht und das Datum der letzten Aktualisierung wird entsprechend angepasst. Bitte überprüfen Sie diese Seite regelmäßig, um sich über Änderungen auf dem Laufenden zu halten."
            }
            h2 { +"9. Hintergrund:" }
            p {
                +"Es wird gern Gewinn mit dieser Webseite gemacht. Es wird keine Werbung geschaltet. Es handelt sich hierbei um ein privates \"Spaß\"-Projekt."
            }

            p {
                +"Durch die Nutzung der Webseite \"Lauftreff Tracker\" stimmen Sie den Bedingungen dieser Datenschutzvereinbarung zu."
            }

        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.postRegister() {
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1 {
                +"Du wurdest registriert"
            }
            p {
                +"""
                        Du bekommst ab jetzt jeden Dienstag gegen 19 Uhr eine E-Mail.
                        In der E-Mail gibt es einen Link, mit dem du deine Teilnahme bestätigen kannst.
                        Deine Teilnahmen tauchen dann auf der 
                        """
                a(href = "/table") {
                    +"Rangliste "
                }
                +"auf."
            }

        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.unsubscribe() {
    val token = call.request.queryParameters["token"]!!
    try {
        val email = JwtService.getEmailFromUnsubscribeToken(token)
        try {
            DatabaseService.deleteUserByEmail(email)
        } catch (e: NotFoundException) {
            errorFlash("Nutzer nicht gefunden. Dann sind deine Daten wahrscheinlich schon gelöscht.")
            return
        }
    } catch (e: Throwable) {
        call.application.environment.log.error("Invalid token",e)
        errorFlash("Invalider Token. Möglicherweise ist der Token abgelaufen.")
        return
    }

    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1 {
                +"Deine Daten wurden gelöscht"
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.participate() {
    val token = call.request.queryParameters["token"]!!
    try {
        val data = JwtService.getDataFromParticipateToken(token)
        try {
            DatabaseService.registerParticipation(data)
        } catch (e: Exception) {
            call.application.environment.log.error("Register participation failed",e)
            errorFlash("Eintrag bereits vorhanden oder Nutzer gelöscht. Hast du den Link bereits geklickt?")
            return
        }
    } catch (e: Throwable) {
        call.application.environment.log.error("Invalid token",e)
        errorFlash("Invalider Token. Du musst den Link innerhalb einer Woche klicken.")
        return
    }

    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            h1 {
                +"Wir haben deine Teilnahme erfasst: "
                a(href = "table"){
                    +"Zur Rangliste"
                }
            }
        }
    }
}

private fun Routing.registerPost() {
    post("/register") {
        val formParameters = call.receiveParameters()
        val email = formParameters["email"].toString()
        val displayName = formParameters["displayName"].toString()
        call.application.environment.log.info("received register post:$email $displayName")
        try {
            sendWelcomeEmail(email)
        } catch (e: Exception){
            call.application.environment.log.warn("Could not send email for register", e)
            errorFlash("Konnte E-Mail nicht senden!")
            return@post
        }
        try {
            DatabaseService.createUser(email,displayName)
        } catch (e: PSQLException){
            call.application.environment.log.warn("Could not register", e)
            if(e.message?.contains("duplicate key") == true){
                errorFlash("Nutzer existiert bereits!")
                return@post
            }
        }
        call.respondRedirect("/post-register")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.errorFlash(error:String) {
    call.respondHtmlTemplate(LayoutTemplate()) {
        content {
            div(classes = "alert alert-danger") {
                role = "alert"
                +error
            }
        }
    }
}

private fun Routing.registerGet() {
    get("/register") {
        call.respondHtmlTemplate(LayoutTemplate()) {
            content {
                div(classes = "container-fluid login-container") {
                    form(classes = "register-form", action = "/register") {
                        method = FormMethod.post
                        div(classes = "") {
                            input(name = "displayName", type = InputType.text) {
                                placeholder = "Angezeigter Name(Alias)"
                                classes = setOf("form-control", "username")
                                required = true
                            }
                            input(name = "email", type = InputType.email) {
                                placeholder = "Email"
                                classes = setOf("form-control", "email")
                                required = true
                            }
                            div(classes = "form-check mt-2") {
                                input(name = "email", classes = "form-check-input", type = InputType.checkBox) {
                                    required = true
                                }
                                label(classes="form-check-label ms-2"){
                                    p {
                                        +"Ich habe die "
                                        a(href = "/dsgvo"){
                                            +"Datenschutzvereinbarung"
                                        }
                                        +" gelesen"
                                    }
                                }
                            }
                        }
                        button(classes = "submit-button btn btn-outline-primary btn-block") {
                            +"Registrieren"
                        }
                    }
                }
            }
        }
    }
}
