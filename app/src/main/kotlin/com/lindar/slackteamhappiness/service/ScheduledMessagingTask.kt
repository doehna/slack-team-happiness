package com.lindar.slackteamhappiness.service

import com.lindar.slackteamhappiness.config.SlackProperties
import com.lindar.slackteamhappiness.service.slack.SlackCache
import com.lindar.slackteamhappiness.service.slack.service.SlackMessagingService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ScheduledMessagingTask(
    private val slackMessagingService: SlackMessagingService,
    private val slackCache: SlackCache,
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

    private fun sendToAllUsers() {
        val users = slackCache.getUsers()

        users.forEach {
            slackMessagingService.sendMessageToUser(it.id, "Please share your weekly feedback!")
        }
    }
}
