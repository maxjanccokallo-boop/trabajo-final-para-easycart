package com.example.easycart

import android.app.Application
import com.google.firebase.FirebaseApp

class EasyCartApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
