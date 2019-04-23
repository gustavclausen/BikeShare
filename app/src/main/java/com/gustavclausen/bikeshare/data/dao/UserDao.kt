package com.gustavclausen.bikeshare.data.dao

import com.gustavclausen.bikeshare.data.entities.User
import io.realm.Realm
import io.realm.RealmQuery
import java.util.*

class UserDao(val realm: Realm) {

    /**
     * Returns id of newly created user
     */
    fun addUserAsync(fullName: String): String {
        val newUserId: String = UUID.randomUUID().toString()

        realm.executeTransactionAsync { realm ->
            val user = realm.createObject(User::class.java, newUserId)
            user.fullName = fullName
        }

        return newUserId
    }

    fun findById(userId: String): User? {
        return whereQuery().equalTo(User.Fields.ID, userId).findFirst()
    }

    fun addToBalance(userId: String, amount: Double) {
        realm.executeTransaction {
            val user = findById(userId) ?: return@executeTransaction
            user.accountBalance += amount
        }
    }

    fun subtractFromBalance(userId: String, amount: Double) {
        realm.executeTransaction {
            val user = findById(userId) ?: return@executeTransaction
            user.accountBalance -= amount
        }
    }

    private fun whereQuery(): RealmQuery<User> {
        return realm.where(User::class.java)
    }
}