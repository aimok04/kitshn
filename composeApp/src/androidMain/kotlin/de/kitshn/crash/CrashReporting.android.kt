package de.kitshn.crash

import androidx.compose.runtime.Composable
import org.acra.ACRA
import org.acra.ktx.sendWithAcra

@Composable
actual fun crashReportHandler(): ((error: Throwable?) -> Unit)? = {
    if(it == null) {
        ACRA.errorReporter.handleException(null)
    } else {
        it.sendWithAcra()
    }
}