package com.lindar.slackteamhappiness.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class AppProperties(
    val google: Google
)

data class Google (
    val credentialsFilePath: String,
    val applicationName: String,
    val spreadsheetId: String,
    val sheetName: String
)
