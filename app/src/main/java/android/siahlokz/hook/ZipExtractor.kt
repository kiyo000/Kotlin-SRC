package android.siahlokz.hook

import android.content.Context
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

class ZipExtractor(private val context: Context) {

    fun unzip(zipFilePath: String, password: String, outputDir: String): Boolean {
        return try {
            val zipFile = ZipFile(zipFilePath)

            if (zipFile.isEncrypted) {
                zipFile.setPassword(password.toCharArray())
            }

            zipFile.extractAll(outputDir)
            true
        } catch (e: ZipException) {
            e.printStackTrace()
            false
        }
    }
}