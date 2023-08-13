package vontrostorff.de.templates

import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTemplate: Template<HTML> {
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        head {
            title("Uni Bonn Lauftreff Anwesenheit")
            link(href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css", rel = "stylesheet")
            link{
                rel="icon"
                href="/static/runner.svg"
                sizes="any"
                type="image/svg+xml"
            }
            script {
                src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"
            }
            link(href = "/static/main.css", rel = "stylesheet")
            meta{
                name="viewport"
                content="width=device-width, initial-scale=1"
            }
        }
        body {
            div{
                navbar()
                div(classes="m-2"){
                    insert(content)
                }
            }
        }
    }

    private fun DIV.navbar() {
        nav(classes = "navbar navbar-expand-sm bg-primary navbar-dark") {
            div(classes = "container-fluid") {
                ul(classes = "navbar-nav") {
                    li("nav-item") {
                        a(classes = "nav-link", href = "/") {
                            +"Info"
                        }
                    }
                    li("nav-item") {
                        a(classes = "nav-link", href = "/table") {
                            +"Rangliste"
                        }
                    }
                    li("nav-item") {
                        a(classes = "nav-link", href = "/register") {
                            +"Registrieren"
                        }
                    }
                    li("nav-item") {
                        a(classes = "nav-link", href = "/impressum") {
                            +"Impressum"
                        }
                    }
                }
            }
        }
    }

}
