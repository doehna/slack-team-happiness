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
import com.lindar.slackteamhappiness.config.AppProperties
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.util.*

@Service
class TeamHappinessGoogleSheetService(
    private val appProperties: AppProperties
) {
    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

    fun appendValues(selectedFeedback: String, respondentName: String, messageDate: String) {
        try {
            val values = listOf(
                listOf<Any>(
                    selectedFeedback, respondentName, messageDate
                )
            )

            val sheetsService = getSheetsService()

            val body = ValueRange().setValues(values)
            val result = sheetsService.spreadsheets().values()
                .append(appProperties.google.spreadsheetId, appProperties.google.sheetName, body)
                .setValueInputOption("RAW")
                .execute()

            println("${result.updates.updatedCells} cells appended.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun getSheetsService(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials()
        val requestInitializer = HttpCredentialsAdapter(credentials)
        return Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
            .setApplicationName(appProperties.google.applicationName)
            .build()
    }

    @Throws(Exception::class)
    private fun getCredentials(): GoogleCredentials? {
        FileInputStream(System.getProperty("user.home") + appProperties.google.credentialsFilePath).use { inputStream ->
            return ServiceAccountCredentials.fromStream(inputStream)
                .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        }
    }
}
