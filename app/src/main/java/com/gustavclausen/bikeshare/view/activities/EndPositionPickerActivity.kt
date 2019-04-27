package com.gustavclausen.bikeshare.view.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.data.entities.Coordinate
import com.gustavclausen.bikeshare.utils.InternetConnectionUtils
import com.gustavclausen.bikeshare.view.dialogs.InfoDialog
import com.gustavclausen.bikeshare.view.utils.MapConstants

class EndPositionPickerActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mStartPosition: Coordinate
    private var mEndPositionMarker: Coordinate? = null

    companion object {
        const val EXTRA_START_POSITION = "com.gustavclausen.bikeshare.start_position_picker"
        const val EXTRA_END_POSITION = "com.gustavclausen.bikeshare.end_position_picker"

        const val SAVED_END_POSITION_MARKER = "endPositionMarker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        title = getString(R.string.title_end_position_picker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Displays the "back"-button in the action bar

        if (savedInstanceState != null) {
            mEndPositionMarker = savedInstanceState.getSerializable(SAVED_END_POSITION_MARKER) as Coordinate?
        }

        mStartPosition = intent.getSerializableExtra(EXTRA_START_POSITION) as Coordinate

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_end_position_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.pick_location_button -> {
                sendEndPosition()
                true
            }
            android.R.id.home -> {
                // Finishes the activity and navigates the user back to the activity that started this activity
                finish()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if device has available internet connection
        if (!InternetConnectionUtils.isConnected(this)) {
            // Device is not connected to the Internet, display error dialog
            InfoDialog.newInstance(
                dialogText = getString(R.string.internet_connection_required),
                finishActivity = true,
                finishActivityToastText = getString(R.string.internet_connection_required_toast)
            ).show(supportFragmentManager, "dialog")

            return
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putSerializable(SAVED_END_POSITION_MARKER, mEndPositionMarker)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)
        mMap.setMinZoomPreference(MapConstants.minZoomPreference)
        mMap.setMaxZoomPreference(MapConstants.maxZoomPreference)

        // Set bounds on map
        val builder = LatLngBounds.builder()
        builder.include(MapConstants.mapBoundsTop)
        builder.include(MapConstants.mapBoundsBottom)

        val bounds = builder.build()
        mMap.setLatLngBoundsForCameraTarget(bounds)

        mapPositionMarkers()

        // Move camera to start position
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(mStartPosition.latitude, mStartPosition.longitude),
                15f
            )
        )
    }

    override fun onMapClick(position: LatLng?) {
        position ?: return

        mEndPositionMarker = Coordinate(position.latitude, position.longitude)
        mapPositionMarkers()
    }

    private fun mapPositionMarkers() {
        mMap.clear()

        // Mark start location
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(mStartPosition.latitude, mStartPosition.longitude))
                .title(getString(R.string.marker_start_position_title))
                .icon(MapConstants.startPositionMarkerColor)
        ).showInfoWindow()

        // Mark end location if set
        if (mEndPositionMarker != null) {
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(mEndPositionMarker!!.latitude, mEndPositionMarker!!.longitude))
                    .title(getString(R.string.marker_end_position_title))
                    .icon(MapConstants.endPositionMarkerColor)
            ).showInfoWindow()
        }
    }

    private fun sendEndPosition() {
        // User tries to submit without selecting end location on map
        if (mEndPositionMarker == null) {
            Toast.makeText(this, getString(R.string.no_end_position_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_END_POSITION, mEndPositionMarker)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
