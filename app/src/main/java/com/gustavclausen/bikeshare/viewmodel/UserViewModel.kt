package com.gustavclausen.bikeshare.viewmodel

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.dao.UserDao
import com.gustavclausen.bikeshare.entities.User
import io.realm.Realm

class UserViewModel : ViewModel() {

    private val realm = Realm.getDefaultInstance()
    private val dao = UserDao(realm)

    /**
     * Returns id of newly created user
     */
    fun create(fullName: String): String {
       return dao.addUserAsync(fullName)
    }

    fun getById(userId: String): User? {
        return dao.findById(userId)
    }

    fun addToBalance(userId: String, amount: Double) {
        dao.addToBalance(userId, amount)
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }
}
