package de.kitshn.crash

import androidx.compose.runtime.Composable

@Composable
expect fun crashReportHandler(): ((error: Throwable?) -> Unit)?