package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.AccountHelper
import io.github.lee0701.gukhanwiki.android.Loadable
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _signinResult = MutableLiveData<Loadable<AccountHelper.SignedInAccount>>()
    val signinResult: LiveData<Loadable<AccountHelper.SignedInAccount>> = _signinResult

    private val _signedInAccount = MutableLiveData<AccountHelper.SignedInAccount>()
    val signedInAccount: LiveData<AccountHelper.SignedInAccount> = _signedInAccount

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val result = AccountHelper.signIn(username, password)
            _signinResult.postValue(result)
        }
    }

    fun useAccount(signedInAccount: AccountHelper.SignedInAccount) {
        _signedInAccount.postValue(signedInAccount)
    }

}