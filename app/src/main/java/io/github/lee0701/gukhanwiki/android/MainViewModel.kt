package io.github.lee0701.gukhanwiki.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _url = MutableLiveData<String>()
    val url: LiveData<String> = _url

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _tempTitle = MutableLiveData<String?>()
    val tempTitle: LiveData<String?> = _tempTitle

    private val _signedInAccount = MutableLiveData<Result<AccountHelper.SignedInAccount?>>()
    val signedInAccount: LiveData<Result<AccountHelper.SignedInAccount?>> = _signedInAccount

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> get() = _snackbarMessage

    private val _startpageClosed = MutableLiveData<Boolean>()
    val startpageClosed: LiveData<Boolean> = _startpageClosed

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _signedInAccount.postValue(Result.Error(throwable))
        throwable.printStackTrace()
    }

    fun setStartpageClosed() {
        _startpageClosed.postValue(true)
    }

    fun updateUrl(url: String) {
        _url.postValue(url)
    }

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

    fun setTempTitle(title: String?) {
        _tempTitle.postValue(title)
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val result = AccountHelper.signIn(username, password)
            _signedInAccount.postValue(result)
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val signInResult = this@MainViewModel.signedInAccount.value
            val csrfToken = if(signInResult is Result.Loaded) signInResult.data?.csrfToken else null
            if(csrfToken != null) {
                val response = GukhanWikiApi.actionApiService.logout(token = csrfToken)
                if(response.error != null) _signedInAccount.postValue(Result.Error(RuntimeException(response.error["*"])))
                else _signedInAccount.postValue(Result.Loaded(null))
                _signedInAccount.postValue(Result.Loaded(null))
            }
        }
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = Event(msg)
    }

}