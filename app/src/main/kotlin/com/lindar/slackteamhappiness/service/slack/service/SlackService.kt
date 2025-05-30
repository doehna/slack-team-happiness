package com.lindar.slackteamhappiness.service.slack.service

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.SlackApiException
import com.slack.api.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SlackService(
    private val methodsClient: MethodsClient
) {
    fun getUserToGroupsMap(): Map<String, List<String>> {
        // make a single call to Slack to get all groups and group them by users
        try {
            val slackUserGroupMap = methodsClient
                .usergroupsList { req -> req.includeDisabled(false).includeUsers(true) }
                .usergroups
                .associate { it.id to it.users }

            return invertGroupToUserMap(slackUserGroupMap)
        } catch (e: SlackApiException) {
            if (e.response.code == 429) {
                val retryAfter = e.response.headers["Retry-After"]?.toLongOrNull()
                logger.error { "⚠️ Rate limit exceeded! Retry after $retryAfter seconds." }
            } else {
                logger.error { "❌ Slack API error: ${e.response.code} - ${e.response.body}" }
            }

            return mapOf()
        }
    }

    fun getUsers(): List<User> {
        try {
            return methodsClient.usersList { it }.members
                ?.filter { user -> !user.isDeleted && !user.isBot && user.profile != null && user.id != "USLACKBOT" }
                .orEmpty()
        } catch (e: SlackApiException) {
            if (e.response.code == 429) {
                val retryAfter = e.response.headers["Retry-After"]?.toLongOrNull()
                logger.error { "⚠️ Rate limit exceeded! Retry after $retryAfter seconds." }
            } else {
                logger.error { "❌ Slack API error: ${e.response.code} - ${e.response.body}" }
            }

            return listOf()
        }
    }

    private fun invertGroupToUserMap(groupToUserMap: Map<String, List<String>>): Map<String, List<String>> {
        return groupToUserMap
            .flatMap { (groupId, userIds) -> userIds.map { userId -> userId to groupId } }
            .groupBy({ it.first }, { it.second })
    }
}