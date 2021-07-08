package com.scaler.microblogs.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scaler.microblogs.databinding.ItemTagBinding

class TagsAdapter(private val onItemTagClick: OnItemTagClick) :
    ListAdapter<String, TagsAdapter.TagsViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        val binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }

    inner class TagsViewHolder(binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                     onItemTagClick.onTagClick(item)
                }
            }
        }

        val hashTag = binding.textViewTag
        fun bind(item: String) {
            hashTag.text = item
        }
    }

    interface OnItemTagClick {
        fun onTagClick(tag:String)
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }


}