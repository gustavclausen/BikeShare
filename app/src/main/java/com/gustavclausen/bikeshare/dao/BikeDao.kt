package com.gustavclausen.bikeshare.dao

import com.gustavclausen.bikeshare.models.Bike
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

    fun findById(lockId: String): Bike {
        return where().equalTo(Bike.Fields.LOCK_ID, lockId).findFirstAsync()
    }
}