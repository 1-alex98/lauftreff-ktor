package vontrostorff.de

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.html.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.mail.Mail
import vontrostorff.de.plugins.*
import vontrostorff.de.schedule.Scheduler
import vontrostorff.de.templates.LayoutTemplate
import java.util.*
import java.util.concurrent.TimeUnit

fun main() {
    val server = embeddedServer(Netty, environment = engineEnvironment()).start(false)
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 5, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}

private fun startStop() {

}

private fun engineEnvironment() = applicationEngineEnvironment {
    developmentMode = ! Objects.equals(System.getenv("DEVELOPMENT"), null)
    connector {
        port= 8080
        host= "0.0.0.0"
    }
    watchPaths = listOf(
        "build/classes/kotlin/main",
        "build/classes/main",
    )
    module {
        myApplicationModule()
        environment.monitor.subscribe(ApplicationStarted) {
            Scheduler.start()
        }
        environment.monitor.subscribe(ApplicationStopped) {
            Scheduler.stop()
            DatabaseService.stop()
        }
    }
}
class Main
private val log: Logger = LoggerFactory.getLogger(Mail.javaClass)

fun Application.myApplicationModule() {
    configureTemplating()
    configureSerialization()
    configureRouting()
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            vontrostorff.de.log.error("Uncaught error", cause)
            call.respondHtmlTemplate(LayoutTemplate(call), status = HttpStatusCode.InternalServerError) {
                content {
                    div(classes = "m-2") {
                        h1 {
                            +"Das hätte nicht passieren dürfen. Versuche es nochmal oder kontaktiere dem Administrator."
                        }
                        video {
                            attributes["muted"] = true.toString()
                            loop = true
                            style ="width: 100%"
                            autoPlay = true
                            controls = false
                            source {
                                src = "/static/error.mp4"
                                type="video/mp4"
                            }
                        }
                    }
                }
            }
        }
    }
}
