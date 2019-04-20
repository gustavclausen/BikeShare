package com.gustavclausen.bikeshare.viewmodels

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.data.dao.RideDao
import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm
import java.util.*

class RideViewModel : ViewModel() {

    private val realm = Realm.getDefaultInstance()
    private val dao = RideDao(realm)

    val endedRides = dao.findAllEndedRidesAsync()

    fun getById(id: String): Ride? {
        return dao.findById(id)
    }

    // Create to start ride
    fun startRide(
        id: String,
        bike: Bike,
        rider: User,
        startPositionLat: Double,
        startPositionLong: Double,
        startPositionAddress: String,
        startTime: Date = Date()
    ) {
        dao.create(id, bike, rider, startPositionLat, startPositionLong, startPositionAddress, startTime)
    }

    // Update to end ride
    fun endRide(
        id: String,
        endPositionLat: Double,
        endPositionLong: Double,
        endPositionAddress: String,
        distance: Double,
        finalPrice: Double,
        endTime: Date = Date()
    ) {
        dao.update(id, endPositionLat, endPositionLong, endPositionAddress, distance, finalPrice, endTime)
    }

    override fun onCleared() {
        super.onCleared()

        endedRides.removeAllChangeListeners()
        realm.close()
    }
}