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
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
object TeamHappinessGoogleSheetService {
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private const val CREDENTIALS_FILE_PATH = "/credentials.json" // Adjust the path as necessary
    private const val APPLICATION_NAME = "Google Sheets Example"
    private const val SPREADSHEET_ID = "1JlDQvfBi-kNLhbNx4Hwtb2QYIwT4L-b7rrp2RF4vvw4"
    private const val SHEET_NAME = "Sheet1!A1"

    fun appendValues(selectedFeedback: String, respondentName: String) {
        try {

            val values = listOf(
                listOf<Any>(
                    selectedFeedback, respondentName, getNowTimeString()
                )
            )

            val sheetsService = getSheetsService()

            val body = ValueRange().setValues(values)
            val result = sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, SHEET_NAME, body)
                .setValueInputOption("RAW")
                .execute()

            println("${result.updates.updatedCells} cells appended.")
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
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    @Throws(Exception::class)
    private fun getCredentials(): GoogleCredentials? {
        val credentialsStream = TeamHappinessGoogleSheetService::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
        return ServiceAccountCredentials.fromStream(credentialsStream)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))
    }

    fun getNowTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mma z", Locale.ENGLISH)
        val zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).format(formatter)
        return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}
