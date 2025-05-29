package com.lindar.slackteamhappiness.service.slack.service

import com.slack.api.methods.MethodsClient
import org.springframework.stereotype.Service

@Service
class SlackService(
    private val methodsClient: MethodsClient
) {
    fun getUserToGroupsMap(): Map<String, List<String>> {
        // make a single call to Slack to get all groups and group them by users
        val slackUserGroupMap = methodsClient
            .usergroupsList { req -> req.includeDisabled(false).includeUsers(true) }
            .usergroups
            .associate { it.id to it.users }

        return invertGroupToUserMap(slackUserGroupMap)
    }

    private fun invertGroupToUserMap(groupToUserMap: Map<String, List<String>>): Map<String, List<String>> {
        val userToGroups: MutableMap<String, MutableList<String>> = mutableMapOf()

        groupToUserMap.forEach { (groupId, userIds) ->
            userIds.forEach { userId ->
                userToGroups.getOrPut(userId) { mutableListOf() }.add(groupId)
            }
        }
        return userToGroups
    }
}