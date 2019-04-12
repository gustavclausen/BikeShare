package com.gustavclausen.bikeshare.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.Toast

class InfoDialog : DialogFragment() {

    private var mFinishActivity = false // Finish/quit Activity after closing dialog
    private lateinit var mFinishActivityToastText: String // Text to display in toast if Activity quits

    companion object {
        private const val ARGUMENT_DIALOG_TEXT = "dialogText"
        private const val ARGUMENT_FINISH_ACTIVITY = "finish"
        private const val ARGUMENT_FINISH_ACTIVITY_TOAST_TEXT = "finishActivityToastText"

        fun newInstance(
            dialogText: String,
            finishActivity: Boolean = false,
            finishActivityToastText: String = ""
        ): InfoDialog {
            val arguments = Bundle()
            arguments.putString(ARGUMENT_DIALOG_TEXT, dialogText)
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
            arguments.putString(ARGUMENT_FINISH_ACTIVITY_TOAST_TEXT, finishActivityToastText)

            val dialog = InfoDialog()
            dialog.arguments = arguments

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogText = arguments!!.getString(ARGUMENT_DIALOG_TEXT)
        mFinishActivityToastText = arguments!!.getString(ARGUMENT_FINISH_ACTIVITY_TOAST_TEXT)
        mFinishActivity = arguments!!.getBoolean(ARGUMENT_FINISH_ACTIVITY)

        return AlertDialog
            .Builder(activity!!)
            .setMessage(dialogText)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        if (mFinishActivity) {
            Toast.makeText(activity, mFinishActivityToastText, Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }
}