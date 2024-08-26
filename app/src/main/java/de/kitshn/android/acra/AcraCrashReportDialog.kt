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

    private var commentText: String = ""
    private var emailText: String = ""

    override fun buildAndShowDialog(savedInstanceState: Bundle?) {
        this.myHelper = CrashReportDialogHelper(this, intent)
        showBaseDialog()
    }

    private fun showBaseDialog() {
        val formLayout: View = layoutInflater.inflate(R.layout.acra_crash_report_form, null)

        val commentEditText =
            formLayout.findViewById<TextInputEditText>(R.id.acra_crash_report_comment_edittext)
        commentEditText.setText(commentText)
        commentEditText.doOnTextChanged { it, _, _, _ -> this.commentText = it.toString() }

        val emailEditText =
            formLayout.findViewById<TextInputEditText>(R.id.acra_crash_report_email_edittext)
        emailEditText.setText(emailText)
        emailEditText.doOnTextChanged { it, _, _, _ -> this.emailText = it.toString() }

        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.rounded_bug_report_24)
            .setTitle(getString(R.string.acra_dialog_title))
            .setMessage(getString(R.string.acra_dialog_message))
            .setView(formLayout)
            .setPositiveButton(getString(R.string.acra_dialog_button_positive)) { dialog, _ ->
                this.myHelper.sendCrash(commentText.ifBlank { null }, emailText.ifBlank { null })
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