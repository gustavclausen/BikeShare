package com.gustavclausen.bikeshare.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.gustavclausen.bikeshare.dialogs.InfoDialog
import com.gustavclausen.bikeshare.utils.PermissionUtils
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.activities.BikeDetailActivity
import com.gustavclausen.bikeshare.models.BikeDB
import com.gustavclausen.bikeshare.utils.MapStateManager

/*
 * Inspiration source (accessed 2019-04-10):
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo
 */
class RideFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mLocationPermissionDenied: Boolean = false
    private var mClickedItem: ClusterMarkerLocation? = null

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

        childFragmentManager
            .beginTransaction()
            .replace(R.id.map, mapFragment)
            .commit()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride, container, false)
    }

    override fun onResume() {
        super.onResume()

        if (mLocationPermissionDenied) {
            // Location permission was not granted, display error dialog
            InfoDialog.newInstance(
                dialogText = getString(R.string.location_permission_denied),
                finishActivity = true,
                finishActivityToastText = getString(R.string.permission_required_toast)
            ).show(childFragmentManager, "dialog")
        }
    }

    override fun onPause() {
        super.onPause()

        if (::mMap.isInitialized)
            MapStateManager.saveMapState(mMap, context!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMaxZoomPreference(25.0f)

        enableLocation()

        val lastPosition = MapStateManager.getSavedMapState(context!!)
        val update = CameraUpdateFactory.newCameraPosition(lastPosition)
        mMap.moveCamera(update)

        mapAvailableBikes()
    }

    private fun mapAvailableBikes() {
        val clusterManager = ClusterManager<ClusterMarkerLocation>(context, mMap)
        clusterManager.renderer = BikeIconRendered(context!!, mMap, clusterManager)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        clusterManager.setOnClusterItemClickListener { clusterItem ->
            mClickedItem = clusterItem

            // Move camera to clicked item
            val itemPosition = LatLng(clusterItem.position.latitude, clusterItem.position.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(itemPosition, 18f))

            val bikeOptionsMenu = layoutInflater.inflate(R.layout.menu_bike_options, null)
            bikeOptionsMenu.findViewById<Button>(R.id.show_bike_details_button).setOnClickListener {
                startActivity(BikeDetailActivity.newIntent(context!!, clusterItem.bikeLockId))
            }
            val dialog = AlertDialog.Builder(context).create()
            dialog.setView(bikeOptionsMenu)

            dialog.show()

            true
        }

        BikeDB.get().getAllBikes().forEach { bike ->
            val item = ClusterMarkerLocation(LatLng(bike.lastKnownPositionLat, bike.lastKnownPositionLong), bike.lockId)
            clusterManager.addItem(item)
        }
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission to access the location is missing
            PermissionUtils.requestPermission(
                permission = ACCESS_FINE_LOCATION,
                requestId = LOCATION_PERMISSION_REQUEST_CODE,
                rationaleText = getString(R.string.permission_rationale_location),
                finishActivity = true,
                dismissText = getString(R.string.permission_required_toast),
                fragment = this
            )
        } else {
            /*
             * Access to the location has been granted to the app.
             * Now check if location service is available, and start location on map if it is
             */
            checkLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1 // Only request once

        // Check whether location settings on device are satisfied
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(context!!)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener {
            // Location service is enabled, thus enable location on map
            mMap.isMyLocationEnabled = true
        }.addOnFailureListener {
            // Location service is not available, show error dialog and quit Activity afterwards
            InfoDialog.newInstance(
                dialogText = getString(R.string.location_service_error),
                finishActivity = true,
                finishActivityToastText = getString(R.string.location_service_required_toast)
            ).show(childFragmentManager, "dialog")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, ACCESS_FINE_LOCATION))
            enableLocation()
        else
            // Set variable to display the missing permission error dialog when this fragment resumes
            mLocationPermissionDenied = true
    }

    class ClusterMarkerLocation(private val coordinate: LatLng, val bikeLockId: String) : ClusterItem {
        override fun getSnippet(): String {
            return bikeLockId
        }

        override fun getTitle(): String {
            return bikeLockId
        }

        override fun getPosition(): LatLng {
            return coordinate
        }
    }

    class BikeIconRendered(private val context: Context, map: GoogleMap, clusterManager: ClusterManager<ClusterMarkerLocation>) :
        DefaultClusterRenderer<ClusterMarkerLocation>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: ClusterMarkerLocation?, markerOptions: MarkerOptions?) {
            markerOptions?.icon(bitmapDescriptorFromVector(context, R.drawable.ic_bike_location_marker))
        }

        private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
            val scaleFactor = 2
            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth * scaleFactor, vectorDrawable.intrinsicHeight * scaleFactor)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth * scaleFactor, vectorDrawable.intrinsicHeight * scaleFactor, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}
