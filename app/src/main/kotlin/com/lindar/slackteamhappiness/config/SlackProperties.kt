package com.lindar.slackteamhappiness.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "slack")
@ConstructorBinding
data class SlackProperties (
    val testUserId: String?,
    val groups: List<Group>
)

data class Group (
    val name: String,
    val slackGroupId: String,
    val googleSheetName: String
)