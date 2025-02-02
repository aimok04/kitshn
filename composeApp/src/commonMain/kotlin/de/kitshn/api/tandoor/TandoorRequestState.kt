package de.kitshn.api.tandoor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import de.kitshn.ui.component.icons.IconWithStateState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kotlin.coroutines.cancellation.CancellationException

enum class TandoorRequestStateState {
    IDLE,
    LOADING,
    ERROR,
    SUCCESS;

    fun toIconWithState(): IconWithStateState {
        return when(this) {
            IDLE -> IconWithStateState.DEFAULT
            LOADING -> IconWithStateState.LOADING
            ERROR -> IconWithStateState.ERROR
            SUCCESS -> IconWithStateState.SUCCESS
        }
    }

    fun toErrorLoadingSuccessState(): ErrorLoadingSuccessState {
        return when(this) {
            IDLE -> ErrorLoadingSuccessState.SUCCESS
            LOADING -> ErrorLoadingSuccessState.LOADING
            ERROR -> ErrorLoadingSuccessState.ERROR
            SUCCESS -> ErrorLoadingSuccessState.SUCCESS
        }
    }
}

class TandoorRequestState {
    var state by mutableStateOf(TandoorRequestStateState.IDLE)
    var error by mutableStateOf<TandoorRequestsError?>(null)

    suspend fun <T> wrapRequest(request: suspend () -> T): T? {
        error = null
        state = TandoorRequestStateState.LOADING

        try {
            val value = request()
            state = TandoorRequestStateState.SUCCESS

            return value
        } catch(e: TandoorRequestsError) {
            if(e.exception is CancellationException) return null

            Logger.e("TandoorRequestState.kt", e)
            error = e
            state = TandoorRequestStateState.ERROR
        } catch(e: Error) {
            Logger.e("TandoorRequestState.kt", e)
            state = TandoorRequestStateState.ERROR
        } catch(e: Exception) {
            Logger.e("TandoorRequestState.kt", e)
            state = TandoorRequestStateState.ERROR
        }

        return null
    }

    fun reset() {
        state = TandoorRequestStateState.IDLE
        error = null
    }

    @Composable
    fun LoadingStateAdapter(
        onChange: (state: ErrorLoadingSuccessState) -> Unit
    ) {
        LaunchedEffect(state) {
            if(state == TandoorRequestStateState.IDLE) return@LaunchedEffect
            onChange(state.toErrorLoadingSuccessState())
        }
    }
}

@Composable
fun rememberTandoorRequestState(): TandoorRequestState {
    return remember { TandoorRequestState() }
}