package de.kitshn.android.acra

import android.app.Application
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

fun Application.initKitshnAcra() {
    initAcra {
        buildConfigClass = de.kitshn.android.BuildConfig::class.java
        reportFormat = StringFormat.JSON

        mailSender {
            mailTo = getString(de.kitshn.android.R.string.acra_mailto)
            subject =
                "[ ACRA ${de.kitshn.android.BuildConfig.BUILD_TYPE.uppercase()} ] ${de.kitshn.android.BuildConfig.APPLICATION_ID} ${de.kitshn.android.BuildConfig.VERSION_NAME} Crash report"
            body = "The attached file contains crash report data."
            reportFileName = "acra-report.json"
        }

        dialog {
            reportDialogClass = AcraCrashReportDialog::class.java
            enabled = true
        }
    }
}