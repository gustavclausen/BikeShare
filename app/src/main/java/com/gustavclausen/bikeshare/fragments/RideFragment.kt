package com.gustavclausen.bikeshare.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.helpers.MapStateManager

/*
 * Inspiration source (accessed 2019-04-10):
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo
 */
class RideFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mPermissionDenied: Boolean = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.title_ride) // Set toolbar title of parent activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)

        childFragmentManager.beginTransaction()
                            .replace(R.id.map, mapFragment)
                            .commit()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride, container, false)
    }

    override fun onResume() {
        super.onResume()

        // Permission was not granted, display error dialog
        if (mPermissionDenied)
            PermissionDeniedDialog.newInstance(true).show(childFragmentManager, "dialog")
    }

    override fun onPause() {
        super.onPause()

        if (mMap != null) MapStateManager.saveMapState(mMap!!, context!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setMaxZoomPreference(25.0f)

        enableLocation()

        val lastPosition = MapStateManager.getSavedMapState(context!!)
        val update = CameraUpdateFactory.newCameraPosition(lastPosition)
        mMap?.moveCamera(update)
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // Permission to access the location is missing
            requestPermission(LOCATION_PERMISSION_REQUEST_CODE, ACCESS_FINE_LOCATION, finishActivity = true)
        else
            // Access to the location has been granted to the app
            mMap?.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (isPermissionGranted(permissions, grantResults, ACCESS_FINE_LOCATION))
            enableLocation()
        else
            // Display the missing permission error dialog when this fragment resumes
            mPermissionDenied = true
    }

    private fun isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray, permission: String): Boolean {
        grantPermissions.indices.forEach { i ->
            if (permission == grantPermissions[i]) return grantResults[i] == PackageManager.PERMISSION_GRANTED
        }

        return false
    }

    private fun requestPermission(requestId: Int, permission: String, finishActivity: Boolean) {
        if (shouldShowRequestPermissionRationale(permission))
            // Display a dialog with rationale
            RationaleDialog.newInstance(requestId, finishActivity).show(childFragmentManager, "dialog")
        else
            // Location permission has not been granted yet, request it
            requestPermissions(arrayOf(permission), requestId)
    }


    class RationaleDialog : DialogFragment() {

        private var mFinishActivity = false

        companion object {
            private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
                val arguments = Bundle()
                arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)

                val dialog = RationaleDialog()
                dialog.arguments = arguments

                return dialog
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val requestCode = arguments!!.getInt(ARGUMENT_PERMISSION_REQUEST_CODE)
            mFinishActivity = arguments!!.getBoolean(ARGUMENT_FINISH_ACTIVITY)

            return AlertDialog.Builder(activity!!)
                .setMessage(R.string.permission_rationale_location)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // After click on OK, request the permission
                    parentFragment?.requestPermissions(arrayOf(ACCESS_FINE_LOCATION), requestCode)
                    // Do not finish the Activity while requesting permission
                    mFinishActivity = false
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)

            if (mFinishActivity) {
                Toast.makeText(activity, R.string.permission_required_toast, Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }


    class PermissionDeniedDialog : DialogFragment() {

        private var mFinishActivity = false

        companion object {
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
                val arguments = Bundle()
                arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)

                val dialog = PermissionDeniedDialog()
                dialog.arguments = arguments

                return dialog
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            mFinishActivity = arguments!!.getBoolean(ARGUMENT_FINISH_ACTIVITY)

            return AlertDialog.Builder(activity!!)
                              .setMessage(R.string.location_permission_denied)
                              .setPositiveButton(android.R.string.ok, null)
                              .create()
        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)

            if (mFinishActivity) {
                Toast.makeText(activity, R.string.permission_required_toast, Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }
}
