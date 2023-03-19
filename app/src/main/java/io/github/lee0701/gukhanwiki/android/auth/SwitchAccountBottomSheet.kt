package io.github.lee0701.gukhanwiki.android.auth

import android.accounts.Account
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
    private val onClick: (Int, Account?) -> Unit
): BottomSheetDialogFragment() {

    val adapter = SwitchAccountAdapter { i, account ->
        this.onClick(i, account)
        this.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = FragmentSwitchAccountBinding.inflate(inflater, container, false)
        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SwitchAccountBottomSheet.adapter
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }
        return view.root
    }

    companion object {
        const val TAG = "SwitchAccountBottomSheet"
    }
}