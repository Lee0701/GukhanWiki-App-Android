package io.github.lee0701.gukhanwiki.android.auth

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationViewModel: ViewModel() {

    private val _signedInAccount = MutableLiveData<AccountHelper.SignedInAccount>()
    val signedInAccount: LiveData<AccountHelper.SignedInAccount> = _signedInAccount

    private val _alert = MutableLiveData<String?>()
    val alert: LiveData<String?> = _alert

    @SuppressLint("NullSafeMutableLiveData")
    fun signIn(username: String, password: String) {
        viewModelScope.launch((Dispatchers.IO)) {
            val response = AccountHelper.signIn(username, password)
            when(response) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {
                    _alert.postValue(response.exception.message)
                }
                is Loadable.Loaded -> {
                    if(response.data != null) _signedInAccount.postValue(response.data)
                    else _alert.postValue("null")
                    _alert.postValue(null)
                }
            }
        }
    }
}