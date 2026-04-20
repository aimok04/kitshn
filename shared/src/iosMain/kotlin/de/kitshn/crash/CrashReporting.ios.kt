package de.kitshn.crash

import androidx.compose.runtime.Composable
import co.touchlab.crashkios.bugsnag.BugsnagKotlin

@Composable
actual fun crashReportHandler(): ((error: Throwable?) -> Unit)? = {
    if(it == null) {
        BugsnagKotlin.sendHandledException(Exception("Throwable is null"))
    } else {
        BugsnagKotlin.sendHandledException(it)
    }
}