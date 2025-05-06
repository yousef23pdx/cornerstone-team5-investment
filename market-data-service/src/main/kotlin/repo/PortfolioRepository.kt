package com.coded.spring.repo

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "portfolios")
data class Portfolio(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,

    @OneToMany(mappedBy = "portfolio", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val investments: List<Investment> = emptyList()
)

@Repository
interface PortfolioRepository : JpaRepository<Portfolio, Long> {
    fun findByUserId(userId: Long): Portfolio?
}
