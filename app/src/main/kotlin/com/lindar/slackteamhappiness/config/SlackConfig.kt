package com.lindar.slackteamhappiness.config

import com.slack.api.Slack
import com.slack.api.bolt.App
import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
class SlackConfig {

    @Value("\${slack.bot.token}")
    private lateinit var botToken: String

    @Bean
    fun slackMethodClient(): MethodsClient = Slack.getInstance().methods(botToken)

    @Bean
    fun initSlackApp(): App {
        return App()
    }
}