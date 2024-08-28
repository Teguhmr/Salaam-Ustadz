package id.uinjkt.salaamustadz.utils.downloader

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.net.toUri

class AndroidDownloader (
    context: Context): Downloader {
    private val downloadManager =
        context.getSystemService(DownloadManager::class.java)

    override fun downloadImage(url: String): Long {
        val nameOfFile = URLUtil.guessFileName(url, null, "image/jpeg")
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setTitle(nameOfFile)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SalaamUstadzImg-$nameOfFile")
        return downloadManager.enqueue(request)
    }
}