package io.github.lee0701.gukhanwiki.android

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import io.github.lee0701.gukhanwiki.android.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.inputPassword.setOnEditorActionListener { v, actionId, event ->
            binding.submit.performClick()
        }

        binding.submit.setOnClickListener {
            val username = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()
            viewModel.signIn(username, password)
        }

        viewModel.signedInAccount.observe(this) { signedInAccount ->
            if(signedInAccount.password.isBlank()) return@observe
            AccountHelper.addAccount(signedInAccount.username, signedInAccount.password)
            setResult(Activity.RESULT_OK)
            finish()
        }

    }
}