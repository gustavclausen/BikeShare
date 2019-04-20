package com.gustavclausen.bikeshare.view.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

    companion object {
        const val ARG_START_LOCATION = "com.gustavclausen.bikeshare.picker_start_location"
        const val EXTRA_END_LOCATION = "com.gustavclausen.bikeshare.picker_end_location"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        mStartLocation = intent.getSerializableExtra(ARG_START_LOCATION) as Coordinate

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)

        // Mark start location
        val start = LatLng(mStartLocation.lat, mStartLocation.long)
        mMap.addMarker(MarkerOptions().position(start))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f))
    }

    override fun onMapClick(p0: LatLng?) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0!!))
    }

    private fun sendResult(resultCode: Int, endPosition: Coordinate) {
        val intent = Intent()
        intent.putExtra(EXTRA_END_LOCATION, endPosition)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
