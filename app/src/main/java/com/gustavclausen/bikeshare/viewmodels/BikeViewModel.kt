package com.gustavclausen.bikeshare.viewmodels

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.data.dao.BikeDao
import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.User
import com.gustavclausen.bikeshare.data.entities.Coordinate
import io.realm.Realm

class BikeViewModel : ViewModel() {
    private val realm = Realm.getDefaultInstance()
    private val dao = BikeDao(realm)

    val allBikes = dao.findAllAsync()
    val availableBikes = dao.findAllAvailableBikes()

    fun getById(lockId: String): Bike? {
        return dao.findById(lockId)
    }

    fun create(
        lockId: String,
        type: String,
        priceHour: Int,
        picture: ByteArray?,
        owner: User,
        lastKnownPosition: Coordinate,
        locationAddress: String
    ) {
        dao.create(lockId, type, priceHour, picture, owner, lastKnownPosition, locationAddress)
    }

    override fun onCleared() {
        super.onCleared()

        allBikes.removeAllChangeListeners()
        realm.close()
    }
}
