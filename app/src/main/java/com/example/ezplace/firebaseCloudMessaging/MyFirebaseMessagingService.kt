package com.example.ezplace.firebaseCloudMessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ezplace.R
import com.example.ezplace.activities.SignInActivity
import com.example.ezplace.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /** When a new message s received */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val mSharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)
        if (!mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)) return
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!
            val detailedMessage = remoteMessage.data[Constants.FCM_KEY_DETAILED_MESSAGE]!!

            sendNotification(title, message, detailedMessage)
        }

        remoteMessage.notification.let {
            Log.i("remoteMessage", "${it?.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sendNewTokenToServer(token)
    }

    private fun sendNewTokenToServer(token: String) {
        /** Here we have saved the token in the Shared Preferences */
        val sharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    /** Sends notification with given title and message */
    private fun sendNotification(title: String, message: String,detailedMessage : String) {
        val mSharedPreferences: SharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)
        val intent: Intent = Intent(this, SignInActivity::class.java)

        if (mSharedPreferences.contains(Constants.STUDENT_EMAIL)) {
            val email = mSharedPreferences.getString(Constants.STUDENT_EMAIL, "default")
            val password =
                mSharedPreferences.getString(Constants.STUDENT_PASSWORD, "default")

            intent.putExtra(Constants.STUDENT_EMAIL, email)
            intent.putExtra(Constants.STUDENT_PASSWORD, password)
        }

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(
            this, channelId
        )
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundURI)
            .setContentIntent(pendingIntent)
            .setGroup(Constants.GROUP_KEY)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(detailedMessage))

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel EZ Place Title",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        const val PENDING_INTENT_REQUEST_CODE = 1
    }
}