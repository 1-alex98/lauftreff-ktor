package vontrostorff.de.templates

import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*
import kotlinx.html.impl.DelegatingMap
import vontrostorff.de.JwtService

class LayoutTemplate(val call: ApplicationCall) : Template<HTML> {
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        head {
            title("Uni Bonn Lauftreff Anwesenheit")
            link(href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css", rel = "stylesheet")
            link(href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css", rel = "stylesheet")
            script{
                src = "https://cdn.plot.ly/plotly-2.27.0.min.js"
            }
            link{
                rel="manifest"
                href="/static/manifest.json"
            }
            link{
                rel="apple-touch-icon"
                href="/static/runner.png"
            }
            link{
                rel="icon"
                href="/static/runner.svg"
                sizes="any"
                type="image/svg+xml"
            }
            script {
                src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            }
            link(href = "/static/main.css", rel = "stylesheet")
            meta{
                name="viewport"
                content="width=device-width, initial-scale=1"
            }
        }
        body {
            div{
                navbar(call)
                div(classes="m-2"){
                    insert(content)
                }
            }
        }
    }

    private fun DIV.navbar(call: ApplicationCall) {
        nav(classes = "navbar bg-primary navbar-dark navbar-expand-lg") {
            div(classes = "container-fluid") {
                button(classes="navbar-toggler"){
                    type=ButtonType.button
                    attributes["data-bs-toggle"] = "collapse"
                    attributes["data-bs-target"] = "#navbarSupportedContent"
                    attributes["aria-controls"] = "navbarSupportedContent"
                    attributes["aria-expanded"] = "false"
                    attributes["aria-label"] = "Toggle navigation"
                    span(classes="navbar-toggler-icon"){}
                }

                div(classes = "collapse navbar-collapse" ) {
                    id = "navbarSupportedContent"
                    ul(classes = "navbar-nav me-auto mb-2 mb-lg-0") {
                        li("nav-item") {
                            a(classes = "nav-link", href = "/") {
                                i(classes="bi bi-info-circle"){}
                                +" Info"
                            }
                        }
                        li("nav-item") {
                            a(classes = "nav-link", href = "/table") {
                                i(classes="bi bi-list-columns-reverse"){}
                                +" Rangliste"
                            }
                        }
                        li("nav-item") {
                            a(classes = "nav-link", href = "/calendar") {
                                i(classes="bi bi-calendar-week"){}
                                +" Kalender"
                            }
                        }
                        if(JwtService.getUserId(call.request.cookies["token"]) != null ){
                            li("nav-item") {
                                a(classes = "nav-link", href = "/my") {
                                    i(classes="bi bi-calendar3-fill"){}
                                    +" Teilnahmen"
                                }
                            }
                        }
                        li("nav-item") {
                            a(classes = "nav-link", href = "/register") {
                                i(classes="bi bi-person-add"){}
                                +" Registrieren"
                            }
                        }
                        li("nav-item") {
                            a(classes = "nav-link", href = "/impressum") {
                                +" Impressum"
                            }
                        }
                    }
                }
            }
        }
    }

}
