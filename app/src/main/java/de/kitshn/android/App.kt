package de.kitshn.android

import android.app.Application
import android.content.Context
import de.kitshn.android.acra.initKitshnAcra

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initKitshnAcra()
    }

}