package de.kitshn

import android.app.Application
import android.content.Context
import de.kitshn.crash.acra.initKitshnAcra
import de.kitshn.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AndroidApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initKitshnAcra()
    }

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@AndroidApp)
        }
    }

}