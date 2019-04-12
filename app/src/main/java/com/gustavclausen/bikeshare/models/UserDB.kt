package com.gustavclausen.bikeshare.models

import io.realm.Realm
import java.util.*

class UserDB private constructor() {

    private val mRealm = Realm.getDefaultInstance()!!

    companion object {
        private val db: UserDB = UserDB()

        fun get(): UserDB = db
    }

    fun addUser(fullName: String): String? {
        val newUserId: String? = UUID.randomUUID().toString()

        mRealm.executeTransaction { realm ->
            val user = realm.createObject(User::class.java, newUserId)
            user.fullName = fullName
        }

        return newUserId
    }

    fun getUser(userId: String): User? =
        mRealm.where(User::class.java).equalTo("id", userId).findFirst()

    fun addToBalance(userId: String, amount: Double) {
        mRealm.executeTransaction {
            val user = getUser(userId)
            user!!.accountBalance += amount
        }
    }

    fun substractFromBalance(userId: String, amount: Double) {
        mRealm.executeTransaction {
            val user = getUser(userId)
            user!!.accountBalance -= amount
        }
    }
}