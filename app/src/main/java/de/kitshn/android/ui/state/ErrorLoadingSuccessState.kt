package de.kitshn.android.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import coil.compose.AsyncImagePainter

@Composable
fun rememberErrorLoadingSuccessState(
    initialValue: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING
): MutableState<ErrorLoadingSuccessState> {
    return rememberSaveable {
        mutableStateOf(initialValue)
    }
}

enum class ErrorLoadingSuccessState {
    LOADING,
    ERROR,
    SUCCESS;

    fun <T> nullWhenLoading(v: T): T? {
        if(this == LOADING) return null
        return v
    }

    fun combine(state: ErrorLoadingSuccessState): ErrorLoadingSuccessState {
        for(value in entries)
            if(this == value || state == value) return value

        return LOADING
    }

    companion object {
        fun bool(value: Boolean): ErrorLoadingSuccessState {
            if(value) return SUCCESS
            return LOADING
        }
    }
}

fun AsyncImagePainter.State.translateState(): ErrorLoadingSuccessState {
    return when(this) {
        is AsyncImagePainter.State.Error -> ErrorLoadingSuccessState.ERROR
        is AsyncImagePainter.State.Success -> ErrorLoadingSuccessState.SUCCESS
        else -> ErrorLoadingSuccessState.LOADING
    }
}