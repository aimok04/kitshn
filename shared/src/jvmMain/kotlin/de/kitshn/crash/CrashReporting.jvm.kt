package de.kitshn.crash

import androidx.compose.runtime.Composable

@Composable
actual fun crashReportHandler(): ((error: Throwable?) -> Unit)? = null