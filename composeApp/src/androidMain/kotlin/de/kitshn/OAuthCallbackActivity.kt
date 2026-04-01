package de.kitshn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Transparent Activity that receives OAuth callback redirects
 * (kitshn://auth/callback?token=...) from Chrome Custom Tabs,
 * forwards the data to the main AppActivity, and finishes itself.
 */
class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callbackUri = intent.data

        val mainIntent = Intent(this, AppActivity::class.java).apply {
            action = ACTION_OAUTH_CALLBACK
            data = callbackUri
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(mainIntent)
        finish()
    }

    companion object {
        const val ACTION_OAUTH_CALLBACK = "de.kitshn.OAUTH_CALLBACK"
    }
}
