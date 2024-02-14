package com.lindar.slackteamhappiness.service.slack

import com.lindar.slackteamhappiness.service.slack.view.SlackViewIDs
import com.slack.api.methods.MethodsClient
import com.slack.api.model.block.Blocks.*
import com.slack.api.model.block.composition.BlockCompositions.*
import com.slack.api.model.block.element.BlockElements.staticSelect
import org.springframework.stereotype.Service

@Service
class SlackMessagingService(private val methodsClient: MethodsClient) {

    fun sendMessageToUser(userId: String, message: String) {
        try {
            methodsClient.chatPostMessage { req ->
                req.channel(userId)
                    .text(message)
                    .blocks(asBlocks(
                        section() { section ->
                            section
                                .text(markdownText("*How have you been feeling at work this past week?*"))
                        },
                        actions { actions ->
                            actions
                                .blockId(SlackViewIDs.USER_SELECTION_DROPDOWN_BLOCK_ID)
                                .elements(
                                    listOf(
                                        staticSelect {
                                            it.placeholder(plainText("Select an option"))
                                                .actionId(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID)
                                                .options(listOf(
                                                    option(plainText(":smile: Happy"), "happy"),
                                                    option(plainText(":neutral_face: Neutral"), "neutral"),
                                                    option(plainText(":disappointed: Unhappy"), "unhappy")
                                                ))
                                        }
                                    )
                            )
                        }
                    ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
