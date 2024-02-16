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
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent
//import com.amplifyframework.core.Amplify
//import com.amplifyframework.notifications.pushnotifications.NotificationContentProvider
//import com.amplifyframework.notifications.pushnotifications.NotificationPayload
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
//        var notificationPayload: NotificationPayload? = null
    }



    private data class MessageFCM(
        var messageTitle: String = "",
        var messageBody: String = "",
    )
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("★★★ remoteMessage: $remoteMessage")
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
        // 通知の生成
        sendNotification(remoteMessage)
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
//        Amplify.Notifications.Push.registerDevice(token,
//            { Log.i(TAG, "Successfully registered device") },
//            { error -> Log.e(TAG, "Error registering device", error) }
//        )
        sendRegistrationToServer(token)
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        // RemoteMessage を NoticePayload に変換する
//        val notificationPayload = NotificationPayload(NotificationContentProvider.FCM(remoteMessage.data))

        // Pinpoint から送信された通知を Amplify で処理する必要がある
//        val isAmplifyMessage = Amplify.Notifications.Push.shouldHandleNotification(notificationPayload!!)
//        if (isAmplifyMessage) {
//            // Record notification received with Amplify
//            Amplify.Notifications.Push.recordNotificationReceived(notificationPayload,
//                { Log.i(TAG, "Successfully recorded a notification received") },
//                { error -> Log.e(TAG, "Error recording notification received", error) }
//            )
//        }
        var campaignId: String = ""
        var treatmentId: String = ""
        var campaignActivityId: String = ""
        var title: String = ""
        var body: String = ""
        try {
            // PinPointからのメッセージ取得
            campaignId = remoteMessage.data.getValue("pinpoint.campaign.campaign_id") ?: ""
            treatmentId = remoteMessage.data.getValue("pinpoint.campaign.treatment_id") ?: ""
            campaignActivityId = remoteMessage.data.getValue("pinpoint.campaign.campaign_activity_id") ?: ""
            title = remoteMessage.data.getValue("pinpoint.notification.title") ?: ""
            body = remoteMessage.data.getValue("pinpoint.notification.body") ?: ""
        }catch (e: NoSuchElementException){
            e.printStackTrace()
        }
        try {
            // PinPointからのメッセージ取得
            title = remoteMessage.data.getValue("pinpoint.notification.title") ?: ""
            body = remoteMessage.data.getValue("pinpoint.notification.body") ?: ""
        }catch (e: NoSuchElementException){
            e.printStackTrace()
        }
        val messageFCM = MessageFCM()
        messageFCM.messageTitle = title
        messageFCM.messageBody = body
        println("★★★ remoteMessage.data: ${remoteMessage.data}")

        val notificationId = System.currentTimeMillis().toInt()
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("notificationPayload", notificationPayload)
        // PinPointの情報をintentに設定(起動時にMainActivityで受け取る)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("campaignId", campaignId)
        intent.putExtra("treatmentId", treatmentId)
        intent.putExtra("campaignActivityId", campaignActivityId)
        intent.putExtra("title", title)
        intent.putExtra("body", body)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 100
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // intentを保持するために必要。(notificationPayloadの受け渡しのため)
//            PendingIntent.FLAG_IMMUTABLE, // これは既存のインテントが保持されない。
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

//        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


//    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
//        override fun doWork(): Result {
//            // TODO(developer): add long running task here.
//            return Result.success()
//        }
//    }
}
