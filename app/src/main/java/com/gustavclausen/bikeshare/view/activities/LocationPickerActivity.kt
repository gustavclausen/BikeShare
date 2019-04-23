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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.data.entities.Coordinate

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mStartLocation: Coordinate
    private var mEndLocationMarker: Coordinate? = null

    companion object {
        const val ARG_START_LOCATION = "com.gustavclausen.bikeshare.picker_start_location"
        const val EXTRA_END_LOCATION = "com.gustavclausen.bikeshare.picker_end_location"
        const val SAVED_END_LOCATION_MARKER = "endLocationMarker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        title = getString(R.string.location_picker_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Displays the "back"-button in the action bar

        if (savedInstanceState != null) {
            mEndLocationMarker = savedInstanceState.getSerializable(SAVED_END_LOCATION_MARKER) as Coordinate
        }

        mStartLocation = intent.getSerializableExtra(ARG_START_LOCATION) as Coordinate

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_location_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.pick_location_button -> {
                sendResult()
                true
            }
            /*
             * Finishes the activity and navigates the user back
             * to the activity that started this activity.
             */
            android.R.id.home -> {
                finish()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putSerializable(SAVED_END_LOCATION_MARKER, mEndLocationMarker)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)

        mapMarkers()
        val start = LatLng(mStartLocation.latitude, mStartLocation.longitude)
        mMap.addMarker(MarkerOptions().position(start))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f))
    }

    override fun onMapClick(position: LatLng?) {
        position ?: return

        mEndLocationMarker = Coordinate(position.latitude, position.longitude)
        mapMarkers()
    }

    private fun mapMarkers() {
        mMap.clear()
        // Mark start location
        mMap.addMarker(MarkerOptions().position(LatLng(mStartLocation.latitude, mStartLocation.longitude)))

        // Mark end location if set
        if (mEndLocationMarker != null) {
            mMap.addMarker(MarkerOptions().position(LatLng(mEndLocationMarker!!.latitude, mEndLocationMarker!!.longitude)))
        }
    }

    private fun sendResult() {
        // User tries to submit without selecting end location on map
        if (mEndLocationMarker == null) {
            Toast.makeText(this, getString(R.string.no_location_picked), Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_END_LOCATION, mEndLocationMarker)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
