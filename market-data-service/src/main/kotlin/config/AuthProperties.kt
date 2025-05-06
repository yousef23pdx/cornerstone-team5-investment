package com.coded.spring.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth")
class AuthProperties {
    lateinit var baseUrl: String
}
