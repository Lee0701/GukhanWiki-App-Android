package io.github.lee0701.gukhanwiki.android

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.Bundle
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import java.io.Serializable
import java.util.concurrent.TimeUnit

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

    suspend fun signIn(username: String, password: String): Loadable<SignedInAccount> {
        val tokenResult = GukhanWikiApi.actionApiService.retrieveToken(type = "login")
        // TODO: Use string resources
        val loginToken = tokenResult.query.tokens["logintoken"] ?: return Loadable.Error(RuntimeException("Cound not retrieve a login token."))
        val loginResult = GukhanWikiApi.actionApiService.clientLogin(
            loginToken = loginToken,
            username = username,
            password = password,
        )
        // TODO: use string resource
        if(loginResult.clientLogin?.status != "PASS") return Loadable.Error(RuntimeException("Login failed."))

        val account = SignedInAccount(username, password)
        return Loadable.Loaded(account)
    }

    data class SignedInAccount(
        val username: String,
        val password: String,
    ): Serializable
}