package android.siahlokz.hook

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

class ProcessRetriever(private val context: Context) {

    fun getRunningProcess(): String {
        val appDataDir = context.filesDir.absolutePath
        val output = StringBuilder()

        try {
            val command: Array<String> = if (DeviceUtils().checkIfDeviceIsRooted()) {
                arrayOf(
                    "sh",
                    "-c",
                    "su -c chmod 7777 $appDataDir/.thumbcache/pids && su -c $appDataDir/.thumbcache/pids"
                )
            } else {
                arrayOf(
                    "sh",
                    "-c",
                    "chmod 7777 $appDataDir/.thumbcache/pids && $appDataDir/.thumbcache/pids"
                )
            }

            val process = Runtime.getRuntime().exec(command)

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.appendLine(line)
                }
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw RuntimeException("Command execution failed with exit code $exitCode")
            }

        } catch (e: IOException) {
            throw RuntimeException("Error executing shell command", e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Command execution was interrupted", e)
        }

        return output.toString().trim()
    }
}