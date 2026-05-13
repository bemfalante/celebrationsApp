package com.celebrations.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.celebrations.app.R
import com.celebrations.app.data.AppDatabase
import com.celebrations.app.data.TributeRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(context)
        val repository = TributeRepository(database.tributeDao())

        // Sync metadata first to find new tributes
        repository.refreshTributes()

        val pending = repository.getPendingDownloads()
        if (pending.isEmpty()) return Result.success()

        var downloadedAny = false
        for (tribute in pending) {
            try {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(tribute.fileUrl)
                val localFile = File(context.getExternalFilesDir(null), "${tribute.id}_${storageRef.name}")

                storageRef.getFile(localFile).await()

                val updatedTribute = tribute.copy(
                    localPath = localFile.absolutePath,
                    isDownloaded = true
                )
                repository.updateTribute(updatedTribute)
                downloadedAny = true
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.new_tribute_notification_title))
            .setContentText(context.getString(R.string.new_tribute_notification_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
