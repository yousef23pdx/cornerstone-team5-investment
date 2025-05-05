package com.coded.spring.service


import com.coded.spring.client.AlphaVantageClient
import org.springframework.stereotype.Service

@Service
class MarketDataService(private val client: AlphaVantageClient) {

    fun getDaily(symbol: String): String {
        return client.fetchTimeSeries("TIME_SERIES_DAILY", symbol)
    }

    fun getWeekly(symbol: String): String {
        return client.fetchTimeSeries("TIME_SERIES_WEEKLY", symbol)
    }

    fun getMonthly(symbol: String): String {
        return client.fetchTimeSeries("TIME_SERIES_MONTHLY", symbol)
    }
}