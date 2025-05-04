package authentication.security

import authentication.jwt.JwtService
import authentication.users.UsersService
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.authentication.*
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "AUTHENTICATION")
@RestController
@RequestMapping("/auth/v1")
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
    private val usersService: UsersService
) {

    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthenticationRequest): AuthenticationResponse {
        val authToken = UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password)
        val authentication = authenticationManager.authenticate(authToken)

        if (authentication.isAuthenticated) {
            val token = jwtService.generateToken(authRequest.username)
            return AuthenticationResponse(token)
        } else {
            throw UsernameNotFoundException("Invalid login request.")
        }
    }

    @PostMapping("/check-token")
    fun checkToken(principal: Principal): TokenCheckResponse {
        println("🔐 Received token for user: ${principal.name}")
        val user = usersService.findByUsername(principal.name)
        println("✅ Token is valid, userId=${user.id}")
        return TokenCheckResponse(userId = user.id!!)
    }
}

data class AuthenticationRequest(val username: String, val password: String)
data class AuthenticationResponse(val token: String)
data class TokenCheckResponse(val userId: Long)