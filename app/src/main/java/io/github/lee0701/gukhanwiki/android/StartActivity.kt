package io.github.lee0701.gukhanwiki.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private var binding: ActivityStartBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val binding = ActivityStartBinding.inflate(layoutInflater)
        this.binding = binding
        supportActionBar?.title = ""

        binding.checkboxHide.setOnCheckedChangeListener { _, checked ->
            preference.edit().putBoolean("startpage_hide", checked).apply()
        }

        binding.buttonStart.setOnClickListener {
            finish()
        }

        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}