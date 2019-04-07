package com.gustavclausen.bikeshare.models

import android.content.Context

class BikesDB private constructor() {

    companion object {
        private const val BIKE_TYPES_ASSETS_PATH = "bike_types.txt"

        private val db: BikesDB = BikesDB()

        fun get(): BikesDB = db
    }

    fun getBikeTypes(context: Context) : Sequence<String> =
        sequence {
            // Read values from .txt-file found in assets folder
            context.applicationContext.assets.open(BIKE_TYPES_ASSETS_PATH).bufferedReader().useLines { lines ->
                lines.forEach {
                    yield(it)
                }
            }
        }
}