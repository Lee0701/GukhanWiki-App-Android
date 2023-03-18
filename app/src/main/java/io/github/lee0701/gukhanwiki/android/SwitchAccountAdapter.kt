package io.github.lee0701.gukhanwiki.android

import android.accounts.Account
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import io.github.lee0701.gukhanwiki.android.databinding.ListitemAccountBinding

class SwitchAccountAdapter(
    var selectedIndex: Int = -1,
    private val onClick: (position: Int, item: Account) -> Unit,
): ListAdapter<Account, SwitchAccountAdapter.ItemViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListitemAccountBinding.inflate(LayoutInflater.from(parent.context))
        val view = binding.root.apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(position, item) { onClick(position, item) }
    }

    inner class ItemViewHolder(
        itemView: View,
    ): ViewHolder(itemView) {
        fun bind(position: Int, item: Account, onClick: () -> Unit) {
            val binding = ListitemAccountBinding.bind(itemView)
            if(position == selectedIndex) binding.checkIcon.visibility = VISIBLE
            else binding.checkIcon.visibility = INVISIBLE
            binding.title.text = item.name
            binding.root.setOnClickListener { _ ->
                onClick()
            }
        }
    }

    class ItemCallback: DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(
            oldItem: Account,
            newItem: Account
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: Account,
            newItem: Account
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.type == newItem.type
        }
    }
}