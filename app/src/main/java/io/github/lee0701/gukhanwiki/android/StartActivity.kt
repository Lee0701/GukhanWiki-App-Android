package io.github.lee0701.gukhanwiki.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private var _binding: ActivityStartBinding? = null
    val binding: ActivityStartBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        _binding = ActivityStartBinding.inflate(layoutInflater)

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
        _binding = null
    }
}