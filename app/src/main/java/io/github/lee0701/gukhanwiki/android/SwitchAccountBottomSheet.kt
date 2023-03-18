package io.github.lee0701.gukhanwiki.android

import android.accounts.Account
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SwitchAccountBottomSheet(
    private val onClick: (Int, Account) -> Unit
): BottomSheetDialogFragment() {

    val adapter = SwitchAccountAdapter { i, account ->
        this.onClick(i, account)
        this.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_switch_account, container, false)
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SwitchAccountBottomSheet.adapter
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }
        return view
    }

    companion object {
        const val TAG = "SwitchAccountBottomSheet"
    }
}