package com.scaler.microblogs.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scaler.libconduit.models.Comment
import com.scaler.microblogs.databinding.ItemCommentBinding

class CommentsAdapter() :
    ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }

    inner class CommentViewHolder(binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val userName = binding.textViewCommentUser
        val comment = binding.textViewComment
        fun bind(item: Comment) {
            userName.text = item.author?.username
            comment.text = item.body
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Comment>() {

        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }


}