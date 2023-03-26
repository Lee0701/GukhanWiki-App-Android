package io.github.lee0701.gukhanwiki.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _tempTitle = MutableLiveData<String?>()
    val tempTitle: LiveData<String?> = _tempTitle

    private val _signedInAccount = MutableLiveData<Loadable<AccountHelper.SignedInAccount?>>()
    val signedInAccount: LiveData<Loadable<AccountHelper.SignedInAccount?>> = _signedInAccount

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> get() = _snackbarMessage

    private val _startpageClosed = MutableLiveData<Boolean>()
    val startpageClosed: LiveData<Boolean> = _startpageClosed

    fun setStartpageClosed() {
        _startpageClosed.postValue(true)
    }

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

    fun setTempTitle(title: String?) {
        _tempTitle.postValue(title)
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = AccountHelper.signIn(username, password)
            _signedInAccount.postValue(result)
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            val signInResult = this@MainViewModel.signedInAccount.value
            val csrfToken = if(signInResult is Loadable.Loaded) signInResult.data?.csrfToken else null
            if(csrfToken != null) {
                val response = GukhanWikiApi.actionApiService.logout(token = csrfToken)
                if(response.error != null) _signedInAccount.postValue(Loadable.Error(RuntimeException(response.error["*"])))
                else _signedInAccount.postValue(Loadable.Loaded(null))
                _signedInAccount.postValue(Loadable.Loaded(null))
            }
        }
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = Event(msg)
    }

}