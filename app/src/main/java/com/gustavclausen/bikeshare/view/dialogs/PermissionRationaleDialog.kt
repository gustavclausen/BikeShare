package com.gustavclausen.bikeshare.view.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.Toast

class PermissionRationaleDialog : DialogFragment() {

    private var mFinishActivity = false // Finish/quit Activity after closing dialog
    private lateinit var mFinishActivityToastText: String // Text to display in toast if Activity quits

    companion object {
        private const val ARG_PERMISSION_REQUEST_NAME = "permissionRequestName"
        private const val ARG_PERMISSION_REQUEST_CODE = "requestCode"
        private const val ARG_RATIONALE_TEXT = "rationaleText"
        private const val ARG_FINISH_ACTIVITY = "finish"
        private const val ARG_FINISH_ACTIVITY_TOAST_TEXT = "finishActivityToastText"

        fun newInstance(
            requestPermission: String,
            requestCode: Int,
            rationaleText: String,
            finishActivity: Boolean = false,
            finishActivityToastText: String = ""
        ): PermissionRationaleDialog {
            val args = Bundle()
            args.putString(ARG_PERMISSION_REQUEST_NAME, requestPermission)
            args.putInt(ARG_PERMISSION_REQUEST_CODE, requestCode)
            args.putString(ARG_RATIONALE_TEXT, rationaleText)
            args.putBoolean(ARG_FINISH_ACTIVITY, finishActivity)
            args.putString(ARG_FINISH_ACTIVITY_TOAST_TEXT, finishActivityToastText)

            val dialog = PermissionRationaleDialog()
            dialog.arguments = args

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestPermission = arguments!!.getString(ARG_PERMISSION_REQUEST_NAME)
        val requestCode = arguments!!.getInt(ARG_PERMISSION_REQUEST_CODE)
        val rationaleText = arguments!!.getString(ARG_RATIONALE_TEXT)
        mFinishActivity = arguments!!.getBoolean(ARG_FINISH_ACTIVITY)
        mFinishActivityToastText = arguments!!.getString(ARG_FINISH_ACTIVITY_TOAST_TEXT)

        return AlertDialog.Builder(activity!!)
                          .setMessage(rationaleText)
                          .setPositiveButton(android.R.string.ok) { _, _ ->
                              // After click on OK, request the permission
                              parentFragment?.requestPermissions(arrayOf(requestPermission), requestCode)
                              // Do not finish the Activity while requesting permission
                              mFinishActivity = false
                          }
                          .setNegativeButton(android.R.string.cancel, null)
                          .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (mFinishActivity) {
            Toast.makeText(activity, mFinishActivityToastText, Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }
}