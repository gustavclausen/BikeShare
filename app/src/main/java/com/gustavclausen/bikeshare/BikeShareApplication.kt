package com.gustavclausen.bikeshare

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class BikeShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .name("bikeshare.realm")
                .deleteRealmIfMigrationNeeded()
                .build()
        )
    }
}