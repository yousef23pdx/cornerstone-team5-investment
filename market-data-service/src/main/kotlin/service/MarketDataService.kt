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

    fun getDaily(symbol: String): String = client.fetchTimeSeries("TIME_SERIES_DAILY", symbol)

    fun getClosePriceOn(symbol: String, date: String): BigDecimal? {
        val json = JSONObject(getDaily(symbol))
        val ts   = json.optJSONObject("Time Series (Daily)") ?: return null
        val day  = ts.optJSONObject(date) ?: return null
        return day.getString("4. close").toBigDecimal()
    }

    fun getLatestClose(symbol: String): BigDecimal {
        val json = JSONObject(getDaily(symbol))
        val ts   = json.getJSONObject("Time Series (Daily)")

        val latestDate = ts.keys().asSequence().maxOrNull()
            ?: throw IllegalStateException("No data for $symbol")
        return ts.getJSONObject(latestDate).getString("4. close").toBigDecimal()
    }

    fun getWeekly(symbol: String): String {
        return client.fetchTimeSeries("TIME_SERIES_WEEKLY", symbol)
    }

    fun getMonthly(symbol: String): String {
        return client.fetchTimeSeries("TIME_SERIES_MONTHLY", symbol)
    }


    fun searchSymbol(keyword: String): String {
        return client.searchSymbol(keyword)
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
    fun getPercentageChange(symbol: String, fromDate: String, toDate: String): BigDecimal? {
        val json = JSONObject(getDaily(symbol))
        val ts = json.optJSONObject("Time Series (Daily)") ?: return null

        val fromClose = ts.optJSONObject(fromDate)?.optString("4. close")?.toBigDecimalOrNull()
        val toClose = ts.optJSONObject(toDate)?.optString("4. close")?.toBigDecimalOrNull()

        if (fromClose == null || toClose == null || fromClose == BigDecimal.ZERO) return null

        return ((toClose - fromClose) / fromClose * BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)
    }

}