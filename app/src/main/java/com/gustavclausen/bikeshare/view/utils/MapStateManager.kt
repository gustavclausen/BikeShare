package com.gustavclausen.bikeshare.view.utils

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

// Source: https://stackoverflow.com/questions/34636722/android-saving-map-state-in-google-map (accessed 2019-04-10)
class MapStateManager {
    companion object {
        private const val LONGITUDE = "longitude"
        private const val LATITUDE  = "latitude"
        private const val ZOOM      = "zoom"
        private const val BEARING   = "bearing"
        private const val TILT      = "tilt"

        private const val PREFS_NAME = "mapCameraState"

        fun saveMapState(map: GoogleMap, context: Context) {
            val mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = mapStatePrefs.edit()

            val cameraPosition = map.cameraPosition
            editor.putFloat(LATITUDE, cameraPosition.target.latitude.toFloat())
            editor.putFloat(LONGITUDE, cameraPosition.target.longitude.toFloat())
            editor.putFloat(ZOOM, cameraPosition.zoom)
            editor.putFloat(TILT, cameraPosition.tilt)
            editor.putFloat(BEARING, cameraPosition.bearing)
            editor.apply()
        }

        fun getSavedMapState(context: Context): CameraPosition {
            val mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val latitude = mapStatePrefs.getFloat(LATITUDE, 0f)
            val longitude = mapStatePrefs.getFloat(LONGITUDE, 0f)
            val target = LatLng(latitude.toDouble(), longitude.toDouble())

            val zoom = mapStatePrefs.getFloat(ZOOM, 0f)
            val bearing = mapStatePrefs.getFloat(BEARING, 0f)
            val tilt = mapStatePrefs.getFloat(TILT, 0f)

            return CameraPosition(target, zoom, tilt, bearing)
        }
    }
}