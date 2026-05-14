package com.celebrations.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.celebrations.app.R
import com.celebrations.app.data.AppDatabase
import com.celebrations.app.data.TributeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val client = OkHttpClient()

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(context)
        val repository = TributeRepository(database.tributeDao())

        repository.refreshTributes()

        val pending = repository.getPendingDownloads()
        if (pending.isEmpty()) return Result.success()

        var downloadedAny = false
        for (tribute in pending) {
            try {
                if (tribute.fileUrl.isEmpty()) continue

                val request = Request.Builder().url(tribute.fileUrl).build()
                val fileName = tribute.fileUrl.substringAfterLast("/")
                val localFile = File(context.getExternalFilesDir(null), "${tribute.id}_$fileName")

                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use

                        response.body?.let { body ->
                            FileOutputStream(localFile).use { output ->
                                body.byteStream().copyTo(output)
                            }
                        }
                    }
                }

                if (localFile.exists() && localFile.length() > 0) {
                    val updatedTribute = tribute.copy(
                        localPath = localFile.absolutePath,
                        isDownloaded = true
                    )
                    repository.updateTribute(updatedTribute)
                    downloadedAny = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (downloadedAny) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        val channelId = "tributes_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tributes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.celebrations.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.new_tribute_notification_title))
            .setContentText(context.getString(R.string.new_tribute_notification_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
