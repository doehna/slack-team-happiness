package com.lindar.slackteamhappiness.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
@EnableScheduling
class SchedulingConfig : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setTaskScheduler(createThreadPoolTaskScheduler())
    }

    private fun createThreadPoolTaskScheduler(): ThreadPoolTaskScheduler {
        val newTaskScheduler = ThreadPoolTaskScheduler()
        newTaskScheduler.poolSize = 8
        newTaskScheduler.initialize()
        return newTaskScheduler
    }
}