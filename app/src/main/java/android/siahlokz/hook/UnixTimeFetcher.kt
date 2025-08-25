package android.siahlokz.hook;

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UnixTimeFetcher {

    private val client = OkHttpClient()

    private val maxRetries = 8
    private val retryDelay = 1000L

    fun getUnixTimestamp(): String? {
        val url = "http://worldtimeapi.org/api/ip"
        var retryCount = 0

        while (retryCount < maxRetries) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseText = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseText)
                    return jsonResponse.optString("unixtime", "Error: Unix timestamp not found")
                } else {
                    return "Error: ${response.code}"
                }
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= maxRetries) {
                    return "Error: Failed after $maxRetries retries. Exception: ${e.message}"
                }
                TimeUnit.MILLISECONDS.sleep(retryDelay)
            }
        }

        return "Error: Maximum retry attempts reached"
    }
}