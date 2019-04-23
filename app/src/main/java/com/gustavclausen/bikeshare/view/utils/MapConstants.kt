package com.gustavclausen.bikeshare.view.utils

import com.google.android.gms.maps.model.LatLng

object MapConstants {
    const val minZoomPreference: Float = 11f
    const val maxZoomPreference: Float = 35.0f
    const val markerZoomLevel: Float = 18f
    val mapBoundsTop: LatLng = LatLng(55.711809, 12.468891)
    val mapBoundsBottom: LatLng = LatLng(55.557473, 12.682431)
}