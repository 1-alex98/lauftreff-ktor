package vontrostorff.de.plugins

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import vontrostorff.de.mail.Mail
import java.io.File

fun Application.configureRouting() {

    routing {
        get("robots.txt") {
            this@routing.javaClass.getResource("/static/robots.txt")?.let { it1 -> call.respondText(it1.readText()) }
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
