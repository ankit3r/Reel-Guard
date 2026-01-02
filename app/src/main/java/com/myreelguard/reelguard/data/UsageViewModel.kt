package com.myreelguard.reelguard.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrefsRepository(application)

    private val _reelCount = MutableLiveData<Int>()
    val reelCount: LiveData<Int> = _reelCount

    private val _reelLimit = MutableLiveData<Int>()
    val reelLimitLive: LiveData<Int> = _reelLimit

    val reelLimit: Int
        get() = _reelLimit.value ?: repository.reelLimit

    init {
        loadData()
    }

    fun loadData() {
        _reelCount.value = repository.dailyReelCount
        _reelLimit.value = repository.reelLimit
    }

    fun saveReelLimit(limit: Int) {
        repository.reelLimit = limit
        _reelLimit.value = limit          // trigger observers
    }

    fun resetUsage() {
        repository.resetTodaysUsage()
        loadData()
    }
}
