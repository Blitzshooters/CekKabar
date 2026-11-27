package com.tanilink.cekkabar

import android.app.Application
import com.tanilink.cekkabar.di.AppContainer

class CekKabarApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
