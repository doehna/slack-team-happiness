package com.lindar.slackteamhappiness.service.slack.handler

import com.lindar.slackteamhappiness.config.SlackProperties
import com.lindar.slackteamhappiness.config.Team
import com.lindar.slackteamhappiness.service.googlesheets.TeamHappinessGoogleSheetService
import com.lindar.slackteamhappiness.service.slack.view.SlackViewIDs
import com.slack.api.bolt.App
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.methods.MethodsClient
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
    private val teamHappinessGoogleSheetService: TeamHappinessGoogleSheetService,
    private val slackProperties: SlackProperties,
    private val methodsClient: MethodsClient
) {
    fun handleSubmit(app: App) {
        app.blockAction(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) { req, ctx ->
            val selectedFeedback = getSelectedFeedbackFromBlock(req)
            val currentUserId = req.payload.user.id
            val currentUserInfo = getUserInfo(app, ctx.botToken, currentUserId)
            val messageDate = getOriginalMessageDateFromBlock(req)
            val teams = getAllUserTeams(currentUserId)

            teams.forEach {
                teamHappinessGoogleSheetService.appendValues(
                    selectedFeedback,
                    currentUserInfo.user.profile.realNameNormalized,
                    messageDate,
                    it
                )
            }

            sendResponseMessage(selectedFeedback, ctx, req)

            ctx.ack()
        }
    }

    private fun sendResponseMessage(
        selectedFeedback: String,
        ctx: ActionContext,
        req: BlockActionRequest
    ) {
        val responseMessage = "Thank you for your response! $selectedFeedback"

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

    private fun getAllUserTeams(userId: String): List<Team> {
        val allTeams = slackProperties.teams

        val userTeams = allTeams.filter { getGroupUsers(it.slackGroupId).contains(userId) }
        val otherTeam = allTeams.find { it.slackGroupId.isEmpty() }!!

        // if user isn't assigned to any group, return 'Other' group by default
        return userTeams.ifEmpty { listOf(otherTeam) }
    }

    private fun getGroupUsers(groupId: String): List<String> {
        return if (groupId.isEmpty())
            listOf()
        else
            methodsClient.usergroupsUsersList { it.usergroup(groupId) }.users
    }

    private fun getUserInfo(app: App, slackBotToken: String, userId: String): UsersInfoResponse {
        return app.slack.methods(slackBotToken).usersInfo { it.user(userId) }
    }

    fun getSelectedFeedbackFromBlock(req: BlockActionRequest): String {
        // Find the block ID that contains our action ID
        val blockId = req.payload.state.values.keys.find { 
            req.payload.state.values[it]?.containsKey(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID) == true 
        } ?: SlackViewIDs.USER_SELECTION_DROPDOWN_BLOCK_ID

        return req.payload.state.values[blockId]?.get(SlackViewIDs.USER_SELECTION_DROPDOWN_ACTION_ID)?.selectedOption?.text?.text ?: ""
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
