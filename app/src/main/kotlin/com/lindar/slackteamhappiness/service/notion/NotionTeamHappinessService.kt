package com.lindar.slackteamhappiness.service.notion

import notion.api.v1.model.common.PropertyType
import notion.api.v1.model.common.RichTextType
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.model.users.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class NotionTeamHappinessService (
    @Value("\${notion.database.team-happiness-db-id}") private val NOTION_TEAM_HAPPINESS_DB_ID: String,
    private val notionAPI: NotionAPI,
) {

    fun storeFeedback(email: String, feedback: String) {
        val currentUserInfo = findUserByEmail(email)

        val properties = mapOf(
            "Employee" to PageProperty(people = listOf(currentUserInfo)),
            "Feedback" to PageProperty(type = PropertyType.Title, title = listOf( PageProperty.RichText(type = RichTextType.Text, text = PageProperty.RichText.Text(feedback)))),
        )
        notionAPI.insertIntoDatabase(NOTION_TEAM_HAPPINESS_DB_ID, properties)
    }

    private val users: List<User> by lazy { notionAPI.listAllUsers() }

    private fun findUserByEmail(email: String): User {
        return users.find { it.person?.email == email } ?: throw Exception("User with email $email not found")
    }
}