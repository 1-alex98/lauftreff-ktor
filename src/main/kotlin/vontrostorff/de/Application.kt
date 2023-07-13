package vontrostorff.de

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.select
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import vontrostorff.de.database.User
import vontrostorff.de.plugins.*
import java.lang.reflect.Array.set
import java.time.LocalDate

object DatabaseService{
    val database = Database.connect("jdbc:postgresql://localhost:5432/postgres", user = "postgres", password = "postgres")

    fun createUser(email:String,name:String){
        database.insert(User) {
            set(it.name, name)
            set(it.email, email)
        }
    }

}

fun main() {
    embeddedServer(Netty, environment = engineEnvironment())
        .start(wait = true)
}

private fun engineEnvironment() = applicationEngineEnvironment {
    developmentMode = true
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
    }
}

fun Application.myApplicationModule() {
    configureTemplating()
    configureSerialization()
    configureRouting()
}
