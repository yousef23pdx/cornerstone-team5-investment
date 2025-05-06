package com.coded.spring.controller

import com.coded.spring.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "PORTFOLIO", description = "Manage portfolios and investments")
class PortfolioController(
    private val portfolioService: PortfolioService
) {

    @GetMapping("/secure")
    fun secureEndpoint(@RequestAttribute("userId") userId: Long): String {
        return "Hello user $userId 🎉"
    }

    @PostMapping("/create")
    @Operation(summary = "Create an empty portfolio for the authenticated user")
    fun create(@RequestAttribute("userId") userId: Long): ResponseEntity<Any> {
        val p = portfolioService.createPortfolio(userId)
        return ResponseEntity.ok(mapOf("portfolioId" to p.id))
    }

    @GetMapping("/summary")
    @Operation(summary = "Get a full summary (valuation & P/L) of your portfolio")
    fun summary(@RequestAttribute("userId") userId: Long): ResponseEntity<PortfolioSummary> =
        ResponseEntity.ok(portfolioService.getSummary(userId))

    @GetMapping("/analytics")
    @Operation(summary = "Aggregated analytics (gains, % return, etc.)")
    fun analytics(@RequestAttribute("userId") userId: Long): ResponseEntity<PortfolioAnalytics> =
        ResponseEntity.ok(portfolioService.getAnalytics(userId))

    @PostMapping("/investment")
    @Operation(summary = "Add an investment to your portfolio")
    fun addInvestment(
        @RequestAttribute("userId") userId: Long,
        @RequestBody req: AddInvestmentRequest
    ): ResponseEntity<Any> {
        val inv = portfolioService.addInvestment(userId, req)
        return ResponseEntity.ok(mapOf("investmentId" to inv.id))
    }

    @DeleteMapping("/investment/{id}")
    @Operation(summary = "Delete an investment you own")
    fun deleteInvestment(
        @RequestAttribute("userId") userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        portfolioService.deleteInvestment(userId, id)
        return ResponseEntity.noContent().build()
    }
}
