package com.lindar.slackteamhappiness.service.googlesheets

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.util.*

@Service
class TeamHappinessGoogleSheetService(
    @Value("\${google.sheets.application-name}") private val applicationName: String,
    @Value("\${google.sheets.engineering.spreadsheet-id}") private val engineeringSpreadsheetId: String,
    @Value("\${google.sheets.engineering.sheet-name}") private val engineeringSheetName: String,
    @Value("\${google.sheets.product.spreadsheet-id}") private val productSpreadsheetId: String,
    @Value("\${google.sheets.product.sheet-name}") private val productSheetName: String
) {
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private val CREDENTIALS_FILE_PATH = "/conf/credentials.json" // Adjust the path as necessary

    fun appendValues(selectedFeedback: String, respondentName: String, messageDate: String, team: String = "Engineering") {
        try {
            val (spreadsheetId, sheetName) = when (team.lowercase()) {
                "product" -> Pair(productSpreadsheetId, productSheetName)
                else -> Pair(engineeringSpreadsheetId, engineeringSheetName)
            }

            val values = listOf(
                listOf<Any>(
                    selectedFeedback, respondentName, messageDate, team
                )
            )

            val sheetsService = getSheetsService()

            val body = ValueRange().setValues(values)
            val result = sheetsService.spreadsheets().values()
                .append(spreadsheetId, sheetName, body)
                .setValueInputOption("RAW")
                .execute()

            println("${result.updates.updatedCells} cells appended to $team sheet.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun getSheetsService(): Sheets {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials()
        val requestInitializer = HttpCredentialsAdapter(credentials)
        return Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
            .setApplicationName(applicationName)
            .build()
    }

    @Throws(Exception::class)
    private fun getCredentials(): GoogleCredentials? {
        FileInputStream(System.getProperty("user.home") + CREDENTIALS_FILE_PATH).use { inputStream ->
            return ServiceAccountCredentials.fromStream(inputStream)
                .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        }
    }
}
