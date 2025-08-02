package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sharetask.data.model.User
import com.example.sharetask.databinding.ItemUserSearchBinding

class UserSearchAdapter(private val onAddClick: (User) -> Unit) :
    ListAdapter<User, UserSearchAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class UserViewHolder(val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                
                // Show NIM if available, otherwise hide the TextView
                if (!user.nim.isNullOrEmpty()) {
                    tvUserNim.visibility = View.VISIBLE
                    tvUserNim.text = "NIM: ${user.nim}"
                } else {
                    tvUserNim.visibility = View.GONE
                }
                
                // Set click listener for add button
                btnAddUser.setOnClickListener {
                    onAddClick(user)
                }
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}