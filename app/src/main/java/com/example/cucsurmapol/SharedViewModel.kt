package com.example.cucsurmapol
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _info = MutableLiveData<String>()
    val info: LiveData<String> get() = _info

    fun setInfo(newInfo: String) {
        _info.postValue(newInfo)
    }
}