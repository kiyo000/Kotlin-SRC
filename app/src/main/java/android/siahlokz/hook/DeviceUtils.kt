package android.siahlokz.hook

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeviceUtils {

    fun checkIfDeviceIsRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            result != null && result.contains("su")
        } catch (e: Exception) {
            false
        }
    }

    fun isInVirtualApp(context: Context): Boolean {
        val appDataDir = context.filesDir.absolutePath
        if (appDataDir.split("/").size > 6) {
            return true
        } else {
            if (!checkIfDeviceIsRooted()) {
                showWarningDialog(context)
            }
            return false
        }
    }

    private fun showWarningDialog(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Warning")
            .setMessage("You're running this application without root permission which is essential. \n\nPlease use a virtual app if your device is not rooted!")
            .setIcon(R.drawable.icon)
            .setPositiveButton("OK") { _, _ ->
                if (context is AppCompatActivity) {
                    context.finish()
                }
            }
            .setCancelable(false)
            .show()
    }
}