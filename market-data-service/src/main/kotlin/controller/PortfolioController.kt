package com.coded.spring.controller

import com.coded.spring.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @Operation(
        summary = "Test secured endpoint",
        description = "Returns a greeting message using the authenticated user's ID",
        parameters = [Parameter(name = "userId", hidden = true)],
        responses = [ApiResponse(responseCode = "200", description = "Greeting message returned")]
    )
    fun secureEndpoint(@RequestAttribute("userId") userId: Long): String {
        return "Hello user $userId !"
    }

    @PostMapping("/create")
    @Operation(
        summary = "Create an empty portfolio for the authenticated user",
        parameters = [Parameter(name = "userId", hidden = true)],
        responses = [ApiResponse(responseCode = "200", description = "Portfolio created successfully")]
    )
    fun create(@RequestAttribute("userId") userId: Long): ResponseEntity<Any> {
        val p = portfolioService.createPortfolio(userId)
        return ResponseEntity.ok(mapOf("portfolioId" to p.id))
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get portfolio summary",
        description = "Returns the valuation and P/L of the user's portfolio",
        parameters = [Parameter(name = "userId", hidden = true)],
        responses = [ApiResponse(
            responseCode = "200",
            description = "Portfolio summary returned",
            content = [Content(schema = Schema(implementation = PortfolioSummary::class))]
        )]
    )
    fun summary(@RequestAttribute("userId") userId: Long): ResponseEntity<PortfolioSummary> =
        ResponseEntity.ok(portfolioService.getSummary(userId))

    @GetMapping("/analytics")
    @Operation(
        summary = "Get portfolio analytics",
        description = "Returns aggregated portfolio metrics such as gains and return percentage",
        parameters = [Parameter(name = "userId", hidden = true)],
        responses = [ApiResponse(
            responseCode = "200",
            description = "Analytics returned",
            content = [Content(schema = Schema(implementation = PortfolioAnalytics::class))]
        )]
    )
    fun analytics(@RequestAttribute("userId") userId: Long): ResponseEntity<PortfolioAnalytics> =
        ResponseEntity.ok(portfolioService.getAnalytics(userId))

    @PostMapping("/investment")
    @Operation(
        summary = "Add investment",
        description = "Adds a new investment to the authenticated user's portfolio",
        parameters = [Parameter(name = "userId", hidden = true)],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Investment details",
            required = true,
            content = [Content(schema = Schema(implementation = AddInvestmentRequest::class))]
        ),
        responses = [ApiResponse(responseCode = "200", description = "Investment added successfully")]
    )
    fun addInvestment(
        @RequestAttribute("userId") userId: Long,
        @RequestBody req: AddInvestmentRequest
    ): ResponseEntity<Any> {
        val inv = portfolioService.addInvestment(userId, req)
        return ResponseEntity.ok(mapOf("investmentId" to inv.id))
    }

    @DeleteMapping("/investment/{id}")
    @Operation(
        summary = "Delete investment",
        description = "Deletes a specific investment from the portfolio",
        parameters = [
            Parameter(name = "userId", hidden = true),
            Parameter(name = "id", description = "Investment ID", required = true)
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "Investment deleted successfully"),
            ApiResponse(responseCode = "404", description = "Investment not found")
        ]
    )
    fun deleteInvestment(
        @RequestAttribute("userId") userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        portfolioService.deleteInvestment(userId, id)
        return ResponseEntity.noContent().build()
    }
}