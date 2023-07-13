package vontrostorff.de.database

import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object User : Table<Nothing>("user") {
    val email = varchar("email").primaryKey()
    val name = varchar("name")
}