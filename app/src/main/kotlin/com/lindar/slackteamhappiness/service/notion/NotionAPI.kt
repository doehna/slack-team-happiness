package com.lindar.slackteamhappiness.service.notion

import notion.api.v1.NotionClient
import notion.api.v1.model.pages.Page
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.model.users.User
import notion.api.v1.request.pages.CreatePageRequest
import notion.api.v1.request.users.ListUsersRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class NotionAPI (
    @Value("\${notion.api.token}") private val NOTION_SECRET_API_TOKEN: String,
) {

    fun fetchDatatableResultsFromNotion(databaseId: String): List<Page> {
        return NotionClient(token = NOTION_SECRET_API_TOKEN).use { client ->
            var queryResults = client.queryDatabase(databaseId)
            val pages = ArrayList(queryResults.results)
            while (queryResults.hasMore) {
                queryResults = client.queryDatabase(databaseId, startCursor = queryResults.nextCursor)
                pages.addAll(queryResults.results)
            }
            return@use pages
        }
    }

    fun insertIntoDatabase(databaseId: String, params: Map<String, PageProperty>): Page {
        NotionClient(token = NOTION_SECRET_API_TOKEN).use { client ->
            return client.createPage(CreatePageRequest(
                    parent = PageParent.database(databaseId),
                    properties = params
                )
            )
        }
    }

    fun listAllUsers(): List<User> {
        NotionClient(token = NOTION_SECRET_API_TOKEN).use { client ->
            var startCursor: String? = null
            val pageSize = 100
            val userList = ArrayList<User>()
            do {
                val response = client.listUsers(ListUsersRequest(startCursor, pageSize))
                userList.addAll(response.results)
                startCursor = response.nextCursor ?: break
            } while (response.hasMore)
            return userList
        }
    }
}