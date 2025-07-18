package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharetask.databinding.ItemLatestViewBinding

class LatestViewAdapter(private val games: List<String>) :
    RecyclerView.Adapter<LatestViewAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder {
        val binding =
            ItemLatestViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LatestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        // Bind view data here
    }

    override fun getItemCount() = games.size

    class LatestViewHolder(val binding: ItemLatestViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}