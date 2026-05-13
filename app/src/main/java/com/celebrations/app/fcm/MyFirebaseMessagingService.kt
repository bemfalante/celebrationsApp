package com.celebrations.app.fcm

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.celebrations.app.worker.DownloadWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // When a message is received (either notification or data),
        // we trigger the DownloadWorker to check for new content.
        triggerDownload()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token can be sent to server if needed for targeted notifications
    }

    private fun triggerDownload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(downloadWorkRequest)
    }
}
