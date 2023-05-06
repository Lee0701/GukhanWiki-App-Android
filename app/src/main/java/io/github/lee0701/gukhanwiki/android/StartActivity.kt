package io.github.lee0701.gukhanwiki.android

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
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

        val locale = preference.getString("display_locale", "ko-Kore-KR")
        val values = resources.getStringArray(R.array.start_locale_values)
        ArrayAdapter.createFromResource(
            this,
            R.array.start_locale_entries,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLocale.adapter = this
            binding.spinnerLocale.isSelected = false
            binding.spinnerLocale.setSelection(values.indexOf(locale), true)
        }
        binding.spinnerLocale.onItemSelectedListener = object: OnItemSelectedListener {
            @SuppressLint("ApplySharedPref")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = values[position]
                if(value != locale) {
                    preference.edit().putString("display_locale", value).commit()
                    GukhanWikiApplication.restart(this@StartActivity)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}