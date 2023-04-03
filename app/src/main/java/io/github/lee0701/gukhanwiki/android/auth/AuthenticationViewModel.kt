package io.github.lee0701.gukhanwiki.android.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationViewModel: ViewModel() {

    private val _signedInAccount = MutableLiveData<AccountHelper.SignedInAccount>()
    val signedInAccount: LiveData<AccountHelper.SignedInAccount> = _signedInAccount

    private val _alert = MutableLiveData<String?>()
    val alert: LiveData<String?> = _alert

    fun signIn(username: String, password: String) {
        viewModelScope.launch((Dispatchers.IO)) {
            val response = AccountHelper.signIn(username, password)
            when(response) {
                is Result.Loading -> {}
                is Result.Error -> {
                    _alert.postValue(response.exception.message)
                }
                is Result.Loaded -> {
                    response.data?.let { _signedInAccount.postValue(it) }
                    if(response.data == null) _alert.postValue("null")
                }
            }
        }
    }
}