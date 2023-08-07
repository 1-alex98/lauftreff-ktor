package vontrostorff.de

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import vontrostorff.de.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
            configureTemplating()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertContains("Lauftreff Tracker", bodyAsText())
        }
    }
}
