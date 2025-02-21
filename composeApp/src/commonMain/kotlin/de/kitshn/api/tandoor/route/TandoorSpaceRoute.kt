package de.kitshn.api.tandoor.route

import com.fleeksoft.ksoup.Ksoup
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorScrapedSpace
import de.kitshn.api.tandoor.reqAny
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.util.filter
import io.ktor.util.flattenEntries

class TandoorSpaceRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    val httpClient = HttpClient {
        followRedirects = true
    }

    suspend fun switch(spaceId: Int): Boolean {
        client.reqAny("/switch-active-space/$spaceId/", HttpMethod.Get)
        return true
    }

    // workaround using web scraping since there is no API to retrieve all spaces (currently, planned for Tandoor v2)
    suspend fun retrieveSpaces(): List<TandoorScrapedSpace> {
        val cookie = client.credentials.cookie ?: this.createLoginCookies()
        if(cookie == null) throw NullPointerException("Expected cookie to be not null")

        val response = httpClient.request {
            url(client.credentials.instanceUrl + "/space-overview")
            method = HttpMethod.Get
            headers {
                client.credentials.customHeaders.forEach {
                    set(it.field, it.value)
                }

                set("Referer", client.credentials.instanceUrl)
                set("Cookie", cookie)
            }
        }

        if(!response.status.isSuccess())
            throw TandoorRequestsError(null, response)

        val doc = Ksoup.parse(html = response.bodyAsText())

        val spaces = mutableListOf<TandoorScrapedSpace>()
        doc.getElementsByAttributeValueStarting("href", "/switch-space/").forEach {
            if(!it.hasClass("dropdown-item")) return@forEach

            val id = it.attribute("href")?.value?.split("/")?.last()?.toInt()
                ?: return@forEach

            spaces.add(
                TandoorScrapedSpace(
                    id = id,
                    name = it.text(),
                    active = it.getElementsByClass("fa-dot-circle").size > 0
                )
            )
        }

        if(spaces.isEmpty())
            throw TandoorRequestsError(
                null,
                response,
                doc.html().replace(client.credentials.instanceUrl, "* REDACTED *")
            )

        return spaces.sortedBy { it.name }
    }

    private suspend fun createLoginCookies(): String? {
        // retrieve csrf token and csrf cookie
        val getResponse = httpClient.request {
            url(client.credentials.instanceUrl + "/accounts/login")
            method = HttpMethod.Get
            headers {
                client.credentials.customHeaders.forEach {
                    set(it.field, it.value)
                }

                set("Referer", client.credentials.instanceUrl)
            }
        }

        if(!getResponse.status.isSuccess())
            throw TandoorRequestsError(null, getResponse)

        val content = getResponse.bodyAsText()

        val csrfCookie = getResponse.headers["Set-Cookie"]?.run {
            this.split("csrftoken=")[1].split("; ")[0]
        }

        val csrfForm = content.run {
            "<input type=\"hidden\" name=\"csrfmiddlewaretoken\" value=\"(.+)\">".toRegex()
                .find(this)?.value?.split("value=\"")?.get(1)?.split("\">")?.get(0)
        }

        if(csrfCookie == null || csrfForm == null) return null

        // login using username and password
        val postResponse = httpClient.submitForm(
            formParameters = Parameters.build {
                append("csrfmiddlewaretoken", csrfForm)
                append("login", client.credentials.username)
                append("password", client.credentials.password)
            }
        ) {
            url(client.credentials.instanceUrl + "/accounts/login/")
            headers {
                client.credentials.customHeaders.forEach {
                    set(it.field, it.value)
                }

                set("Cookie", "csrftoken=${csrfCookie}")
                set("Referer", client.credentials.instanceUrl)
            }
        }

        if(postResponse.status.value > 302)
            throw TandoorRequestsError(null, postResponse)

        // build cookie string
        return postResponse.headers.filter { key, _ -> key.lowercase() == "set-cookie" }
            .flattenEntries()
            .joinToString(separator = "; ") { it.second }
    }

}