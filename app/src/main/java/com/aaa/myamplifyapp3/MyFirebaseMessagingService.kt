package com.aaa.myamplifyapp3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.amplifyframework.core.Amplify
import com.amplifyframework.notifications.pushnotifications.NotificationContentProvider
import com.amplifyframework.notifications.pushnotifications.NotificationPayload
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private data class MessageFCM(
        var messageTitle: String = "",
        var messageBody: String = "",
    )
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        /* ユーザーがプッシュ通知をいつ受信または開いたかを記録できます。 https://docs.amplify.aws/android/build-a-backend/push-notifications/record-notifications/#handle-notification-received */
        // Convert the RemoteMessage into a NotificationPayload
        val notificationPayload = NotificationPayload(NotificationContentProvider.FCM(remoteMessage.data))

        // Amplify should handle notification if it is sent from Pinpoint
        val isAmplifyMessage = Amplify.Notifications.Push.shouldHandleNotification(notificationPayload)
        if (isAmplifyMessage) {
            // let Amplify handle foreground and background message received
            Amplify.Notifications.Push.handleNotificationReceived(notificationPayload,
                { Log.i("MyAmplifyApp", "Successfully handled a notification") },
                { error -> Log.e("MyAmplifyApp", "Error handling notification", error) }
            )

            /* 記録通知を受信しました https://docs.amplify.aws/android/build-a-backend/push-notifications/record-notifications/#record-notification-received */
            // Record notification received with Amplify
            Amplify.Notifications.Push.recordNotificationReceived(notificationPayload,
                { Log.i("MyAmplifyApp", "Successfully recorded a notification received") },
                { error -> Log.e("MyAmplifyApp", "Error recording notification received", error) }
            )
        }


        // メッセージが届いていない場合は、こちらを参照してください: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")
        // メッセージがデータペイロードを含んでいるか確認します。
        if (remoteMessage.data.isNotEmpty()) {
            // データが長時間実行されるジョブによって処理される必要があるか確認します
//            if (needsToBeScheduled()) {
//                // 長時間実行されるタスク（10秒以上）の場合は WorkManager を使用します。
////                scheduleJob()
//            } else {
//                // 10秒以内にメッセージを処理します
//                handleNow()
//            }
            handleNow()
        }
        // PinPointからのメッセージ取得
        val messageFCM: MessageFCM = MessageFCM()
        messageFCM.messageTitle = remoteMessage.data.getValue("pinpoint.notification.title") ?: ""
        messageFCM.messageBody = remoteMessage.data.getValue("pinpoint.notification.body") ?: ""
        // 通知の生成
        sendNotification(messageFCM)
    }
    // [END receive_message]

    private fun needsToBeScheduled() = true

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Register device with Pinpoint
        Amplify.Notifications.Push.registerDevice(token,
            { Log.i("MyAppService", "Successfully registered device") },
            { error -> Log.e("MyAppService", "Error registering device", error) }
        )

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

//    private fun scheduleJob() {
//        // [START dispatch_job]
//        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
//            .build()
//        WorkManager.getInstance(this)
//            .beginWith(work)
//            .enqueue()
//        // [END dispatch_job]
//    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(messageFCM: MessageFCM) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 100
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageFCM.messageTitle)
            .setContentText(messageFCM.messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)   // 通知の優先度

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

//    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
//        override fun doWork(): Result {
//            // TODO(developer): add long running task here.
//            return Result.success()
//        }
//    }
}
