package com.gustavclausen.bikeshare.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.gustavclausen.bikeshare.dao.BikeDao
import io.realm.Realm

class BikeViewModel(lockId: String) : ViewModel() {
    private val realm = Realm.getDefaultInstance()
    private val dao = BikeDao(realm)

    val bike = dao.findByIdAsync(lockId)

    override fun onCleared() {
        bike.removeAllChangeListeners()
        realm.close()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val lockId: String) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeViewModel(lockId) as T
        }
    }
}
