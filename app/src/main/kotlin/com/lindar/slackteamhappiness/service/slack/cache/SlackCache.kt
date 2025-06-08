package com.lindar.slackteamhappiness.service.slack.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.lindar.slackteamhappiness.config.Group
import com.lindar.slackteamhappiness.service.slack.service.SlackService
import com.slack.api.model.User
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SlackCache(
    private val slackService: SlackService
) {
    private val cacheKey = "cache"
    private var slackCache: LoadingCache<String, Map<User, List<Group>>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build { slackService.getUserToGroupsMap() }

    fun getSlackUserGroupData(): Map<User, List<Group>> {
        return slackCache[cacheKey]
    }
}