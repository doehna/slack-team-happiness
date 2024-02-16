package com.lindar.slackteamhappiness.service

import com.lindar.slackteamhappiness.service.slack.SlackMessagingService
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.response.usergroups.users.UsergroupsUsersListResponse
import com.slack.api.methods.response.users.UsersInfoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledMessagingTask(private val slackMessagingService: SlackMessagingService,
                             private val methodsClient: MethodsClient,
                             @Value("\${slack.teams.eng-team}") private val SLACK_TEAM_GROUP_ID: String) {

    @Scheduled(cron = "0 45 14 * * FRI", zone = "CET")
    fun sendWeeklyHappinessSurvey() {
        println("Sending weekly happiness survey to team members...")
        val userGroupResponse = getGroupUsers(SLACK_TEAM_GROUP_ID)

        println("Users in group: ${userGroupResponse.users}")

        userGroupResponse.users.forEach { userId ->
            slackMessagingService.sendMessageToUser(userId, "Please share your weekly feedback!")
        }
    }

    private fun getGroupUsers(groupId: String): UsergroupsUsersListResponse {
        return methodsClient.usergroupsUsersList { it.usergroup(groupId) }
    }

    private fun getUserInfo(userId: String): UsersInfoResponse {
        return methodsClient.usersInfo { it.user(userId) }
    }
}
