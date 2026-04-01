package de.kitshn

import android.app.Application
import android.content.Context
import de.kitshn.crash.acra.initKitshnAcra

class AndroidApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initKitshnAcra()
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}