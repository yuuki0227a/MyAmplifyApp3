package com.aaa.myamplifyapp3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationClient
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MyAmplifyApp3"
    }

    private lateinit var mPinpointManager: PinpointManager

    private fun getPinpointManager(applicationContext: Context): PinpointManager {
        if (!this::mPinpointManager.isInitialized) {
            // Initialize the AWS Mobile Client
            val awsConfig = AWSConfiguration(applicationContext)
            println("★★★ awsConfig $awsConfig")

            AWSMobileClient.getInstance()
                .initialize(applicationContext, awsConfig, object : Callback<UserStateDetails> {
                    override fun onResult(userStateDetails: UserStateDetails) {
                        Log.i("INIT", userStateDetails.userState.toString())
                    }
                    override fun onError(e: Exception) {
                        Log.e("INIT", "Initialization error.", e)
                    }
                })
            val pinpointConfig = PinpointConfiguration(
                applicationContext,
                AWSMobileClient.getInstance(),
                awsConfig
            )
            println("★★★ pinpointConfig $pinpointConfig")

            mPinpointManager = PinpointManager(pinpointConfig)
        }

        return mPinpointManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    override fun onStart() {
        super.onStart()
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
            Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
        })

        try {
            // https://qiita.com/Idenon/items/a77f2da4de78dd0db74d
            mPinpointManager = getPinpointManager(applicationContext)
            mPinpointManager.sessionClient.startSession()
            println("★★★ pinpointManager $mPinpointManager")
            println("★★★ pinpointManager complete")

            // プッシュ通知からアプリを開いた場合intentにデータが入っている
            val campaignId = intent.getStringExtra("campaignId")
            val treatmentId = intent.getStringExtra("treatmentId")
            val campaignActivityId = intent.getStringExtra("campaignActivityId")
            val title = intent.getStringExtra("title")
            val body = intent.getStringExtra("body")
            if(campaignId != null && treatmentId != null && campaignActivityId != null && title != null && body != null){
                notificationOpenedEvent(campaignId, treatmentId, campaignActivityId, title, body)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        val button: Button = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
//            submitEvent()
        }
    }

    override fun onStop() {
        super.onStop()
        mPinpointManager.sessionClient.stopSession()
        mPinpointManager.analyticsClient.submitEvents()
    }
    private fun submitEvent(){
        mPinpointManager.analyticsClient?.submitEvents()
        println("★★★ submitEvent pinpointManager.analyticsClient ${mPinpointManager.analyticsClient}")
        println("★★★ submitEvent pinpointManager.analyticsClient.allEvents ${mPinpointManager.analyticsClient.allEvents}")
    }
    private fun notificationOpenedEvent(campaignId: String, treatmentId: String, campaignActivityId: String, title: String, body: String){
        println("★★★ notificationOpenedEvent")
        val event: AnalyticsEvent? =
            mPinpointManager.analyticsClient.createEvent("_campaign.opened_notification")
                .withAttribute("_campaign_id", campaignId)
                .withAttribute("treatment_id", treatmentId)
                .withAttribute("campaign_activity_id", campaignActivityId)
                .withAttribute("notification_title", title)
                .withAttribute("notification_body", body)
                .withMetric("Opened", 1.0)
        println("★★★ notificationOpenedEvent event $event")
        mPinpointManager.analyticsClient?.recordEvent(event)
        submitEvent()
    }
}