package com.gustavclausen.bikeshare

import android.app.Application
import android.content.Context
import com.gustavclausen.bikeshare.models.UserDB
import io.realm.Realm
import io.realm.RealmConfiguration

class BikeShareApplication : Application() {

    companion object {
        const val PREF_USER_FILE = "user"
        const val PREF_USER_ID = "user_id"
    }

    override fun onCreate() {
        super.onCreate()

        // Realm configuration
        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .name("bikeshare.realm")
                .deleteRealmIfMigrationNeeded()
                .build()
        )

        // User account configuration
        val userPreferences = applicationContext.getSharedPreferences(PREF_USER_FILE, Context.MODE_PRIVATE)
        val registeredUserId = userPreferences.getString(PREF_USER_ID, null)

        // Create dummy user account if it is the first time user starts the app
        if (registeredUserId == null) {
            val newUserId = UserDB.get().addUser("Frank Castle")

            val editor = userPreferences.edit()
            editor.putString(PREF_USER_ID, newUserId)
            editor.apply()
        }
    }
}