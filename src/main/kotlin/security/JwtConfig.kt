package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import server.models.requests.auth.AuthRequest
import utils.Properties
import java.util.*

object JwtConfig {

    private val secret = Properties.get("jwt.secret")
    private val issuer = Properties.get("jwt.issuer")
    private val realm = Properties.get("jwt.realm")

    private val algorithm = Algorithm.HMAC256(secret)

    fun configure(config: JWTAuthenticationProvider.Config) {
        config.realm = realm
        config.verifier(getVerifier())
        config.validate { credential ->
            if (credential.payload.getClaim("issuer").asString() != Properties.get("jwt.issuer"))
                JWTPrincipal(credential.payload) else null
        }
    }

    fun generateToken(user: AuthRequest): String = JWT.create()
        .withIssuer(issuer)
        .withClaim("username", user.username)
        .withExpiresAt(Date(System.currentTimeMillis() + 86000000)) // TODO: move to config
        .sign(algorithm)

    private fun getVerifier() = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()
}