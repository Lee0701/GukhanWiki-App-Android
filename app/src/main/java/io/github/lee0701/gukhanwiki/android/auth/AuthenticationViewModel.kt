package io.github.lee0701.gukhanwiki.android.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import kotlinx.coroutines.launch

class AuthenticationViewModel: ViewModel() {

    private val _signedInAccount = MutableLiveData<AccountHelper.SignedInAccount>()
    val signedInAccount: LiveData<AccountHelper.SignedInAccount> = _signedInAccount

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val signedInAccount = AccountHelper.signIn(username, password)
            when(signedInAccount) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {
                }
                is Loadable.Loaded -> {
                    _signedInAccount.postValue(signedInAccount.data!!)
                }
            }
        }
    }
}