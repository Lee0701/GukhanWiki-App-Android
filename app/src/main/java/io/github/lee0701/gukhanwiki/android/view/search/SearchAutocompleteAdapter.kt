package io.github.lee0701.gukhanwiki.android.view.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.lee0701.gukhanwiki.android.databinding.ListitemSearchAutocompleteBinding

class SearchAutocompleteAdapter(
    private val onClick: (position: Int, item: SearchAutocompleteItem) -> Unit
): ListAdapter<SearchAutocompleteItem, SearchAutocompleteAdapter.ItemViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListitemSearchAutocompleteBinding.inflate(LayoutInflater.from(parent.context))
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
        holder.bind(item) { action -> onClick(position, item) }
    }

    class ItemViewHolder(
        itemView: View,
    ): ViewHolder(itemView) {
        fun bind(item: SearchAutocompleteItem, onClick: (action: SearchAutocompleteItem.Action?) -> Unit) {
            val binding = ListitemSearchAutocompleteBinding.bind(itemView)
            binding.title.text = item.title
            binding.action.visibility = if(item.action != null) View.VISIBLE else View.GONE
            item.action?.label?.let { binding.action.setText(it) }
            binding.root.setOnClickListener {
                onClick(null)
            }
            binding.action.setOnClickListener {
                if(item.action != null) onClick(item.action)
            }
        }
    }

    class ItemCallback: DiffUtil.ItemCallback<SearchAutocompleteItem>() {
        override fun areItemsTheSame(
            oldItem: SearchAutocompleteItem,
            newItem: SearchAutocompleteItem
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SearchAutocompleteItem,
            newItem: SearchAutocompleteItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}