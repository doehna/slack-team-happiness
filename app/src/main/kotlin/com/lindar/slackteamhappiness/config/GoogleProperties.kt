package com.lindar.slackteamhappiness.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "google")
@ConstructorBinding
data class GoogleProperties (
    val applicationName: String,
    val credentialsFilePath: String
)