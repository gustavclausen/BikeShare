package com.gustavclausen.bikeshare.data.dao

import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.User
import com.gustavclausen.bikeshare.data.entities.Coordinate
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults

class BikeDao(val realm: Realm) {

    private fun where(): RealmQuery<Bike> {
        return realm.where(Bike::class.java)
    }

    fun findAllAsync(): RealmResults<Bike> {
        return where().findAllAsync()
    }

    fun findById(lockId: String): Bike? {
        return where().equalTo(Bike.Fields.LOCK_ID, lockId).findFirst()
    }

    fun findAllAvailableBikesAsync(): List<Bike> {
        return where().equalTo(Bike.Fields.IN_USE, false).findAllAsync()
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
        realm.executeTransaction { realm ->
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


    fun updateAvailability(lockId: String, inUse: Boolean) {
        realm.executeTransaction {
            val bike = findById(lockId) ?: return@executeTransaction
            bike.inUse = inUse
        }
    }

    fun updateLastKnownLocation(lockId: String, lastKnownPosition: Coordinate, lastKnownAddress: String) {
        realm.executeTransaction {
            val bike = findById(lockId) ?: return@executeTransaction
            bike.lastKnownPositionLat = lastKnownPosition.lat
            bike.lastKnownPositionLong = lastKnownPosition.long
            bike.lastLocationAddress = lastKnownAddress
        }
    }
}