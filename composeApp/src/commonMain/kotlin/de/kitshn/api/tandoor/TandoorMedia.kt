package de.kitshn.api.tandoor

import androidx.compose.runtime.Composable
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest

class TandoorMedia(
    val client: TandoorClient
) {

    @Composable
    fun createImageBuilder(endpoint: String): ImageRequest.Builder {
        return createImageBuilder(LocalPlatformContext.current, endpoint)
    }

    fun createImageBuilder(context: PlatformContext, mEndpoint: String): ImageRequest.Builder {
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

    fun getImageBuilderByContext(context: PlatformContext): ImageRequest.Builder {
        return ImageRequest.Builder(context)
            .httpHeaders(
                NetworkHeaders.Builder()
                    .set("Authorization", "Bearer ${client.credentials.token?.token ?: ""}")
                    .build()
            )
    }

    @Composable
    fun getImageBuilder(): ImageRequest.Builder {
        return getImageBuilderByContext(LocalPlatformContext.current)
    }

}