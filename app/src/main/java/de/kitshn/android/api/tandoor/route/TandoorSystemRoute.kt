package de.kitshn.android.api.tandoor.route

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class TandoorSystemData(
    val version: String
)

class TandoorSystemRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun retrieveSystemData(): TandoorSystemData {
        val cookie = this.login()
        val systemInfo = get(client, "system", cookie)

        val version =
            "<h3 class=\"mt-5\">System Information</h3>.+<h5 class=\"mb-1\">(.+)</h5>".toRegex(
                RegexOption.DOT_MATCHES_ALL
            )
                .find(systemInfo)?.value?.split("<h5")?.get(1)?.split(" - ")?.get(1)?.split("</h5>")
                ?.get(0)

        if(version == null) throw Exception("COULD_NOT_PARSE_VERSION")

        val systemData = TandoorSystemData(version = version)
        client.container.systemData = systemData
        return systemData
    }

    private suspend fun login(): String? {
        val data = getNetworkResponse(client, "accounts/login")
        val content = data.data.decodeToString()

        val csrfCookie = data.headers?.get("Set-Cookie")?.run {
            this.split("csrftoken=")[1].split("; ")[0]
        }

        val csrfForm = content.run {
            "<input type=\"hidden\" name=\"csrfmiddlewaretoken\" value=\"(.+)\">".toRegex()
                .find(this)?.value?.split("value=\"")?.get(1)?.split("\">")?.get(0)
        }

        if(csrfCookie == null || csrfForm == null) return null
        return login(client, csrfCookie, csrfForm)
    }

}

@Throws(TandoorRequestsError::class)
suspend fun login(
    client: TandoorClient,
    csrfCookie: String,
    csrfForm: String
) = suspendCoroutine { cont ->
    val url = "${client.credentials.instanceUrl}/accounts/login/"
    val queue = Volley.newRequestQueue(client.context, object : HurlStack() {
        override fun createConnection(url: URL?): HttpURLConnection {
            val connection = super.createConnection(url)
            connection.instanceFollowRedirects = false
            return connection
        }
    })

    val request = object : StringRequest(
        Method.POST,
        url,
        { },
        { error: VolleyError? ->
            if(error?.networkResponse?.statusCode == 302) {
                val cookieString = error.networkResponse?.allHeaders?.toMutableList()
                    ?.filter { it.name == "Set-Cookie" }
                    ?.joinToString(separator = "; ") { it.value }

                cont.resume(cookieString)
            } else {
                cont.resumeWithException(TandoorRequestsError(error))
            }
        }
    ) {
        override fun getBodyContentType(): String {
            return "application/x-www-form-urlencoded; charset=UTF-8"
        }

        override fun getParams(): Map<String, String> {
            val params: MutableMap<String, String> = HashMap()
            params["csrfmiddlewaretoken"] = csrfForm
            params["login"] = client.credentials.username
            params["password"] = client.credentials.password
            return params
        }

        override fun getHeaders(): MutableMap<String, String> {
            val headers = super.getHeaders().toMutableMap()
            headers["Cookie"] = "csrftoken=$csrfCookie"
            headers["Referer"] = url
            return headers
        }
    }

    request.setShouldCache(false)
    queue.add(request)
}

@Throws(TandoorRequestsError::class)
suspend fun getNetworkResponse(
    client: TandoorClient,
    endpoint: String,
    cookie: String? = null
) = suspendCoroutine { cont ->
    val queue = Volley.newRequestQueue(client.context)
    val url = "${client.credentials.instanceUrl}/$endpoint"

    val request = object : StringRequest(
        Method.GET,
        url,
        { },
        { error: VolleyError? ->
            error?.printStackTrace()
            cont.resumeWithException(TandoorRequestsError(error))
        }
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = super.getHeaders().toMutableMap()
            headers["Cookie"] = cookie
            return headers
        }

        override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
            response?.let { cont.resume(response) }
            return super.parseNetworkResponse(response)
        }
    }

    queue.add(request)
}

@Throws(TandoorRequestsError::class)
suspend fun get(
    client: TandoorClient,
    endpoint: String,
    cookie: String? = null
) = suspendCoroutine { cont ->
    val queue = Volley.newRequestQueue(client.context)
    val url = "${client.credentials.instanceUrl}/$endpoint"

    val request = object : StringRequest(
        Method.GET,
        url,
        { it?.let { cont.resume(it) } },
        { error: VolleyError? ->
            error?.printStackTrace()
            cont.resumeWithException(TandoorRequestsError(error))
        }
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = super.getHeaders().toMutableMap()
            headers["Cookie"] = cookie
            return headers
        }
    }

    queue.add(request)
}