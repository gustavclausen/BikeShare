package com.gustavclausen.bikeshare.view.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
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
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.utils.InternetConnectionUtils
import com.gustavclausen.bikeshare.utils.PermissionUtils
import com.gustavclausen.bikeshare.view.activities.BikeDetailActivity
import com.gustavclausen.bikeshare.view.activities.BikeShareActivity
import com.gustavclausen.bikeshare.view.dialogs.InfoDialog
import com.gustavclausen.bikeshare.view.utils.MapConstants
import com.gustavclausen.bikeshare.view.utils.MapStateManager
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import com.gustavclausen.bikeshare.viewmodels.UserViewModel

/*
 * Inspiration source (accessed 2019-04-10):
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo
 */
class BikeMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<ClusterMarkerLocation>
    private var mSelectedBikeLockId: String? = null
    private var mLocationPermissionDenied: Boolean = false
    private var mOptionsMenuDialog: AlertDialog? = null
    private var mOptionsMenuIsShowing: Boolean = false

    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mRideVM: RideViewModel
    private lateinit var mUserVM: UserViewModel

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val SAVED_OPTIONS_MENU_IS_SHOWING = "savedOptionsMenuIsShowing"
        private const val SAVED_SELECTED_BIKE_LOCK_ID = "savedSelectedBikeLockId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mOptionsMenuIsShowing = savedInstanceState.getBoolean(SAVED_OPTIONS_MENU_IS_SHOWING)
            mSelectedBikeLockId = savedInstanceState.getString(SAVED_SELECTED_BIKE_LOCK_ID)
        }

        activity?.title = getString(R.string.title_bike_map) // Set toolbar title of parent activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)

        childFragmentManager
            .beginTransaction()
            .replace(R.id.map, mapFragment)
            .commit()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bike_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBikeVM = ViewModelProviders.of(this).get(BikeViewModel::class.java)
        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
        mUserVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
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

            return
        }

        // Check if device has available internet connection
        if (!InternetConnectionUtils.isConnected(context!!)) {
            // Device is not connected to the Internet, display error dialog
            InfoDialog.newInstance(
                dialogText = getString(R.string.internet_connection_required_message),
                finishActivity = true,
                finishActivityToastText = getString(R.string.internet_connection_required_toast)
            ).show(childFragmentManager, "dialog")
        }

        if (mOptionsMenuIsShowing)
            showOptionsMenu()
    }

    override fun onPause() {
        super.onPause()

        mOptionsMenuDialog?.dismiss()

        if (::mMap.isInitialized)
            MapStateManager.saveMapState(mMap, context!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SAVED_OPTIONS_MENU_IS_SHOWING, mOptionsMenuIsShowing)
        outState.putString(SAVED_SELECTED_BIKE_LOCK_ID, mSelectedBikeLockId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, ACCESS_FINE_LOCATION)) {
            enableLocation()
        } else {
            // Set variable to display the missing permission error dialog when this fragment resumes
            mLocationPermissionDenied = true
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
            requestLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(MapConstants.minZoomPreference)
        mMap.setMaxZoomPreference(MapConstants.maxZoomPreference)

        enableLocation()

        // Cluster manager used to group adjacent bikes when it becomes dense on the map
        mClusterManager = ClusterManager(context, mMap)
        val renderer = CustomClusterRenderer(context!!, mMap, mClusterManager)
        renderer.minClusterSize = 2 // Cluster has to consist of at least two bikes
        mClusterManager.renderer = renderer
        mClusterManager.setOnClusterItemClickListener { marker ->
            mSelectedBikeLockId = marker.bikeLockId

            // Move camera to clicked marker
            val markerPosition = LatLng(marker.position.latitude, marker.position.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, MapConstants.markerZoomLevel))
            showOptionsMenu()

            true
        }

        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mMap.setOnCameraMoveListener(renderer)

        // Set bounds on map
        val builder = LatLngBounds.builder()
        builder.include(MapConstants.mapBoundsTop)
        builder.include(MapConstants.mapBoundsBottom)

        val bounds = builder.build()
        mMap.setLatLngBoundsForCameraTarget(bounds)

        // Move to last saved camera location
        val lastPosition = MapStateManager.getSavedMapState(context!!)
        val update = CameraUpdateFactory.newCameraPosition(lastPosition)
        mMap.moveCamera(update)

        markAvailableBikes()
    }

    private fun markAvailableBikes() {
        mBikeVM.availableBikes.forEach { bike ->
            val bikeMarker = ClusterMarkerLocation(
                LatLng(bike.positionLatitude, bike.positionLongitude),
                bike.lockId
            )
            mClusterManager.addItem(bikeMarker)
        }
    }

    private fun showOptionsMenu() {
        mOptionsMenuDialog = AlertDialog.Builder(context).create()
        mOptionsMenuDialog!!.setOnShowListener {
            mOptionsMenuIsShowing = true
        }
        mOptionsMenuDialog!!.setOnDismissListener {
            mOptionsMenuIsShowing = false
        }

        // Show options menu
        val bikeOptionsMenu = layoutInflater.inflate(R.layout.menu_bike_options, null)
        // Show details button
        bikeOptionsMenu.findViewById<Button>(R.id.show_bike_details_button).setOnClickListener {
            // Open detail view of bike when 'Show details'-button is clicked
            startActivity(BikeDetailActivity.newIntent(context!!, mSelectedBikeLockId!!))
        }
        // Unlock ride button
        bikeOptionsMenu.findViewById<Button>(R.id.unlock_ride_button).setOnClickListener {
            // Mock Bluetooth pairing to unlock bike
            Toast.makeText(context!!, getString(R.string.pairing_success), Toast.LENGTH_SHORT).show()
            mOptionsMenuDialog!!.cancel()
            startRideHandling()
        }

        mOptionsMenuDialog!!.setView(bikeOptionsMenu)
        mOptionsMenuDialog!!.show()
    }

    private fun startRideHandling() {
        val bikeShareActivity = (activity as BikeShareActivity)

        // Get current user
        val user = mUserVM.getById(bikeShareActivity.getUserId()!!)!!

        // Create ride
        val selectedBike = mBikeVM.getById(mSelectedBikeLockId!!)!!
        val newRideId = mRideVM.startRide(
            selectedBike,
            user,
            selectedBike.positionLatitude,
            selectedBike.positionLongitude,
            selectedBike.positionAddress
        )

        // Update bike to be in use
        mBikeVM.updateAvailability(selectedBike.lockId, inUse = true)

        // Navigate to fragment where user can end ride
        bikeShareActivity.updateLastRide(newRideId)
        bikeShareActivity.loadRideFragment()
    }


    inner class ClusterMarkerLocation(private val coordinate: LatLng, val bikeLockId: String) : ClusterItem {
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

    inner class CustomClusterRenderer(
        private val context: Context,
        private val map: GoogleMap,
        clusterManager: ClusterManager<ClusterMarkerLocation>
    ) : DefaultClusterRenderer<ClusterMarkerLocation>(context, map, clusterManager), GoogleMap.OnCameraMoveListener {

        private var mCurrentZoomLevel: Float = 0f

        // Set custom icon on marker
        override fun onBeforeClusterItemRendered(item: ClusterMarkerLocation?, markerOptions: MarkerOptions?) {
            markerOptions?.icon(bitmapDescriptorFromVector(context, R.drawable.ic_bike_location_marker))
        }

        override fun onCameraMove() {
            mCurrentZoomLevel = map.cameraPosition.zoom
        }

        /*
         * Convert vector from resources to bitmap for marker to use
         */
        private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
            val scaleFactor = 2

            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
            vectorDrawable.setBounds(
                0,
                0,
                vectorDrawable.intrinsicWidth * scaleFactor,
                vectorDrawable.intrinsicHeight * scaleFactor
            )

            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth * scaleFactor,
                vectorDrawable.intrinsicHeight * scaleFactor,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)

            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarkerLocation>?): Boolean {
            // Determine if cluster will be created
            var wouldCluster = super.shouldRenderAsCluster(cluster)

            // Don't render cluster in super dense area if camera is zoomed far in
            if (wouldCluster) {
                wouldCluster = mCurrentZoomLevel < MapConstants.markerZoomLevel
            }

            return wouldCluster
        }
    }
}
