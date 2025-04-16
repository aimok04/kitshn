package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.json
import de.kitshn.saveBreadcrumb
import kotlinx.serialization.Serializable

@Serializable
data class TandoorServerSettings(
    val version: String
)

class TandoorServerSettingsRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun current(): TandoorServerSettings {
        val response = json.decodeFromString<TandoorServerSettings>(
            client.getObject("/server-settings/current/").toString()
        )

        saveBreadcrumb("serverVersion", response.version)

        client.container.serverSettings = response
        return response
    }

}