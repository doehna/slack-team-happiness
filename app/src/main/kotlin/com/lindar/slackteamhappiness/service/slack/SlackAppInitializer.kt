package com.lindar.slackteamhappiness.service.slack

import com.lindar.slackteamhappiness.service.slack.handler.SlackFeedbackSubmitHandler
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class SlackAppInitializer @Autowired constructor(
    val app: App,
    val slackFeedbackSubmitHandler: SlackFeedbackSubmitHandler,
) {

    @PostConstruct
    fun init() {
        slackFeedbackSubmitHandler.handleSubmit(app)
    }

    @EventListener(ContextRefreshedEvent::class)
    fun startSocketModeApp() {
        SocketModeApp(app).start()
    }
}
