package com.gustavclausen.bikeshare.viewmodels

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.gustavclausen.bikeshare.BikeShareApplication
import com.gustavclausen.bikeshare.data.dao.BikeDao
import com.gustavclausen.bikeshare.data.dao.RideDao
import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.Coordinate
import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm

class BikeViewModel : ViewModel() {

    private val realm = Realm.getDefaultInstance()
    private val bikeDao = BikeDao(realm)
    private val rideDao = RideDao(realm)

    val allBikes = bikeDao.findAllAsync()
    val availableBikes = bikeDao.findAllAvailableBikesAsync()

    fun getById(lockId: String): Bike? {
        return bikeDao.findById(lockId)
    }

    fun create(
        lockId: String,
        type: String,
        priceHour: Int,
        picture: ByteArray?,
        owner: User,
        position: Coordinate,
        positionAddress: String
    ) {
        bikeDao.create(lockId, type, priceHour, picture, owner, position, positionAddress)
    }

    fun updateAvailability(lockId: String, inUse: Boolean) {
        bikeDao.updateAvailability(lockId, inUse)
    }

    fun updatePosition(lockId: String, position: Coordinate, positionAddress: String) {
        bikeDao.updatePosition(lockId, position, positionAddress)
    }

    /**
     * Deletes bike, all its rides and unclear last ride set if any
     */
    fun delete(lockId: String, context: Context) {
        rideDao.deleteAllRidesForBike(lockId) // Delete all rides
        bikeDao.delete(lockId) // Delete bike

        // Remove on-going ride if set
        val userPreferences = context.getSharedPreferences(
            BikeShareApplication.PREF_USER_FILE,
            Context.MODE_PRIVATE
        )
        val editor = userPreferences.edit()
        editor.putString(BikeShareApplication.PREF_LAST_RIDE_ID, null)
        editor.apply()
    }

    override fun onCleared() {
        super.onCleared()

        allBikes.removeAllChangeListeners()
        availableBikes.removeAllChangeListeners()
        realm.close()
    }
}
