package io.github.lee0701.gukhanwiki.android.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import java.io.Serializable

object AccountHelper {
    private var accountManager: AccountManager? = null

    private const val ACCOUNT_TYPE = "io.github.lee0701.gukhanwiki.android"

    fun initAccountManager(context: Context) {
        accountManager = AccountManager.get(context)
    }

    fun getAccountManager(): AccountManager? {
        return accountManager
    }

    fun getAccounts(): List<Account>? {
        return accountManager?.getAccountsByType(ACCOUNT_TYPE)?.toList()
    }

    fun addAccount(username: String, password: String) {
        Account(username, ACCOUNT_TYPE).also { account ->
            accountManager?.addAccountExplicitly(account, password, null)
        }
    }

    fun getPassword(account: Account): String? {
        return try {
            accountManager?.getPassword(account)
        } catch(ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun signIn(username: String, password: String): Result<SignedInAccount?> {
        val tokenResult = GukhanWikiApi.actionApiService.retrieveToken(type = "login")
        val loginToken = tokenResult.query.tokens["logintoken"] ?: return Result.Error(
            RuntimeException("token")
        )
        val loginResult = GukhanWikiApi.actionApiService.clientLogin(
            loginToken = loginToken,
            username = username,
            password = password,
        )
        if(loginResult.error != null) return Result.Error(RuntimeException(loginResult.error["*"]))
        if(loginResult.clientLogin?.status != "PASS") return Result.Error(RuntimeException(loginResult.clientLogin?.status))

        val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
        val account = SignedInAccount(username = username, password = password, csrfToken = csrfToken)
        return Result.Loaded(account)
    }

    data class SignedInAccount(
        val username: String,
        val password: String? = null,
        val csrfToken: String? = null,
    ): Serializable
}