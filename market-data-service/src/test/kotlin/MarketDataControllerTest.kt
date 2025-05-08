package com.coded.spring.controller

import com.coded.spring.service.MarketDataService
import com.coded.spring.service.StockData
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.coded.spring.security.RemoteTokenFilter
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration

@WebMvcTest(
    controllers = [MarketDataController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [RemoteTokenFilter::class]
    )],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class, SecurityFilterAutoConfiguration::class]
)
class MarketDataControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var service: MarketDataService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET daily returns 200 with data`() {
        Mockito.`when`(service.getDaily("AAPL")).thenReturn("sample daily data")
        mockMvc.perform(get("/api/v1/stock/daily?symbol=AAPL"))
            .andExpect(status().isOk)
            .andExpect(content().string("sample daily data"))
    }

    @Test
    fun `GET weekly returns 200 with data`() {
        Mockito.`when`(service.getWeekly("AAPL")).thenReturn("sample weekly data")
        mockMvc.perform(get("/api/v1/stock/weekly?symbol=AAPL"))
            .andExpect(status().isOk)
            .andExpect(content().string("sample weekly data"))
    }

    @Test
    fun `GET monthly returns 200 with data`() {
        Mockito.`when`(service.getMonthly("AAPL")).thenReturn("sample monthly data")
        mockMvc.perform(get("/api/v1/stock/monthly?symbol=AAPL"))
            .andExpect(status().isOk)
            .andExpect(content().string("sample monthly data"))
    }

    @Test
    fun `GET search returns 200 with result`() {
        Mockito.`when`(service.searchSymbol("apple")).thenReturn("search result")
        mockMvc.perform(get("/api/v1/stock/search?keyword=apple"))
            .andExpect(status().isOk)
            .andExpect(content().string("search result"))
    }

    @Test
    fun `GET daily by date returns 200 with valid data`() {
        val stockData = StockData(
            symbol = "AAPL",
            date = "2024-01-01",
            open = 100.toBigDecimal(),
            high = 110.toBigDecimal(),
            low = 90.toBigDecimal(),
            close = 105.toBigDecimal(),
            volume = 1_000_000
        )

        Mockito.`when`(service.getDailyDataByDate("AAPL", "2024-01-01")).thenReturn(stockData)

        mockMvc.perform(get("/api/v1/stock/daily/by-date?symbol=AAPL&date=2024-01-01"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.date").value("2024-01-01"))
            .andExpect(jsonPath("$.close").value(105))
    }

    @Test
    fun `GET daily by date returns 404 when not found`() {
        Mockito.`when`(service.getDailyDataByDate("AAPL", "2024-01-02")).thenReturn(null)

        mockMvc.perform(get("/api/v1/stock/daily/by-date?symbol=AAPL&date=2024-01-02"))
            .andExpect(status().isNotFound)
    }
}