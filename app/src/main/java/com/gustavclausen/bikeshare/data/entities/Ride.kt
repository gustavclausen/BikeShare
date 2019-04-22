package com.gustavclausen.bikeshare.data.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Ride : RealmObject() {
    @PrimaryKey
    @Required
    var id: String = ""
    var bike: Bike? = null
    var rider: User? = null
    var startPositionLat: Double = 0.0
    var startPositionLong: Double = 0.0
    var startPositionAddress: String = ""
    var startTime: Date = Date()
    var endPositionLat: Double = 0.0
    var endPositionLong: Double = 0.0
    var endPositionAddress: String = ""
    var endTime: Date = Date()
    var isEnded: Boolean = false
    var distanceKm: Double = 0.0
    var finalPrice: Double = 0.0

    object Fields {
        const val ID: String = "id"
        const val IS_ENDED: String = "isEnded"
        const val BIKE: String = "bike"
    }
}