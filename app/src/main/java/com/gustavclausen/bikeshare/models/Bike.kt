package com.gustavclausen.bikeshare.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Bike : RealmObject() {
    @PrimaryKey @Required var lockId: String = ""
    @Required var type: String = ""
    var lastKnownPositionLat: Double = 0.0
    var lastKnownPositionLong: Double = 0.0
    var lastKnownPositionAddress: String = ""
    var picture: ByteArray? = null
    var priceHour: Int = 0
    var owner: User? = null
    var inUse: Boolean = false
}