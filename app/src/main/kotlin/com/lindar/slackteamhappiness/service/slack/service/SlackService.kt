package com.lindar.slackteamhappiness.service.slack.service

import com.lindar.slackteamhappiness.config.Group
import com.lindar.slackteamhappiness.config.SlackProperties
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.SlackApiException
import com.slack.api.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SlackService(
    private val methodsClient: MethodsClient,
    private val slackProperties: SlackProperties
) {
    fun getUserToGroupsMap(): Map<User, List<Group>> {
        val userIdsToGroupMap = getUserIdsToGroupsMap()
        val users = getAllUsers()
        val userIdToUser = users.associateBy { it.id }

        return userIdsToGroupMap.mapNotNull { (userId, groups) ->
            userIdToUser[userId]?.let { user -> user to groups }
        }.toMap()
    }

    private fun getUserIdsToGroupsMap(): Map<String, List<Group>> {
        // make a single call to Slack to get all groups and group them by users
        try {
            val slackUserGroupMap = methodsClient
                .usergroupsList { req -> req.includeDisabled(false).includeUsers(true) }
                .usergroups
                .associate { it.id to it.users }

            return getUserIdToConfigGroupMap(slackUserGroupMap)
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

    private fun invertGroupIdToUserIdMap(groupIdToUserIdMap: Map<String, List<String>>): Map<String, List<String>> {
        return groupIdToUserIdMap
            .flatMap { (groupId, userIds) -> userIds.map { userId -> userId to groupId } }
            .groupBy({ it.first }, { it.second })
    }

    private fun getUserIdToConfigGroupMap(groupIdToUserIdMap: Map<String, List<String>>): Map<String, List<Group>> {
        val userIdToGroupIdMap = invertGroupIdToUserIdMap(groupIdToUserIdMap)

        return userIdToGroupIdMap
            .mapValues { (_, groupIds) ->
                slackProperties.groups.filter { it.slackGroupId in groupIds }
            }
            .filterValues { it.isNotEmpty() }
    }

    private fun getAllUsers(): List<User> {
        try {
            val response = methodsClient.usersList { it }

            if (response.isOk) {
                return response.members
            } else {
                logger.error { "Slack API error in usersList: error='${response.error}', needed='${response.needed}', provided='${response.provided}'" }
                return listOf()
            }
        } catch (e: Exception) {
            logger.error { "Unexpected error while calling Slack API: ${e.message}" }
            return listOf()
        }
    }
}








