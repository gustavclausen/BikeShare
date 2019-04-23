package com.gustavclausen.bikeshare.data.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Bike : RealmObject() {
    @PrimaryKey
    @Required
    var lockId: String = "" // Identifies a bike
    @Required
    var type: String = ""
    var positionLatitude: Double = 0.0
    var positionLongitude: Double = 0.0
    var positionAddress: String = ""
    var picture: ByteArray? = null
    var priceHour: Int = 0
    var owner: User? = null
    var inUse: Boolean = false

    object Fields {
        const val LOCK_ID: String = "lockId"
        const val IN_USE: String = "inUse"
    }
}