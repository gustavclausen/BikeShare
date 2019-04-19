package com.gustavclausen.bikeshare

import io.realm.Realm

/**
 * Seed data that is loaded into Realm upon first start of application
 */
class DatabaseInitTransaction : Realm.Transaction {

    override fun execute(realm: Realm) {
        realm.deleteAll()
    }
}
