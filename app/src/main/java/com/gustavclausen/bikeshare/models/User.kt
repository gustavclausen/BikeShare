package com.gustavclausen.bikeshare.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class User : RealmObject() {
    @PrimaryKey @Required var id: String = ""
    @Required var fullName: String = ""
    var accountBalance: Double = 0.0

    override fun toString(): String {
        return fullName
    }
}