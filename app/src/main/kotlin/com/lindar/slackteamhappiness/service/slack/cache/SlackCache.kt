package com.lindar.slackteamhappiness.service.slack.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.lindar.slackteamhappiness.service.slack.service.SlackService
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class SlackCache(
    private val slackService: SlackService
) {
    private val cacheKey = "cache"

    private var slackCache: LoadingCache<String, SlackUserGroupData> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build { SlackUserGroupData(users = slackService.getUsers(), groupsByUserIds = slackService.getUserToGroupsMap()) }

    fun getSlackUserGroupData(): SlackUserGroupData {
        return slackCache[cacheKey]
    }
}