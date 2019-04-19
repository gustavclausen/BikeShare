package com.gustavclausen.bikeshare.viewmodel

import android.arch.lifecycle.ViewModel
import com.gustavclausen.bikeshare.dao.BikeDao
import io.realm.Realm

class BikeListViewModel : ViewModel() {
    private val realm = Realm.getDefaultInstance()
    private val dao = BikeDao(realm)

    val allBikes = dao.findAllAsync()

    override fun onCleared() {
        allBikes.removeAllChangeListeners()
        realm.close()
    }
}