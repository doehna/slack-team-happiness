package com.lindar.slackteamhappiness

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackTeamHappinessApplication

fun main(args: Array<String>) {
    runApplication<SlackTeamHappinessApplication>(*args)
}
