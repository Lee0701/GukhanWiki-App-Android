package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.AccountHelper
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _signInResult = MutableLiveData<Loadable<AccountHelper.SignedInAccount>?>()
    val signInResult: LiveData<Loadable<AccountHelper.SignedInAccount>?> = _signInResult

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val result = AccountHelper.signIn(username, password)
            _signInResult.postValue(result)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
            if(csrfToken != null) GukhanWikiApi.actionApiService.logout(token = csrfToken)
            _signInResult.postValue(null)
        }
    }

}