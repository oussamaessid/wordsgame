package com.example.wordgame

import android.app.Application
import com.example.wordgame.di.AppContainer

class WordGameApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}