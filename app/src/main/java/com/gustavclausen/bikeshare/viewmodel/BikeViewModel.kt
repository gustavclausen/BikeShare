package com.gustavclausen.bikeshare.viewmodel

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.dao.BikeDao
import com.gustavclausen.bikeshare.entities.Bike
import com.gustavclausen.bikeshare.entities.User
import com.gustavclausen.bikeshare.models.Coordinate
import io.realm.Realm

class BikeViewModel : ViewModel() {
    private val realm = Realm.getDefaultInstance()
    private val dao = BikeDao(realm)

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
        realm.close()
    }
}
