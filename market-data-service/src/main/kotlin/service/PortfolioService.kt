package com.coded.spring.service

import com.coded.spring.repo.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal


data class CreatePortfolioRequest(val userId: Long)

data class AddInvestmentRequest(
    val assetSymbol: String,
    val quantity: Double,
    val buyPrice: BigDecimal? = null,
    val investmentDate: String? = null
)


data class InvestmentDTO(
    val id: Long,
    val assetSymbol: String,
    val quantity: Double,
    val buyPrice: BigDecimal,
    val currentPrice: BigDecimal,
    val currentValue: BigDecimal,
    val gainLoss: BigDecimal
)

data class PortfolioSummary(
    val portfolioId: Long,
    val totalInvested: BigDecimal,
    val currentValue: BigDecimal,
    val gainLoss: BigDecimal,
    val investments: List<InvestmentDTO>
)

data class PortfolioAnalytics(
    val totalInvested: BigDecimal,
    val currentValue: BigDecimal,
    val gainLoss: BigDecimal,
    val gainLossPercent: BigDecimal
)


@Service
class PortfolioService(
    private val portfolioRepo: PortfolioRepository,
    private val investmentRepo: InvestmentRepository,
    private val assetRepo: AssetRepository,
    private val marketDataService: MarketDataService
) {

    @Transactional
    fun createPortfolio(userId: Long): Portfolio {
        return portfolioRepo.findByUserId(userId)
            ?: portfolioRepo.save(Portfolio(userId = userId))
    }

    @Transactional
    fun addInvestment(userId: Long, req: AddInvestmentRequest): Investment {
        val portfolio = portfolioRepo.findByUserId(userId)
            ?: throw IllegalStateException("Portfolio not found")

        if (assetRepo.findBySymbolContainingIgnoreCase(req.assetSymbol).isEmpty()) {
            assetRepo.save(Asset(symbol = req.assetSymbol, name = req.assetSymbol, type = "UNKNOWN"))
        }

        val buyPrice = when {
            req.buyPrice != null -> req.buyPrice

            !req.investmentDate.isNullOrBlank() -> {
                marketDataService.getClosePriceOn(req.assetSymbol, req.investmentDate)
                    ?: throw IllegalArgumentException("No price for ${req.assetSymbol} on ${req.investmentDate}")
            }

            else -> marketDataService.getLatestClose(req.assetSymbol)
        }

        val investment = Investment(
            assetSymbol = req.assetSymbol.uppercase(),
            quantity    = req.quantity,
            buyPrice    = buyPrice,
            portfolio   = portfolio
        )
        return investmentRepo.save(investment)
    }

    @Transactional
    fun deleteInvestment(userId: Long, investmentId: Long) {
        val portfolio = portfolioRepo.findByUserId(userId)
            ?: throw IllegalStateException("Portfolio not found")

        val deleted = investmentRepo.deleteByIdAndPortfolioId(investmentId, portfolio.id)
        if (deleted == 0L) throw IllegalArgumentException("Investment not found or not owned by user")
    }


    fun getSummary(userId: Long): PortfolioSummary {
        val portfolio = portfolioRepo.findByUserId(userId)
            ?: throw IllegalStateException("Portfolio not found")

        val investments = investmentRepo.findAllByPortfolioId(portfolio.id)

        val dtoList = investments.map { inv ->
            val currentPrice = latestClosePrice(inv.assetSymbol)
            val currentValue = currentPrice * inv.quantity.toBigDecimal()
            val gainLoss = currentValue - (inv.buyPrice * inv.quantity.toBigDecimal())
            InvestmentDTO(
                id = inv.id,
                assetSymbol = inv.assetSymbol,
                quantity = inv.quantity,
                buyPrice = inv.buyPrice,
                currentPrice = currentPrice,
                currentValue = currentValue,
                gainLoss = gainLoss
            )
        }

        val totalInvested = dtoList.fold(BigDecimal.ZERO) { acc, d ->
            acc + (d.buyPrice * d.quantity.toBigDecimal())
        }
        val currentValue = dtoList.fold(BigDecimal.ZERO) { acc, d -> acc + d.currentValue }
        val gainLoss = currentValue - totalInvested

        return PortfolioSummary(
            portfolioId = portfolio.id,
            totalInvested = totalInvested,
            currentValue = currentValue,
            gainLoss = gainLoss,
            investments = dtoList
        )
    }


    fun getAnalytics(userId: Long): PortfolioAnalytics {
        val summary = getSummary(userId)
        val gainLossPercent = if (summary.totalInvested.compareTo(BigDecimal.ZERO) == 0)
            BigDecimal.ZERO
        else
            summary.gainLoss * BigDecimal(100) / summary.totalInvested

        return PortfolioAnalytics(
            totalInvested = summary.totalInvested,
            currentValue = summary.currentValue,
            gainLoss = summary.gainLoss,
            gainLossPercent = gainLossPercent
        )
    }

    private fun latestClosePrice(symbol: String): BigDecimal =
        marketDataService.getLatestClose(symbol)
}
