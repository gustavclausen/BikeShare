package com.gustavclausen.bikeshare.data.dao

import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import java.util.*

class RideDao(val realm: Realm) {

    private fun where(): RealmQuery<Ride> {
        return realm.where(Ride::class.java)
    }

    fun findAllAsync(): RealmResults<Ride> {
        return where().findAllAsync()
    }

    fun findById(id: String): Ride? {
        return where().equalTo(Ride.Fields.ID, id).findFirst()
    }

    fun findAllEndedRidesAsync(): RealmResults<Ride> {
        return where().equalTo(Ride.Fields.IS_ENDED, true).findAllAsync()
    }

    // Create to start ride
    fun create(
        id: String,
        bike: Bike,
        rider: User,
        startPositionLat: Double,
        startPositionLong: Double,
        startPositionAddress: String,
        startTime: Date
    ) {
        realm.executeTransaction { realm ->
            val ride = realm.createObject(Ride::class.java, id)
            ride.bike = bike
            ride.rider = rider
            ride.startPositionLat = startPositionLat
            ride.startPositionLong = startPositionLong
            ride.startPositionAddress = startPositionAddress
            ride.startTime = startTime
        }
    }

    // Update to end ride
    fun update(
        id: String,
        endPositionLat: Double,
        endPositionLong: Double,
        endPositionAddress: String,
        distance: Double,
        finalPrice: Double,
        endTime: Date
    ) {
        realm.executeTransaction {
            val ride = findById(id) ?: return@executeTransaction
            ride.isEnded = true
            ride.endPositionLat = endPositionLat
            ride.endPositionLong = endPositionLong
            ride.endPositionAddress = endPositionAddress
            ride.distance = distance
            ride.finalPrice = finalPrice
            ride.endTime = endTime
        }
    }
}