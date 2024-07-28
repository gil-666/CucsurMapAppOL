package com.example.cucsurmapol
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _info = MutableLiveData<String>()
    private val _edificioid = MutableLiveData<String>()
    private val _dbLoaded = MutableLiveData<Boolean>()
    var webView: WebView? = null
    var dbLoaded = MutableLiveData<Boolean>(false)
    val info: LiveData<String> get() = _info
    val edificioid: LiveData<String> get() = _edificioid

    fun setSalonid(newInfo: String) {
        _info.postValue(newInfo)
    }

    fun setEdificioid(id:String){
        _edificioid.postValue(id)
    }
}