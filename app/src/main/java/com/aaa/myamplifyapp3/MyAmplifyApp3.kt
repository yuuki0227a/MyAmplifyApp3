package com.aaa.myamplifyapp3

import android.app.Application

class MyAmplifyApp3: Application() {
    override fun onCreate() {
        super.onCreate()
//        try {
//            Amplify.addPlugin(AWSCognitoAuthPlugin())
//            Amplify.addPlugin(AWSPinpointPushNotificationsPlugin())
//            Amplify.addPlugin(AWSPinpointAnalyticsPlugin())
//            // 分析で使用する
//            Amplify.addPlugin(AWSDataStorePlugin())
//            Amplify.configure(applicationContext)
//            Log.i("MyAmplifyApp", "Initialized Amplify")
//
//            val event = AnalyticsEvent.builder()
//                .name("PasswordReset")
//                .addProperty("Channel", "SMS")
//                .addProperty("Successful", true)
//                .addProperty("ProcessDuration", 792)
//                .addProperty("UserAge", 120.3)
//                .build()
//            Amplify.Analytics.recordEvent(event)
//        } catch (error: AmplifyException) {
//            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
//        }
    }
}