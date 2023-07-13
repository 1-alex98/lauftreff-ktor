package vontrostorff.de.plugins

import io.ktor.server.html.*
import kotlinx.html.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import vontrostorff.de.DatabaseService
import vontrostorff.de.templates.LayoutTemplate

fun Application.configureTemplating() {

    routing {
        get("/") {
            call.respondHtmlTemplate(LayoutTemplate()) {
                content {
                    h1 {
                        +"Hello"
                    }
                }
            }
        }
        registerGet()
        registerPost()
        get("table") {
            call.respondHtmlTemplate(LayoutTemplate()) {
                content {
                    h1 {
                        +"table"
                    }
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
        DatabaseService.createUser(email,displayName)
        call.respondRedirect("/table")
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
                                placeholder = "Angezeigter Name"
                                classes = setOf("form-control", "username")
                            }
                            input(name = "email", type = InputType.email) {
                                placeholder = "Email"
                                classes = setOf("form-control", "email")
                            }
                        }
                        button(classes = "submit-button btn btn-outline-primary btn-block") {
                            +"Register"
                        }
                    }
                }
            }
        }
    }
}
