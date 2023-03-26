package io.github.lee0701.gukhanwiki.android.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.inputPassword.setOnEditorActionListener { _, _, _ ->
            binding.submit.performClick()
        }

        binding.submit.setOnClickListener {
            val username = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()
            viewModel.signIn(username, password)
        }

        binding.join.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GukhanWikiApi.DOC_URL.toString() + "Special:CreateAccount"))
            startActivity(intent)
        }

        viewModel.signedInAccount.observe(this) { signedInAccount ->
            if(!signedInAccount.password.isNullOrBlank()) {
                AccountHelper.addAccount(signedInAccount.username, signedInAccount.password)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        viewModel.alert.observe(this) { message ->
            if(message != null) {
                val str = resources.getString(R.string.msg_signin_failed, message)
                binding.alertWindow.text = str
            }
        }

    }
}