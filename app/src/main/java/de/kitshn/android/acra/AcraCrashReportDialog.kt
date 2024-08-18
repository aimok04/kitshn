package de.kitshn.android.acra

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import de.kitshn.android.R
import org.acra.dialog.CrashReportDialog
import org.acra.dialog.CrashReportDialogHelper
import org.json.JSONObject

class AcraCrashReportDialog : CrashReportDialog() {

    private lateinit var myHelper: CrashReportDialogHelper
    private var doNotFinish = false
    private var doNotCancel = false

    private var text: String = ""

    override fun buildAndShowDialog(savedInstanceState: Bundle?) {
        this.myHelper = CrashReportDialogHelper(this, intent)
        showBaseDialog()
    }

    private fun showBaseDialog() {
        val commentLayout: View = layoutInflater.inflate(R.layout.acra_crash_report_comment, null)

        val editText =
            commentLayout.findViewById<TextInputEditText>(R.id.acra_crash_report_comment_edittext)
        editText.setText(text)
        editText.doOnTextChanged { it, _, _, _ -> this.text = it.toString() }

        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.rounded_bug_report_24)
            .setTitle(getString(R.string.acra_dialog_title))
            .setMessage(getString(R.string.acra_dialog_message))
            .setView(commentLayout)
            .setPositiveButton(getString(R.string.acra_dialog_button_positive)) { dialog, _ ->
                this.myHelper.sendCrash(text, null)
                this.doNotCancel = true
                dialog.dismiss()
            }
            .setNeutralButton(getString(R.string.acra_dialog_button_neutral)) { _, _ ->
                showReportDetailsDialog()
            }
            .setNegativeButton(getString(R.string.acra_dialog_button_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        dialog.setOnDismissListener {
            if(doNotFinish) {
                doNotFinish = false
                return@setOnDismissListener
            }

            if(!doNotCancel) myHelper.cancelReports()

            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 150)
        }
    }

    private fun showReportDetailsDialog() {
        this.doNotFinish = true

        val reportString = JSONObject(this.myHelper.reportData.toJSON()).toString(4)

        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.rounded_data_object_24)
            .setTitle(getString(R.string.common_error_report))
            .setMessage(reportString)
            .setPositiveButton(getString(R.string.action_back)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnDismissListener { showBaseDialog() }
        dialog.show()
    }

}