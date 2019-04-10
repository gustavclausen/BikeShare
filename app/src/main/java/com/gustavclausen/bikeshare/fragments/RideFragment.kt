package com.gustavclausen.bikeshare.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.gustavclausen.bikeshare.helpers.MapStateManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.gustavclausen.bikeshare.R

class RideFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

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

    override fun onPause() {
        super.onPause()
        MapStateManager.saveMapState(mMap, context!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMaxZoomPreference(25.0f)

        val lastPosition = MapStateManager.getSavedMapState(context!!)
        val update = CameraUpdateFactory.newCameraPosition(lastPosition)
        mMap.moveCamera(update)
    }
}
