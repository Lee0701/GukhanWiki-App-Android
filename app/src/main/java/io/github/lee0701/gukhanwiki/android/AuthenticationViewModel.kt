package io.github.lee0701.gukhanwiki.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthenticationViewModel: ViewModel() {

    private val _signedInAccount = MutableLiveData<AccountHelper.SignedInAccount>()
    val signedInAccount: LiveData<AccountHelper.SignedInAccount> = _signedInAccount

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val loggedInAccount = AccountHelper.signIn(username, password)
            when(loggedInAccount) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {}
                is Loadable.Loaded -> {
                    _signedInAccount.postValue(loggedInAccount.data!!)
                }
            }
        }
    }
}