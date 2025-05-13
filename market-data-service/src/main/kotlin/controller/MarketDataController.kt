package com.coded.spring.controller

import com.coded.spring.service.MarketDataService
import com.coded.spring.service.StockData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Market Data", description = "Endpoints for retrieving stock market data")
class MarketDataController(private val service: MarketDataService) {

    @GetMapping("/daily")
    @Operation(
        summary = "Get daily stock data",
        description = "Returns the daily stock data for a given symbol",
        parameters = [Parameter(name = "symbol", description = "Stock ticker symbol", required = true)],
        responses = [ApiResponse(responseCode = "200", description = "Successful retrieval of daily data")]
    )
    fun daily(@RequestParam symbol: String): String {
        return service.getDaily(symbol)
    }

    @GetMapping("/weekly")
    @Operation(
        summary = "Get weekly stock data",
        description = "Returns the weekly stock data for a given symbol",
        parameters = [Parameter(name = "symbol", description = "Stock ticker symbol", required = true)],
        responses = [ApiResponse(responseCode = "200", description = "Successful retrieval of weekly data")]
    )
    fun weekly(@RequestParam symbol: String): String {
        return service.getWeekly(symbol)
    }

    @GetMapping("/monthly")
    @Operation(
        summary = "Get monthly stock data",
        description = "Returns the monthly stock data for a given symbol",
        parameters = [Parameter(name = "symbol", description = "Stock ticker symbol", required = true)],
        responses = [ApiResponse(responseCode = "200", description = "Successful retrieval of monthly data")]
    )
    fun monthly(@RequestParam symbol: String): String {
        return service.getMonthly(symbol)
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search for stock symbols",
        description = "Searches for stock symbols based on a keyword",
        parameters = [Parameter(name = "keyword", description = "Search keyword", required = true)],
        responses = [ApiResponse(responseCode = "200", description = "Search results returned")]
    )
    fun search(@RequestParam keyword: String): String {
        return service.searchSymbol(keyword)
    }

    @GetMapping("/daily/by-date")
    @Operation(
        summary = "Get daily stock data by date",
        description = "Returns the daily stock data for a specific symbol and date",
        parameters = [
            Parameter(name = "symbol", description = "Stock ticker symbol", required = true),
            Parameter(name = "date", description = "Date in YYYY-MM-DD format", required = true)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Stock data found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = StockData::class))]),
            ApiResponse(responseCode = "404", description = "Stock data not found")
        ]
    )
    fun dailyByDate(
        @RequestParam symbol: String,
        @RequestParam date: String
    ): ResponseEntity<StockData> {
        val result = service.getDailyDataByDate(symbol, date)
        return result?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/percentage-change")
    @Operation(
        summary = "Get percentage change in closing price",
        description = "Calculates the percentage change in closing price between two dates for a given stock symbol",
        parameters = [
            Parameter(name = "symbol", description = "Stock ticker symbol", required = true),
            Parameter(name = "fromDate", description = "Start date in YYYY-MM-DD format", required = true),
            Parameter(name = "toDate", description = "End date in YYYY-MM-DD format", required = true)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Successfully calculated percentage change"),
            ApiResponse(responseCode = "400", description = "Bad request due to invalid dates or missing data")
        ]
    )
    fun getPercentageChange(
        @RequestParam symbol: String,
        @RequestParam fromDate: String,
        @RequestParam toDate: String
    ): ResponseEntity<Any> {
        val change = service.getPercentageChange(symbol, fromDate, toDate)
        return if (change != null) {
            ResponseEntity.ok(mapOf("symbol" to symbol, "percentageChange" to "$change%"))
        } else {
            ResponseEntity.badRequest().body("Could not calculate percentage change. Check symbol or date range.")
        }
    }
}