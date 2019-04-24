package com.gustavclausen.bikeshare.viewmodels

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.data.dao.RideDao
import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm
import io.realm.RealmResults
import java.util.*

class RideViewModel : ViewModel() {

    private val realm = Realm.getDefaultInstance()
    private val dao = RideDao(realm)

    val endedRides = dao.findAllEndedRidesAsync()

    /**
     * Create to start ride
     *
     * Returns id of newly created user
     */
    fun startRide(
        bike: Bike,
        rider: User,
        startPositionLatitude: Double,
        startPositionLongitude: Double,
        startPositionAddress: String,
        startTime: Date = Date()
    ): String {
        return dao.create(bike, rider, startPositionLatitude, startPositionLongitude, startPositionAddress, startTime)
    }

    fun getById(id: String): Ride? {
        return dao.findById(id)
    }

    fun getAllRidesForBike(bikeLockId: String): RealmResults<Ride> {
        return dao.findAllEndedRidesForBikeAsync(bikeLockId)
    }

    // Update to end ride
    fun endRide(
        id: String,
        endPositionLatitude: Double,
        endPositionLongitude: Double,
        endPositionAddress: String,
        distanceKm: Double,
        finalPrice: Double,
        endTime: Date = Date()
    ) {
        dao.update(id, endPositionLatitude, endPositionLongitude, endPositionAddress, distanceKm, finalPrice, endTime)
    }

    fun deleteRide(id: String) {
        dao.delete(id)
    }

    override fun onCleared() {
        super.onCleared()

        endedRides.removeAllChangeListeners()
        realm.close()
    }
}