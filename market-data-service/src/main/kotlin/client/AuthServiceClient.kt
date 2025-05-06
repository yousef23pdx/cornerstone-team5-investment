package com.coded.spring.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

data class TokenCheckResponse(val userId: Long)

@Component
class AuthServiceClient(
    @Value("\${auth.base-url}") private val baseUrl: String
) {
    private val restTemplate = RestTemplate()

    fun checkToken(token: String): TokenCheckResponse? {
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }
        val entity = HttpEntity<Void>(headers)

        val resp = restTemplate.exchange(
            "$baseUrl/auth/v1/check-token",
            HttpMethod.POST,
            entity,
            TokenCheckResponse::class.java
        )
        println("👉 Token check response: ${resp.statusCode} - ${resp.body}")
        return resp.body
    }
}
