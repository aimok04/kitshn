package de.kitshn

import android.app.Application
import android.content.Context
import de.kitshn.crash.acra.initKitshnAcra

class AndroidApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initKitshnAcra()
    }

}