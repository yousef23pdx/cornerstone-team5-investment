package com.coded.spring.repo

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Entity
@Table(name = "investments")
data class Investment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val assetSymbol: String,
    val quantity: Double,
    val buyPrice: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    val portfolio: Portfolio
)

@Repository
interface InvestmentRepository : JpaRepository<Investment, Long> {
    fun findAllByPortfolioId(portfolioId: Long): List<Investment>
    fun deleteByIdAndPortfolioId(investmentId: Long, portfolioId: Long): Long
}
