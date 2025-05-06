package com.coded.spring.service

import com.coded.spring.client.AlphaVantageClient
import com.hazelcast.shaded.org.json.JSONObject
import org.springframework.stereotype.Service
import java.math.BigDecimal

data class StockData(
    val symbol: String,
    val date: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)

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

    fun getDailyDataByDate(symbol: String, date: String): StockData? {
        val response = client.fetchTimeSeries("TIME_SERIES_DAILY", symbol)
        val json = JSONObject(response)
        val timeSeries = json.optJSONObject("Time Series (Daily)") ?: return null
        val dayData = timeSeries.optJSONObject(date) ?: return null

        return StockData(
            symbol = symbol,
            date = date,
            open = dayData.getString("1. open").toBigDecimal(),
            high = dayData.getString("2. high").toBigDecimal(),
            low = dayData.getString("3. low").toBigDecimal(),
            close = dayData.getString("4. close").toBigDecimal(),
            volume = dayData.getString("5. volume").toLong()
        )
    }
}