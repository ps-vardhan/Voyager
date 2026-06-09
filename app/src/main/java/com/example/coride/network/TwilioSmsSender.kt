package com.example.coride.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Base64

object TwilioSmsSender {
    private const val TAG = "TwilioSmsSender"

    suspend fun sendSms(
        toPhone: String,
        message: String,
        accountSid: String?,
        authToken: String?,
        fromPhone: String?
    ): Boolean = withContext(Dispatchers.IO) {
        if (accountSid.isNullOrEmpty() || authToken.isNullOrEmpty() || fromPhone.isNullOrEmpty()) {
            Log.d(TAG, "[MOCK SMS to $toPhone]: $message")
            return@withContext true
        }

        try {
            val urlString = "https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            
            // Base64 basic authentication
            val userPass = "$accountSid:$authToken"
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userPass.toByteArray())
            connection.setRequestProperty("Authorization", basicAuth)
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val postData = "To=" + URLEncoder.encode(toPhone, "UTF-8") +
                    "&From=" + URLEncoder.encode(fromPhone, "UTF-8") +
                    "&Body=" + URLEncoder.encode(message, "UTF-8")

            connection.outputStream.use { os ->
                os.write(postData.toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "SMS sent successfully to $toPhone")
                return@withContext true
            } else {
                Log.e(TAG, "Failed to send SMS. Response code: $responseCode")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS via Twilio", e)
            return@withContext false
        }
    }
}
