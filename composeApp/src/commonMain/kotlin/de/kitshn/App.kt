package de.kitshn

import android.app.Application
import android.content.Context
import de.kitshn.acra.initKitshnAcra

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initKitshnAcra()
    }

}