package de.kitshn.crash.acra

import android.app.Application
import kitshn.composeApp.BuildConfig
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

fun Application.initKitshnAcra() {
    initAcra {
        buildConfigClass = de.kitshn.BuildConfig::class.java
        reportFormat = StringFormat.JSON

        excludeMatchingSharedPreferencesKeys = listOf(
            "tandoor_credentials"
        )

        httpSender {
            uri = BuildConfig.ACRA_HTTP_URI
            basicAuthLogin = BuildConfig.ACRA_HTTP_BASIC_AUTH_LOGIN
            basicAuthPassword = BuildConfig.ACRA_HTTP_BASIC_AUTH_PASSWORD
        }

        dialog {
            reportDialogClass = AcraCrashReportDialog::class.java
            enabled = true
        }
    }
}