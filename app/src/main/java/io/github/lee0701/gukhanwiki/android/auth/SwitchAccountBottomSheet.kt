package io.github.lee0701.gukhanwiki.android.auth

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.lee0701.gukhanwiki.android.databinding.FragmentSwitchAccountBinding

class SwitchAccountBottomSheet(
    private val onClick: (Int, Account?, type: Int) -> Unit,
): BottomSheetDialogFragment() {

    val adapter = SwitchAccountAdapter { i, account, type ->
        this.onClick(i, account, type)
        this.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSwitchAccountBinding.inflate(inflater, container, false)
        val context = context ?: return View(context)
        binding.signOut.setOnClickListener {
            this.onClick(-1, null, 0)
            this.dismiss()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SwitchAccountBottomSheet.adapter
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }
        binding.addAccount.setOnClickListener {
            val intent = Intent(context, AuthenticationActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        this.dismiss()
    }

    companion object {
        const val TAG = "SwitchAccountBottomSheet"
    }
}