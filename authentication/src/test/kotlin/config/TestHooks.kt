package config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import utils.GlobalToken

class TestHooks {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    companion object {
        private var isTokenGenerated = false
    }

    @Before
    fun generateJwtToken() {
        if (isTokenGenerated) return

        println("🚀 Generating Global JWT Token...")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // Register
        val registrationPayload = """
            {
              "name": "Global User",
              "age": 25,
              "username": "GlobalUser",
              "password": "password123"
            }
        """.trimIndent()
        val registerRequest = HttpEntity(registrationPayload, headers)
        var regResponse =restTemplate.postForEntity("/users/v1/register", registerRequest, String::class.java)

        println(regResponse)
        // Login
        val loginPayload = """
            {
              "username": "GlobalUser",
              "password": "password123"
            }
        """.trimIndent()
        val loginRequest = HttpEntity(loginPayload, headers)
        val response = restTemplate.postForEntity("/auth/login", loginRequest, String::class.java)

        println("🔎 Login Response Body: ${response.body}")

        // Extract JWT token from response JSON
        val responseBody = response.body
        val token = if (!responseBody.isNullOrBlank()) {
            val mapper = jacksonObjectMapper()
            try {
                mapper.readTree(responseBody).get("token")?.asText()
            } catch (e: Exception) {
                println("❌ Failed to extract token: ${e.message}")
                null
            }
        } else {
            null
        }

        GlobalToken.jwtToken = token
        println("✅ Global JWT Token Generated: $token")
        isTokenGenerated = true
    }
}