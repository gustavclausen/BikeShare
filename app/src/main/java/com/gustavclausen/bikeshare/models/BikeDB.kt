package com.gustavclausen.bikeshare.models

import android.content.Context
import io.realm.Realm

class BikeDB private constructor() {

    companion object {
        private const val BIKE_TYPES_ASSETS_PATH = "bike_types.txt"

        private val db = BikeDB()

        fun get(): BikeDB = db
    }

    fun addBike(
        lockId: String,
        type: String,
        priceHour: Int,
        picture: ByteArray?,
        owner: User,
        lastKnownPosition: Coordinate,
        locationAddress: String
    ) {
        Realm.getDefaultInstance().executeTransaction { realm ->
            val bike = realm.createObject(Bike::class.java, lockId)
            bike.type = type
            bike.priceHour = priceHour
            bike.picture = picture
            bike.owner = owner
            bike.lastKnownPositionLat = lastKnownPosition.lat
            bike.lastKnownPositionLong = lastKnownPosition.long
            bike.lastLocationAddress = locationAddress
        }
    }

    fun getAllBikes(): List<Bike> =
        Realm.getDefaultInstance().where(Bike::class.java).findAll().toList()

    fun getBike(lockId: String): Bike? =
        Realm.getDefaultInstance().where(Bike::class.java).equalTo("lockId", lockId).findFirst()

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