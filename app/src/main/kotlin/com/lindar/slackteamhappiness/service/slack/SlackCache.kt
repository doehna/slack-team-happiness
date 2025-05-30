package com.lindar.slackteamhappiness.service.slack

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.lindar.slackteamhappiness.service.slack.service.SlackService
import com.slack.api.model.User
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SlackCache(
    private val slackService: SlackService
) {
    private val groupsCacheKey = "groups"
    private val usersCacheKey = "users"

    private var slackGroupsCache: LoadingCache<String, Map<String, List<String>>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build { slackService.getUserToGroupsMap() }

    private var slackUsersCache: LoadingCache<String, List<User>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build { slackService.getUsers() }

    fun getUserGroups(userId: String): List<String> {
        return slackGroupsCache[groupsCacheKey][userId].orEmpty()
    }

    fun getUsers(): List<User> {
        return slackUsersCache[usersCacheKey]
    }
}