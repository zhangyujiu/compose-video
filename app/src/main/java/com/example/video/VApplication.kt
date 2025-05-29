package com.example.video

import android.app.Application

class VApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Apps.initialize(this)
    }
}