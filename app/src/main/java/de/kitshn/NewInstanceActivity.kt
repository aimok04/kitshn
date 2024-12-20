package de.kitshn

import android.content.Intent
import androidx.activity.ComponentActivity

class NewInstanceActivity : ComponentActivity() {
    override fun onResume() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        )

        finish()
        super.onResume()
    }
}