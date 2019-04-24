package com.gustavclausen.bikeshare.data.dao

import com.gustavclausen.bikeshare.data.entities.Bike
import com.gustavclausen.bikeshare.data.entities.User
import com.gustavclausen.bikeshare.data.entities.Coordinate
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults

class BikeDao(val realm: Realm) {

    fun create(
        lockId: String,
        type: String,
        priceHour: Int,
        picture: ByteArray?,
        owner: User,
        position: Coordinate,
        positionAddress: String
    ) {
        realm.executeTransaction { realm ->
            val bike = realm.createObject(Bike::class.java, lockId)
            bike.type = type
            bike.priceHour = priceHour
            bike.picture = picture
            bike.owner = owner
            bike.positionLatitude = position.latitude
            bike.positionLongitude = position.longitude
            bike.positionAddress = positionAddress
        }
    }

    fun findById(lockId: String): Bike? {
        return whereQuery().equalTo(Bike.Fields.LOCK_ID, lockId).findFirst()
    }

    fun findAllAsync(): RealmResults<Bike> {
        return whereQuery().findAllAsync()
    }

    fun findAllAvailableBikesAsync(): RealmResults<Bike> {
        return whereQuery().equalTo(Bike.Fields.IN_USE, false).findAllAsync()
    }

    fun updateAvailability(lockId: String, inUse: Boolean) {
        realm.executeTransaction {
            val bike = findById(lockId) ?: return@executeTransaction
            bike.inUse = inUse
        }
    }

    fun updatePosition(lockId: String, position: Coordinate, positionAddress: String) {
        realm.executeTransaction {
            val bike = findById(lockId) ?: return@executeTransaction
            bike.positionLatitude = position.latitude
            bike.positionLongitude = position.longitude
            bike.positionAddress = positionAddress
        }
    }

    fun delete(lockId: String) {
        realm.executeTransaction {
            whereQuery().equalTo(Bike.Fields.LOCK_ID, lockId).findAll().deleteAllFromRealm()
        }
    }

    private fun whereQuery(): RealmQuery<Bike> {
        return realm.where(Bike::class.java)
    }
}