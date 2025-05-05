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
                             @Value("\${slack.teams.eng-team}") private val SLACK_ENG_TEAM_GROUP_ID: String?,
                             @Value("\${slack.teams.product-team}") private val SLACK_PRODUCT_TEAM_GROUP_ID: String?,
                             @Value("\${slack.teams.test-user}") private val SLACK_TEST_USER_ID: String?) {

    @Scheduled(cron = "#{'\${schedule.cron}'}", zone = "CET")
    fun sendWeeklyHappinessSurvey() {
        if (!SLACK_TEST_USER_ID.isNullOrEmpty()) {
            println("TESTING MODE: Sending to a single user ${SLACK_TEST_USER_ID}...")
            slackMessagingService.sendMessageToUser(SLACK_TEST_USER_ID, "Please share your weekly feedback!", "Test")
        } else {
            sendToTeam(SLACK_ENG_TEAM_GROUP_ID, "Engineering")
            sendToTeam(SLACK_PRODUCT_TEAM_GROUP_ID, "Product")
        }
    }

    private fun sendToTeam(teamGroupId: String?, teamName: String) {
        if (!teamGroupId.isNullOrEmpty()) {
            println("Sending weekly happiness survey to $teamName team members...")
            val userGroupResponse = getGroupUsers(teamGroupId)

            println("Users in $teamName group: ${userGroupResponse.users}")

            userGroupResponse.users.forEach { userId ->
                slackMessagingService.sendMessageToUser(userId, "Please share your weekly $teamName team feedback!", teamName)
            }
        }
    }

    private fun getGroupUsers(groupId: String): UsergroupsUsersListResponse {
        return methodsClient.usergroupsUsersList { it.usergroup(groupId) }
    }

    private fun getUserInfo(userId: String): UsersInfoResponse {
        return methodsClient.usersInfo { it.user(userId) }
    }
}
