package de.kitshn.android.acra

import android.app.Application
import de.kitshn.android.R
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

fun Application.initKitshnAcra() {
    initAcra {
        buildConfigClass = de.kitshn.android.BuildConfig::class.java
        reportFormat = StringFormat.JSON

        httpSender {
            uri = getString(R.string.acra_http_uri)
            basicAuthLogin = getString(R.string.acra_http_basic_auth_login)
            basicAuthPassword = getString(R.string.acra_http_basic_auth_password)
        }

        dialog {
            reportDialogClass = AcraCrashReportDialog::class.java
            enabled = true
        }
    }
}