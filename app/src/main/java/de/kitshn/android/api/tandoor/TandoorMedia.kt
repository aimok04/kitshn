package de.kitshn.android.api.tandoor

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

class TandoorMedia(
    val client: TandoorClient
) {

    @Composable
    fun createImageBuilder(endpoint: String): ImageRequest.Builder {
        return createImageBuilder(LocalContext.current, endpoint)
    }

    fun createImageBuilder(context: Context, mEndpoint: String): ImageRequest.Builder {
        var endpoint = mEndpoint

        val builder =
            if(endpoint.startsWith("/") || endpoint.startsWith(client.credentials.instanceUrl)) {
                if(endpoint.startsWith("/")) endpoint = "${client.credentials.instanceUrl}$endpoint"
                getImageBuilderByContext(context)
            } else {
                ImageRequest.Builder(context)
            }

        return builder.data(endpoint)
    }

    fun getImageBuilderByContext(context: Context): ImageRequest.Builder {
        return ImageRequest.Builder(context)
            .addHeader("Authorization", "Bearer ${client.credentials.token?.token ?: ""}")
    }

    @Composable
    fun getImageBuilder(): ImageRequest.Builder {
        return getImageBuilderByContext(LocalContext.current)
    }

}