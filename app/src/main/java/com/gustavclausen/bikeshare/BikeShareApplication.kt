package com.gustavclausen.bikeshare

import android.app.Application
import com.gustavclausen.bikeshare.data.DatabaseInitTransaction
import io.realm.Realm
import io.realm.RealmConfiguration

class BikeShareApplication : Application() {

    companion object {
        const val PREF_USER_FILE = "user"
        const val PREF_USER_ID = "user_id"
        const val PREF_LAST_RIDE_ID = "last_ride_id"
    }

    override fun onCreate() {
        super.onCreate()

        // Realm configuration
        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .name("bikeshare.realm")
                .deleteRealmIfMigrationNeeded()
                .initialData(DatabaseInitTransaction())
                .build()
        )
    }
}