package com.coded.spring.controller

import com.coded.spring.repo.Investment
import com.coded.spring.repo.Portfolio
import com.coded.spring.service.*
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
import java.math.BigDecimal

@WebMvcTest(
    controllers = [PortfolioController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [RemoteTokenFilter::class]
    )],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class, SecurityFilterAutoConfiguration::class]
)
class PortfolioControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var portfolioService: PortfolioService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `create portfolio returns portfolioId`() {
        val userId = 42L
        val portfolio = Portfolio(id = 101, userId = userId)

        Mockito.`when`(portfolioService.createPortfolio(userId)).thenReturn(portfolio)

        mockMvc.perform(post("/api/v1/portfolio/create")
            .requestAttr("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.portfolioId").value(101))
    }

    @Test
    fun `get summary returns portfolio summary`() {
        val userId = 42L
        val summary = PortfolioSummary(
            portfolioId = 101,
            totalInvested = BigDecimal("1000"),
            currentValue = BigDecimal("1200"),
            gainLoss = BigDecimal("200"),
            investments = emptyList()
        )

        Mockito.`when`(portfolioService.getSummary(userId)).thenReturn(summary)

        mockMvc.perform(get("/api/v1/portfolio/summary")
            .requestAttr("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.portfolioId").value(101))
            .andExpect(jsonPath("$.gainLoss").value(200))
    }

    @Test
    fun `get analytics returns gain percentage`() {
        val userId = 42L
        val analytics = PortfolioAnalytics(
            totalInvested = BigDecimal("1000"),
            currentValue = BigDecimal("1200"),
            gainLoss = BigDecimal("200"),
            gainLossPercent = BigDecimal("20.0")
        )

        Mockito.`when`(portfolioService.getAnalytics(userId)).thenReturn(analytics)

        mockMvc.perform(get("/api/v1/portfolio/analytics")
            .requestAttr("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.gainLossPercent").value(20.0))
    }

    @Test
    fun `secure endpoint returns greeting`() {
        val userId = 99L

        mockMvc.perform(get("/api/v1/portfolio/secure")
            .requestAttr("userId", userId))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello user 99 🎉"))
    }

    @Test
    fun `add investment returns investmentId`() {
        val userId = 42L
        val request = AddInvestmentRequest("AAPL", 10.0, BigDecimal("150.0"), "2024-01-01")
        val investment = Investment(
            id = 321,
            assetSymbol = "AAPL",
            quantity = 10.0,
            buyPrice = BigDecimal("150.0"),
            portfolio = Portfolio(id = 101, userId = userId)
        )

        Mockito.`when`(portfolioService.addInvestment(userId, request)).thenReturn(investment)

        mockMvc.perform(post("/api/v1/portfolio/investment")
            .requestAttr("userId", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.investmentId").value(321))
    }

    @Test
    fun `delete investment returns no content`() {
        val userId = 42L
        val investmentId = 321L

        mockMvc.perform(delete("/api/v1/portfolio/investment/$investmentId")
            .requestAttr("userId", userId))
            .andExpect(status().isNoContent)
    }
}