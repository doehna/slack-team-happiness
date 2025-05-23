package com.lindar.slackteamhappiness

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@SpringBootApplication
@ConfigurationPropertiesScan
class SlackTeamHappinessApplication

fun main(args: Array<String>) {
    runApplication<SlackTeamHappinessApplication>(*args)
}
