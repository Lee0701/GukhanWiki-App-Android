package io.github.lee0701.gukhanwiki.android.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = Authenticator(this)
        return authenticator.iBinder
    }
}