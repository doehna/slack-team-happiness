package com.lindar.slackteamhappiness.service

import com.lindar.slackteamhappiness.config.SlackProperties
import com.lindar.slackteamhappiness.service.slack.SlackMessagingService
import com.slack.api.methods.MethodsClient
import com.slack.api.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ScheduledMessagingTask(
    private val slackMessagingService: SlackMessagingService,
    private val methodsClient: MethodsClient,
    private val slackProperties: SlackProperties
) {
    @Scheduled(cron = "#{'\${schedule.cron}'}", zone = "CET")
    fun sendWeeklyHappinessSurvey() {
        if (!slackProperties.testUserId.isNullOrEmpty()) {
            logger.info { "TESTING MODE: Sending to a single user ${slackProperties.testUserId}..." }
            slackMessagingService.sendMessageToUser(slackProperties.testUserId, "Please share your weekly feedback!")
        } else {
            sendToAllUsers()
        }
    }

    private fun getAllUsers(): List<User> {
        val response = methodsClient.usersList { it }

        if (response.isOk) {
            return response.members?.filter { user ->
                !user.isDeleted && !user.isBot && user.profile != null && user.id != "USLACKBOT"
            }.orEmpty()
        } else {
            logger.error { "Error occurred during fetching the list of users from Slack" }
            return listOf()
        }
    }

    private fun sendToAllUsers() {
        val users = getAllUsers()

        users.forEach {
            slackMessagingService.sendMessageToUser(it.id, "Please share your weekly feedback!")
        }
    }
}
