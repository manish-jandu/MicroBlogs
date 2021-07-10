package com.scaler.microblogs.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.databinding.ItemPostBinding

class ArticleAdapter() : PagingDataAdapter<Article, ArticleAdapter.PostViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }

    inner class PostViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val userName = binding.textViewUserName
        private val description = binding.textViewDescription
        private val profilePhoto = binding.imageViewProfile
        private val time = binding.textViewTime

        fun bind(item: Article) {
            userName.text = item.author?.username
            description.text = item.description
            time.text = item.updatedAt
            Glide.with(profilePhoto)
                .load(item.author!!.image)
                .centerCrop()
                .into(profilePhoto)
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Article>() {

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Article,
            newItem: Article
        ): Boolean {
            return oldItem.title == newItem.title
        }
    }
}