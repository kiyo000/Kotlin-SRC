package android.siahlokz.hook

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AssetExtractor(private val context: Context) {

    fun extractAssetFile(assetFileName: String, targetDirectory: String): String? {
        val assetManager: AssetManager = context.assets
        val dir = File(context.filesDir, targetDirectory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val outFile = File(dir, assetFileName)

        return try {
            assetManager.open(assetFileName).use { inputStream ->
                FileOutputStream(outFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }

            outFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}