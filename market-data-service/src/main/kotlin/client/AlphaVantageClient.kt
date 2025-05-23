package com.coded.spring.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AlphaVantageClient(
    @Value("\${alpha.apikey}") private val apiKey: String
) {
    private val restTemplate = RestTemplate()
    private val baseUrl = "https://www.alphavantage.co/query"

    @Cacheable("marketData")
    fun fetchTimeSeries(function: String, symbol: String, interval: String? = null): String {
        val url = buildString {
            append("$baseUrl?function=$function&symbol=$symbol&apikey=$apiKey")
            if (interval != null) append("&interval=$interval")
            append("&outputsize=full")
        }
        return restTemplate.getForObject(url, String::class.java) ?: "{}"
    }

    fun searchSymbol(keyword: String): String {
        val url = "https://www.alphavantage.co/query" +
                "?function=SYMBOL_SEARCH" +
                "&keywords=${keyword}" +
                "&apikey=$apiKey"

        return restTemplate.getForObject(url,String::class.java) ?: "{}"  // Use your HTTP client here (e.g., WebClient, RestTemplate, khttp, etc.)
    }
}