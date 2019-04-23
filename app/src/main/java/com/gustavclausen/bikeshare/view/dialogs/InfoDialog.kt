package com.gustavclausen.bikeshare.view.dialogs

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
        private const val ARG_DIALOG_TEXT = "dialogText"
        private const val ARG_FINISH_ACTIVITY = "finish"
        private const val ARG_FINISH_ACTIVITY_TOAST_TEXT = "finishActivityToastText"

        fun newInstance(
            dialogText: String,
            finishActivity: Boolean = false,
            finishActivityToastText: String = ""
        ): InfoDialog {
            val args = Bundle()
            args.putString(ARG_DIALOG_TEXT, dialogText)
            args.putBoolean(ARG_FINISH_ACTIVITY, finishActivity)
            args.putString(ARG_FINISH_ACTIVITY_TOAST_TEXT, finishActivityToastText)

            val dialog = InfoDialog()
            dialog.arguments = args

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogText = arguments!!.getString(ARG_DIALOG_TEXT)
        mFinishActivityToastText = arguments!!.getString(ARG_FINISH_ACTIVITY_TOAST_TEXT)
        mFinishActivity = arguments!!.getBoolean(ARG_FINISH_ACTIVITY)

        return AlertDialog.Builder(activity!!)
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