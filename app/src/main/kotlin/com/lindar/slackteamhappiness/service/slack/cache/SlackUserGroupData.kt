package com.lindar.slackteamhappiness.service.slack.cache

import com.slack.api.model.User


data class SlackUserGroupData (
    val users: List<User>,
    val groupsByUsers: Map<String, List<String>>
)