package com.coded.spring.controller

import com.coded.spring.service.MarketDataService
import com.coded.spring.service.StockData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stock")
class MarketDataController(private val service: MarketDataService) {

    @GetMapping("/daily")
    fun daily(@RequestParam symbol: String): String {
        return service.getDaily(symbol)
    }
    @GetMapping("/weekly")
    fun weekly(@RequestParam symbol: String): String {
        return service.getWeekly(symbol)
    }
    @GetMapping("/monthly")
    fun monthly(@RequestParam symbol: String): String {
        return service.getMonthly(symbol)
    }

    @GetMapping("/search")
    fun search(@RequestParam keyword: String): String {
        return service.searchSymbol(keyword)
    }
    @GetMapping("/daily/by-date")
    fun dailyByDate(
        @RequestParam symbol: String,
        @RequestParam date: String
    ): ResponseEntity<StockData> {
        val result = service.getDailyDataByDate(symbol, date)
        return result?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    }
}