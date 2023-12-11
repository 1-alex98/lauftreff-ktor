/*
* Sending an email using kotlin and javax.mail
*
* Usage: java -jar app.jar <user> <password> <from> <to> <cc>
*/
package vontrostorff.de.mail

import io.ktor.http.*
import jakarta.mail.*
import jakarta.mail.internet.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import vontrostorff.de.JwtService
import vontrostorff.de.database.CourseHappening
import vontrostorff.de.database.User
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


object Mail{
}

private val log: Logger = LoggerFactory.getLogger(Mail.javaClass)

val mailSession = createMailSession()
fun sendWelcomeEmail(receiverEmail: String) {
    var emailHtml = Mail.javaClass.getResource("/mail/welcome.html")?.readText()
    emailHtml = replaceUnsubscribe(emailHtml, receiverEmail)
    sendEmail(
        receiverEmail, "Willkommen Lauftreff Uni Bonn",
        """
    Willkommen
    Danke, dass du dich registriert hast. Ab nun wirst du jeden Dienstag gegen 19 Uhr eine E-Mail bekommen. Dort wird es einen Knopf geben, mit dem du deine Anwesenheit bestätigen kannst.
    Auf der Webseite siehst du dann, wer wie oft da war.
    Der jeweils führende und der zweite trägt dann das jeweilige T-shirt.
    """.trimIndent(), emailHtml
    )
    log.info("Send welcome email to $receiverEmail")
}
fun sendHappeningEmail(user: User, courseHappening: CourseHappening) {
    var emailHtml = Mail.javaClass.getResource("/mail/participated_question.html")?.readText()
    emailHtml = replaceUnsubscribe(emailHtml, user.email)
    emailHtml = replaceButtonLink(emailHtml, user, courseHappening)
    emailHtml = replaceRankButtonLink(emailHtml)
    emailHtml = replaceCourse(emailHtml,courseHappening.course.name)
    emailHtml = replaceVariable(emailHtml, "date", LocalDate.ofInstant(courseHappening.date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.YY")))
    if (emailHtml != null) {
        sendEmail(
            user.email, "Warst du heute ${LocalDate.ofInstant(courseHappening.date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM"))} da?",
        """
        Warst du heute beim Lauftreff? Wenn ja klicke unten auf den Link, damit wir es zählen können...
        Email muss als HTML angezeigt werden!!!
        """.trimIndent(), emailHtml
        )
        log.info("Send ask email to ${user.email}")
    }
}

private fun replaceUnsubscribe(emailHtml: String?, receiverEmail: String): String{
    val urlBuilder = URLBuilder(System.getenv("HOST_URL"))
    urlBuilder.path("unsubscribe")
    urlBuilder.parameters.clear()
    urlBuilder.parameters.append("token", JwtService.unsubscribeToken(receiverEmail))
    return replaceVariable(emailHtml, "unsubscribe", urlBuilder.buildString())!!
}

private fun replaceCourse(emailHtml: String?, courseName: String): String{
    assert(courseName.matches(Regex("[äöüÄÖÜa-zA-Z0-9 .!/?]*"))) {
        "courseName String contains invalid characters."
    }
    return replaceVariable(emailHtml, "course", courseName)!!
}
private fun replaceButtonLink(emailHtml: String?, user: User, courseHappening: CourseHappening): String{
    val urlBuilder = URLBuilder(System.getenv("HOST_URL"))
    urlBuilder.path("participate")
    urlBuilder.parameters.clear()
    urlBuilder.parameters.append("token", JwtService.participateToken(user.id, courseHappening.id))
    return replaceVariable(emailHtml, "link", urlBuilder.buildString())!!
}
private fun replaceRankButtonLink(emailHtml: String?): String{
    val urlBuilder = URLBuilder(System.getenv("HOST_URL"))
    urlBuilder.path("table")
    return replaceVariable(emailHtml, "linkRank", urlBuilder.buildString())!!
}

fun replaceVariable(emailHtml: String?, name: String, value: String): String? {
    return emailHtml?.replace("\${$name}", value)
}

private fun sendEmail(receiverEmail: String, subject: String, plainText: String, textHtml: String) {
    if(!Objects.equals(System.getenv("DEVELOPMENT"), null)){
        log.info("$receiverEmail : $subject, \n$textHtml")
        return
    }
    try {
        // Create MimeMessage object
        val message = MimeMessage(mailSession)

        // Set sender and recipient
        val senderMail = System.getenv("EMAIL_USER")
        message.setFrom(InternetAddress(senderMail))
        message.addRecipient(Message.RecipientType.TO, InternetAddress(receiverEmail))

        // Set subject
        message.subject = subject

        // Create multipart/alternative content
        val multipart = MimeMultipart("alternative")

        // Create plain text version
        val plainTextPart = MimeBodyPart()
        plainTextPart.setText(plainText, "UTF-8")

        // Create HTML version
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(textHtml, "text/html; charset=UTF-8")

        // Add parts to the multipart content
        multipart.addBodyPart(plainTextPart)
        multipart.addBodyPart(htmlPart)

        // Set content type of the message to "mixed/alternative"
        message.setContent(multipart)

        // Send the email
        Transport.send(message)
    } catch (e: MessagingException) {
        e.printStackTrace()
    }
}

private fun createMailSession(): Session? {
    if(!Objects.equals(System.getenv("DEVELOPMENT"), null)){
        log.info("No init of email in DEVELOPMENT")
        return null
    }
    // SMTP server configuration properties
    val properties = System.getProperties()
    properties["mail.smtp.host"] = "mail.uni-bonn.de"
    properties["mail.smtp.port"] = "587"
    val senderMail = System.getenv("EMAIL_USER")
    properties.setProperty("mail.smtp.user", senderMail)
    properties["mail.smtp.auth"] = true
    properties["mail.smtp.socketFactory.port"] = 587
    properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
    properties["mail.smtp.socketFactory.fallback"] = true
    properties["mail.smtp.starttls.enable"] = true

    properties.setProperty("mail.smtp.password", System.getenv("EMAIL_PASSWORD"))
    val session = Session.getDefaultInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(senderMail, System.getenv("EMAIL_PASSWORD"))
        }
    })
    return session
}
