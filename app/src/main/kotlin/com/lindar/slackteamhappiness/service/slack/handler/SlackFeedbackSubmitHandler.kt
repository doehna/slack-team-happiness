package com.lindar.slackteamhappiness.service.slack.handler

import com.lindar.slackteamhappiness.service.googlesheets.TeamHappinessGoogleSheetService
import com.lindar.slackteamhappiness.service.slack.view.SlackViewIDs
import com.slack.api.bolt.App
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SlackFeedbackSubmitHandler (
    private val teamHappinessGoogleSheetService: TeamHappinessGoogleSheetService
) {

    fun handleSubmit(app: App) {
        app.blockAction(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) { req, ctx ->
            val selectedFeedback = getSelectedFeedbackFromBlock(req)
            val currentUserId = req.payload.user.id
            val currentUserInfo = getUserInfo(app, ctx.botToken, currentUserId)
            val messageDate = getOriginalMessageDateFromBlock(req)

            teamHappinessGoogleSheetService.appendValues(
                selectedFeedback,
                currentUserInfo.user.profile.realNameNormalized,
                messageDate
            )

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

    fun getOriginalMessageDateFromBlock(req: BlockActionRequest): String {
        val payload = req.payload

        val messageTs = payload.message?.ts ?: return getNowTimeString() // Handle null safety if message or ts is null

        val timestamp = messageTs.toDouble().toLong()
        val instant = Instant.ofEpochSecond(timestamp)

        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mma z", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC)

        return formatter.format(instant)
    }

    fun getNowTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mma z", Locale.ENGLISH)
        val zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).format(formatter)
        return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}