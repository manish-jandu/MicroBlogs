package com.scaler.microblogs.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.databinding.ItemPostBinding
import com.scaler.microblogs.utils.ArticleType
import java.text.SimpleDateFormat
import java.util.*

class ArticleAdapter(
    private val onArticleClick: OnArticleClick,
    private val articleType: ArticleType
) :
    PagingDataAdapter<Article, ArticleAdapter.PostViewHolder>(DiffUtilCallback()) {

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

    interface OnArticleClick {
        fun onItemClick(slug: String, articleType: ArticleType)
        fun onProfileClick(userName: String)
    }

    inner class PostViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val userName = binding.textViewUserName
        private val description = binding.textViewDescription
        private val profilePhoto = binding.imageViewProfile
        private val body = binding.textViewBody
        private val time = binding.textViewTime
        private val root = binding.root

        fun bind(item: Article) {
            userName.text = item.author?.username
            description.text = item.description
            body.text = item.body
            time.text =formattedTime(item.updatedAt!!)

            Glide.with(profilePhoto)
                .load(item.author!!.image)
                .centerCrop()
                .into(profilePhoto)

            root.setOnClickListener {
                onArticleClick.onItemClick(item.slug!!, articleType)
            }

            userName.setOnClickListener {
                onArticleClick.onProfileClick(item.author!!.username!!)
            }

            profilePhoto.setOnClickListener {
                onArticleClick.onProfileClick(item.author!!.username!!)
            }
        }

        private fun formattedTime(updatedAt: String): String? {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputForamt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(updatedAt)
            return outputForamt.format(date)
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