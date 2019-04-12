package com.gustavclausen.bikeshare.utils

import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import com.gustavclausen.bikeshare.dialogs.PermissionRationaleDialog

class PermissionUtils {

    companion object {
        fun isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray, permission: String): Boolean {
            grantPermissions.indices.forEach { i ->
                if (permission == grantPermissions[i]) return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }

            return false
        }

        fun requestPermission(
            permission: String,
            requestId: Int,
            rationaleText: String,
            finishActivity: Boolean,
            dismissText: String,
            fragment: Fragment
        ) {
            if (fragment.shouldShowRequestPermissionRationale(permission))
                // Display a dialog with rationale
                PermissionRationaleDialog.newInstance(
                    requestPermission = permission,
                    requestCode = requestId,
                    rationaleText = rationaleText,
                    finishActivity = finishActivity,
                    finishActivityToastText = dismissText
                ).show(fragment.childFragmentManager, "dialog")
            else
                // Permission has not been granted yet, request it
                fragment.requestPermissions(arrayOf(permission), requestId)
        }
    }
}