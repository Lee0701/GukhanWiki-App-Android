package io.github.lee0701.gukhanwiki.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _signInResult = MutableLiveData<Loadable<AccountHelper.SignedInAccount?>>()
    val signInResult: LiveData<Loadable<AccountHelper.SignedInAccount?>> = _signInResult

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

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
            _signInResult.postValue(Loadable.Loaded(null))
            val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
            if(csrfToken == null) _signInResult.postValue(Loadable.Error(RuntimeException("Token error.")))
            else {
                val response = GukhanWikiApi.actionApiService.logout(token = csrfToken)
                if(response.error != null) _signInResult.postValue(Loadable.Error(RuntimeException(response.error["*"])))
                else _signInResult.postValue(Loadable.Loaded(null))
                _signInResult.postValue(Loadable.Loaded(null))
            }
        }
    }

    fun displayMessage(message: String) {
        _message.postValue(message)
    }

    fun clearMessage() {
        _message.postValue(null)
    }

}