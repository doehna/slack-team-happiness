package com.lindar.slackteamhappiness.service.slack.handler

import com.lindar.slackteamhappiness.config.SlackProperties
import com.lindar.slackteamhappiness.config.Group
import com.lindar.slackteamhappiness.service.googlesheets.TeamHappinessGoogleSheetService
import com.lindar.slackteamhappiness.service.slack.cache.SlackCache
import com.lindar.slackteamhappiness.service.slack.view.SlackViewIDs
import com.slack.api.bolt.App
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.User
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SlackFeedbackSubmitHandler(
    private val teamHappinessGoogleSheetService: TeamHappinessGoogleSheetService,
    private val slackProperties: SlackProperties,
    private val slackCache: SlackCache
) {
    fun handleSubmit(app: App) {
        app.blockAction(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) { req, ctx ->
            val selectedFeedback = getSelectedFeedbackFromBlock(req)
            val currentUserId = req.payload.user.id
            val currentUser = getUserInfo(currentUserId)

            if (currentUser != null) {
                val messageDate = getOriginalMessageDateFromBlock(req)
                val groups = getAllUserGroups(currentUser)
                groups.forEach {
                    teamHappinessGoogleSheetService.appendValues(
                        selectedFeedback,
                        currentUser.realName,
                        messageDate,
                        it
                    )
                }

                val responseMessage = "Thank you for your response! $selectedFeedback"
                sendResponseMessage(responseMessage, ctx, req)
            } else {
                val responseMessage = "Thank you for your feedback!\n" +
                        "However, it won't be saved because you're currently not a member of any feedback group.\n" +
                        "If you believe this is a mistake, please let us know!"
                sendResponseMessage(responseMessage, ctx, req)
            }

            ctx.ack()
        }
    }

    private fun sendResponseMessage(
        responseMessage: String,
        ctx: ActionContext,
        req: BlockActionRequest
    ) {
        ctx.client().chatUpdate {
            it
                .channel(req.payload.channel.id)
                .ts(req.payload.message.ts)  // Use the timestamp of the original message to identify it
                .blocks(
                    Blocks.asBlocks(
                        Blocks.section { section ->
                            section
                                .text(BlockCompositions.plainText(responseMessage, true))
                        }
                    )
                )
        }
    }

    private fun getAllUserGroups(user: User): List<Group> {
        val userGroups = slackCache.getSlackUserGroupData()[user] ?: listOf()
        val otherGroup = slackProperties.groups.find { it.slackGroupId.isEmpty() }
        val otherList = if (otherGroup != null) listOf(otherGroup) else listOf()

        // if user isn't assigned to any of the groups specified in application.yml, return 'Other' group by default
        return userGroups.ifEmpty { otherList }
    }

    private fun getUserInfo(userId: String): User? {
        return slackCache.getSlackUserGroupData()
            .entries
            .find { it.key.id == userId }?.key
    }

    private fun getSelectedFeedbackFromBlock(req: BlockActionRequest): String {
        // Find the block ID that contains our action ID
        val blockId = req.payload.state.values.keys.find {
            req.payload.state.values[it]?.containsKey(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) == true
        } ?: SlackViewIDs.USER_SELECTION_DROPDOWN_BLOCK_ID

        return req.payload.state.values[blockId]?.get(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID)?.selectedOption?.text?.text ?: ""
    }

    private fun getOriginalMessageDateFromBlock(req: BlockActionRequest): String {
        val payload = req.payload

        val messageTs = payload.message?.ts ?: return getNowTimeString() // Handle null safety if message or ts is null

        val timestamp = messageTs.toDouble().toLong()
        val instant = Instant.ofEpochSecond(timestamp)

        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mma z", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC)

        return formatter.format(instant)
    }

    private fun getNowTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mma z", Locale.ENGLISH)
        val zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).format(formatter)
        return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}
