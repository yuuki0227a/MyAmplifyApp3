package com.aaa.myamplifyapp3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.amplifyframework.core.Amplify
import com.amplifyframework.notifications.pushnotifications.NotificationPayload
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MyAmplifyApp3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FCMトークン取得
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        // FCMサービスの起動
        startService(
            Intent(this, MyFirebaseMessagingService::class.java)
        )

        /* 記録通知が開かれました https://docs.amplify.aws/android/build-a-backend/push-notifications/record-notifications/#record-notification-opened */
        // Get the payload from the intent
        NotificationPayload.fromIntent(intent)?.let {
            // Record notification opened when activity launches
            Amplify.Notifications.Push.recordNotificationOpened(it,
                { Log.i(TAG, "Successfully recorded notification opened") },
                { error -> Log.e(TAG, "Error recording notification opened", error) }
            )
        }

        /* Amplify Auth からユーザー ID を取得します https://docs.amplify.aws/android/build-a-backend/push-notifications/identify-user/#get-the-user-id-from-amplify-auth  */
//        var user: String? = null
//        Amplify.Auth.getCurrentUser(
//            { authUser -> user = authUser.userId },
//            { Log.e("MyAmplifyApp", "Error getting current user", it) }
//        )
//
//        if(user != null){
//            /* Amazon Pinpoint に対してユーザーを識別する https://docs.amplify.aws/android/build-a-backend/push-notifications/identify-user/#identify-the-user-to-amazon-pinpoint */
//            Amplify.Notifications.Push.identifyUser(
//                user!!,
//                { Log.i("MyAmplifyApp", "Identified user successfully") },
//                { error -> Log.e("MyAmplifyApp", "Error identifying user", error) }
//            )
//        }
    }
}