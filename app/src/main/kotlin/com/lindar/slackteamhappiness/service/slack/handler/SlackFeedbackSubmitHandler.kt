package com.lindar.slackteamhappiness.service.slack.handler

import com.lindar.slackteamhappiness.service.notion.NotionTeamHappinessService
import com.lindar.slackteamhappiness.service.slack.view.SlackViewIDs
import com.slack.api.bolt.App
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import org.springframework.stereotype.Service

@Service
class SlackFeedbackSubmitHandler (
    private val notionTeamHappinessService: NotionTeamHappinessService
) {

    fun handleSubmit(app: App) {
        app.blockAction(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) { req, ctx ->
            val selectedFeedback = getSelectedFeedbackFromBlock(req)
            val currentUserId = req.payload.user.id

            val currentUserInfo = getUserInfo(app, ctx.botToken, currentUserId)

            notionTeamHappinessService.storeFeedback(currentUserInfo.user.profile.email, selectedFeedback)

            val responseMessage = "Thank you for your response! $selectedFeedback"

            ctx.client().chatUpdate {
                it
                    .channel(req.payload.channel.id)
                    .ts(req.payload.message.ts)  // Use the timestamp of the original message to identify it
                    .blocks(
                        Blocks.asBlocks(
                            Blocks.section() { section ->
                                section
                                    .text(BlockCompositions.plainText(responseMessage, true))
                            }
                        )
                    )
            }

            ctx.ack()
        }
    }

    private fun getUserInfo(app: App, slackBotToken: String, userId: String): UsersInfoResponse {
        return app.slack.methods(slackBotToken).usersInfo { it.user(userId) }
    }

    fun getSelectedFeedbackFromBlock(req: BlockActionRequest): String {
        return req.payload.state.values[SlackViewIDs.USER_SELECTION_DROPDOWN_BLOCK_ID]?.get(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID)?.selectedOption?.text?.text ?: ""
    }
}