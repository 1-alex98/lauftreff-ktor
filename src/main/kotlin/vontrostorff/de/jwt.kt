package vontrostorff.de

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import vontrostorff.de.database.CourseHappening
import vontrostorff.de.database.CourseParticipation
import vontrostorff.de.database.DatabaseService
import vontrostorff.de.database.User
import vontrostorff.de.mail.Mail
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

private val log: Logger = LoggerFactory.getLogger(JwtService.javaClass)

object JwtService{
    val key: ECKey
    init {
        val ecJWK: ECKey = ECKeyGenerator(Curve.P_256)
            .keyID("lt-main")
            .generate()
        val keyJsonString = DatabaseService.createJWTKeyIfNotExistsOrGet(
            ecJWK.toJSONString()
        )
        key = ECKey.parse(keyJsonString)
    }

    fun unsubscribeToken(email: String): String {
        // Create the EC signer
        // Create the EC signer
        val signer: JWSSigner = ECDSASigner(key)

        // Creates the JWS object with payload
        val jwtClaimsSetBuilder = JWTClaimsSet.Builder()
        jwtClaimsSetBuilder.claim("email", email)
        jwtClaimsSetBuilder.audience("unsubscribe")
        jwtClaimsSetBuilder.expirationTime(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
        // Creates the JWS object with payload
        val jwsObject = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKeyID()).build(),
            jwtClaimsSetBuilder.build()
        )

        // Compute the EC signature

        // Compute the EC signature
        jwsObject.sign(signer)

        // Serialize the JWS to compact form

        // Serialize the JWS to compact form
        return jwsObject.serialize()
    }
    fun getEmailFromUnsubscribeToken(token: String): String {
        val signedJWT = SignedJWT.parse(token)
        val verifier: JWSVerifier = ECDSAVerifier(key)
        signedJWT.verify(verifier)
        if(!signedJWT.jwtClaimsSet.audience[0].equals("unsubscribe")) {
            throw Exception("wrong audience")
        }
        if(!signedJWT.jwtClaimsSet.expirationTime.after(Date())) {
            throw Exception("expired")
        }

        return signedJWT.jwtClaimsSet.getStringClaim("email")
    }

    fun participateToken(userId: Int, happeningId: Int): String {
        // Create the EC signer
        // Create the EC signer
        val signer: JWSSigner = ECDSASigner(key)

        // Creates the JWS object with payload
        val jwtClaimsSetBuilder = JWTClaimsSet.Builder()
        jwtClaimsSetBuilder.claim("userId", userId)
        jwtClaimsSetBuilder.claim("happeningId", happeningId)
        jwtClaimsSetBuilder.audience("participate")
        jwtClaimsSetBuilder.expirationTime(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
        // Creates the JWS object with payload
        val jwsObject = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKeyID()).build(),
            jwtClaimsSetBuilder.build()
        )

        // Compute the EC signature

        // Compute the EC signature
        jwsObject.sign(signer)

        // Serialize the JWS to compact form

        // Serialize the JWS to compact form
        return jwsObject.serialize()
    }

    fun getDataFromParticipateToken(token: String): CourseParticipation {
        val signedJWT = signedJWTParticipate(token, "participate")

        return CourseParticipation {
            user = User {
                id = signedJWT.jwtClaimsSet.getIntegerClaim("userId")
            }
            courseHappening = CourseHappening{
                id = signedJWT.jwtClaimsSet.getIntegerClaim("happeningId")
            }
        }
    }

    fun getLoginTokenFromParticipationToken(token: String): String {
        val signedJWT = signedJWTParticipate(token, "participate")

        val id = signedJWT.jwtClaimsSet.getIntegerClaim("userId")
        val signer: JWSSigner = ECDSASigner(key)

        val jwtClaimsSetBuilder = JWTClaimsSet.Builder()
        jwtClaimsSetBuilder.claim("userId", id)
        jwtClaimsSetBuilder.audience("login")
        jwtClaimsSetBuilder.expirationTime(Date.from(Instant.now().plus(30*6, ChronoUnit.DAYS)))
        val jwsObject = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKeyID()).build(),
            jwtClaimsSetBuilder.build()
        )
        jwsObject.sign(signer)
        return jwsObject.serialize()
    }

    private fun signedJWTParticipate(token: String, audience: String): SignedJWT {
        val signedJWT = SignedJWT.parse(token)
        val verifier: JWSVerifier = ECDSAVerifier(key)
        signedJWT.verify(verifier)
        if (!signedJWT.jwtClaimsSet.audience[0].equals(audience)) {
            throw Exception("wrong audience")
        }
        if (!signedJWT.jwtClaimsSet.expirationTime.after(Date())) {
            throw Exception("expired")
        }
        return signedJWT
    }

    fun getUserId(token: String?): Int? {
        if(token == null) return null
        return try {
            val signedJWTParticipate = signedJWTParticipate(token, "login")
            signedJWTParticipate.jwtClaimsSet.getIntegerClaim("userId")
        } catch (e: Exception) {
            log.warn("login token invalid", e)
            null
        }
    }
}