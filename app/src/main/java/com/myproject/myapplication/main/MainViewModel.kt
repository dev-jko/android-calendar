package com.myproject.myapplication.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _itemIndex = MutableLiveData(0)

    val itemIndex: LiveData<Int> = _itemIndex


    fun setItemIndex(index: Int) {
        _itemIndex.postValue(index)
    }


    fun getIntSharedPreference(context: Context, name: String, key: String, defValue: Int = 0): Int {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
            .getInt(key, defValue)
    }

    fun getInitPagerItem(context: Context): Int {
        return getIntSharedPreference(context, "settings", "pagerItem")
    }

    fun saveIntSharedPreferences(context: Context, name: String, key: String, value: Int) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
            .putInt(key, value)
            .apply()
    }

    fun savePagerItem(context: Context) {
        saveIntSharedPreferences(context, "setting", "pagerItem", _itemIndex.value ?: 0)
    }


}