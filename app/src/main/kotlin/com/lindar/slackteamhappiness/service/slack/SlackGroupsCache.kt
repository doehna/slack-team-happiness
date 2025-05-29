package com.lindar.slackteamhappiness.service.slack

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.lindar.slackteamhappiness.service.slack.service.SlackService
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SlackGroupsCache(
    private val slackService: SlackService
) {
    private val cacheKey = "groups"

    private var slackGroupsCache: LoadingCache<String, Map<String, List<String>>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build { slackService.getUserToGroupsMap() }

    fun getUserGroups(userId: String): List<String> {
        return slackGroupsCache[cacheKey].orEmpty()[userId].orEmpty()
    }
}