package com.got.oubb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.content.Context




class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle token refresh here if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val title = remoteMessage.notification?.title ?: "Título"
        val body = remoteMessage.notification?.body ?: "Cuerpo"
        showNotification(title, body)

    }


    fun getRemoteView(title: String,message: String): RemoteViews {
        val remoteView=RemoteViews("com.got.oubb",R.layout.notification)

        remoteView.setTextViewText(R.id.title,title)
        remoteView.setTextViewText(androidx.appcompat.R.id.message,message)
        remoteView.setImageViewResource(R.id.app_logo,R.drawable.ic_launcher_foreground)

        return remoteView
    }


    private fun showNotification(title: String, message: String) {
        val channelId = "01"
        val channelName = "GO TO UBB"

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_action_person) // Reemplaza con tu icono de notificación
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notificationId = System.currentTimeMillis().toInt() // Use a unique ID
        notificationManager.notify(notificationId, notificationBuilder.build())
    }



}