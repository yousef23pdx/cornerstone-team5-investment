package com.coded.spring

import com.hazelcast.config.Config
import com.hazelcast.config.JoinConfig
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class HazelcastCacheConfig {

    @Bean
    fun hazelcastInstance(): HazelcastInstance {
        val config = Config("market-cache")
        config.networkConfig = NetworkConfig().apply {
            join = JoinConfig().apply {
                multicastConfig.isEnabled = false
                tcpIpConfig.isEnabled = true
                tcpIpConfig.addMember("127.0.0.1")
            }
        }

        config.getMapConfig("marketData").timeToLiveSeconds = 600 // 10 minutes

        return Hazelcast.newHazelcastInstance(config)
    }

    @Bean
    fun cacheManager(hazelcastInstance: HazelcastInstance): CacheManager {
        return HazelcastCacheManager(hazelcastInstance)
    }
}