package com.coded.spring.repo

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "assets")
data class Asset(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val symbol: String,
    val name: String,
    val type: String
)

@Repository
interface AssetRepository : JpaRepository<Asset, Long> {
    fun findBySymbolContainingIgnoreCase(symbol: String): List<Asset>
}
