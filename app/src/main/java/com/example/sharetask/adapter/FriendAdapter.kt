package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sharetask.data.model.User
import com.example.sharetask.databinding.ItemFriendBinding
import com.bumptech.glide.Glide

class FriendAdapter : ListAdapter<User, FriendAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)
    }

    class FriendViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            // Load profile image if available, otherwise use default
            if (user.profilePic != null) {
                Glide.with(binding.root.context)
                    .load(user.profilePic)
                    .centerCrop()
                    .into(binding.imgProfile)
            }
        }
    }
    
    private class FriendDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}