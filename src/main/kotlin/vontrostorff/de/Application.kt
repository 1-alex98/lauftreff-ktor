package vontrostorff.de

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.html.*
import org.ktorm.database.Database
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.database.User
import vontrostorff.de.database.Users
import vontrostorff.de.plugins.*
import vontrostorff.de.schedule.Scheduler
import vontrostorff.de.templates.LayoutTemplate
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
    developmentMode = !System.getenv("DEVELOPMENT").equals(null)
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

fun Application.myApplicationModule() {
    configureTemplating()
    configureSerialization()
    configureRouting()
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondHtmlTemplate(LayoutTemplate(), status = HttpStatusCode.InternalServerError) {
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
