package com.vrsabu.markme.pushNotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "New Notification"
        val body = message.notification?.body ?: "You have a new message"

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "push_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Persist token locally so the app (e.g., login flow) can read and send it to backend when appropriate
        try {
            FcmTokenStore.saveToken(applicationContext, token)
        } catch (_: Exception) {
            // ignore storage failure; nothing we can do in service
        }

        // Optionally, you could immediately attempt to send the token to your backend if you have a stored auth token.
        // That's intentionally not done here because network work from FirebaseMessagingService can be constrained.
        // Instead, read FcmTokenStore.getToken(context) from your login flow and call your API to register the token.
    }
}
