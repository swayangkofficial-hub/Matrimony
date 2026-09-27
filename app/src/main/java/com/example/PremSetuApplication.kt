package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class PremSetuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebaseSafely(this)
    }

    companion object {
        fun initFirebaseSafely(context: Application) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val app = FirebaseApp.initializeApp(context)
                    if (app == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:621137827880:android:premsetu")
                            .setProjectId("aistudio-premsetu")
                            .setApiKey("AIzaSyB3vFMockSafeApiKeyForGracefulInit")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                }
            } catch (e: Exception) {
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:621137827880:android:premsetu")
                        .setProjectId("aistudio-premsetu")
                        .setApiKey("AIzaSyB3vFMockSafeApiKeyForGracefulInit")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                } catch (inner: Exception) {
                    Log.w("PremSetuApplication", "Firebase safe init fallback warning: ${inner.message}")
                }
            }
        }
    }
}
