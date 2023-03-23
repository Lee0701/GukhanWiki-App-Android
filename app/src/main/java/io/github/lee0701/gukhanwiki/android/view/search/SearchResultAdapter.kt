package io.github.lee0701.gukhanwiki.android.view.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.lee0701.gukhanwiki.android.databinding.ListitemSearchResultBinding

class SearchResultAdapter(
    private val onClick: (position: Int, item: SearchResultItem) -> Unit
): ListAdapter<SearchResultItem, SearchResultAdapter.ItemViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListitemSearchResultBinding.inflate(LayoutInflater.from(parent.context))
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
        holder.bind(item) { onClick(position, item) }
    }

    class ItemViewHolder(
        itemView: View,
    ): ViewHolder(itemView) {
        fun bind(item: SearchResultItem, onClick: () -> Unit) {
            val binding = ListitemSearchResultBinding.bind(itemView)
            binding.title.text = item.title
            binding.description.text = item.excerpt
            if(item.thumbnail != null) binding.thumbnail.setImageDrawable(item.thumbnail)
            binding.root.setOnClickListener { _ ->
                onClick()
            }
        }
    }

    class ItemCallback: DiffUtil.ItemCallback<SearchResultItem>() {
        override fun areItemsTheSame(
            oldItem: SearchResultItem,
            newItem: SearchResultItem
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SearchResultItem,
            newItem: SearchResultItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}