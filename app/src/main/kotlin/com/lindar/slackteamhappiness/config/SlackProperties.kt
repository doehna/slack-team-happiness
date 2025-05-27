package com.lindar.slackteamhappiness.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "slack")
@ConstructorBinding
data class SlackProperties (
    val testUserId: String?,
    val teams: List<Team>
)

data class Team (
    val name: String,
    val slackGroupId: String,
    val googleSheetId: String,
    val googleSheetName: String
)


