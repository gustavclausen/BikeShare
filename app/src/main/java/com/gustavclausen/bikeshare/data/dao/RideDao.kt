package com.gustavclausen.bikeshare.data.dao

import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import java.util.*

class RideDao(val realm: Realm) {

    /**
     * Create to start ride
     *
     * Returns id of newly created ride
     */
    fun create(
        bike: Bike,
        rider: User,
        startPositionLatitude: Double,
        startPositionLongitude: Double,
        startPositionAddress: String,
        startTime: Date
    ): String {
        val id: String = UUID.randomUUID().toString()

        realm.executeTransaction { realm ->
            val ride = realm.createObject(Ride::class.java, id)
            ride.bike = bike
            ride.rider = rider
            ride.startPositionLatitude = startPositionLatitude
            ride.startPositionLongitude = startPositionLongitude
            ride.startPositionAddress = startPositionAddress
            ride.startTime = startTime
        }

        return id
    }

    fun findById(id: String): Ride? {
        return whereQuery().equalTo(Ride.Fields.ID, id).findFirst()
    }

    fun findAllEndedRidesAsync(): RealmResults<Ride> {
        return whereQuery().equalTo(Ride.Fields.IS_ENDED, true).findAllAsync()
    }

    fun findAllEndedRidesForBikeAsync(bikeLockId: String): RealmResults<Ride> {
        return whereQuery()
            .equalTo(Ride.Fields.IS_ENDED, true)
            .contains("${Ride.Fields.BIKE}.${Bike.Fields.LOCK_ID}", bikeLockId)
            .findAllAsync()
    }

    // Update to end ride
    fun update(
        id: String,
        endPositionLatitude: Double,
        endPositionLongitude: Double,
        endPositionAddress: String,
        distanceKm: Double,
        finalPrice: Double,
        endTime: Date
    ) {
        realm.executeTransaction {
            val ride = findById(id) ?: return@executeTransaction
            ride.isEnded = true
            ride.endPositionLatitude = endPositionLatitude
            ride.endPositionLongitude = endPositionLongitude
            ride.endPositionAddress = endPositionAddress
            ride.distanceKm = distanceKm
            ride.finalPrice = finalPrice
            ride.endTime = endTime
        }
    }

    fun delete(id: String) {
        realm.executeTransaction {
            whereQuery().equalTo(Ride.Fields.ID, id).findAll().deleteAllFromRealm()
        }
    }

    fun deleteAllRidesForBike(bikeLockId: String) {
        realm.executeTransaction {
            whereQuery()
                .contains("${Ride.Fields.BIKE}.${Bike.Fields.LOCK_ID}", bikeLockId)
                .findAll()
                .deleteAllFromRealm()
        }
    }

    private fun whereQuery(): RealmQuery<Ride> {
        return realm.where(Ride::class.java)
    }
}