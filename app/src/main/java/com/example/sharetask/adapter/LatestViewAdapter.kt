package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharetask.R
import com.example.sharetask.data.model.LatestView
import com.example.sharetask.databinding.ItemLatestViewBinding
import java.util.concurrent.TimeUnit

class LatestViewAdapter(
    private val onItemClicked: (LatestView) -> Unit
) : ListAdapter<LatestView, LatestViewAdapter.LatestViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder {
        val binding =
            ItemLatestViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LatestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LatestViewHolder(private val binding: ItemLatestViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(latestView: LatestView) {
            // Set judul pertanyaan
            binding.tvDocTitle.text = latestView.title
            
            // Set deskripsi pertanyaan
            binding.tvDocDescription.text = latestView.description
            
            // Set waktu dilihat
            binding.tvViewedTime.text = "Viewed ${getTimeAgo(latestView.viewedAt)}"
            
            // Load thumbnail jika ada
            Glide.with(binding.root.context)
                .load(latestView.thumbnailUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.ivViewThumbnail)
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClicked(latestView)
            }
            binding.btnContinue.setOnClickListener {
                onItemClicked(latestView)
            }
        }
        
        private fun getTimeAgo(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - timestamp
            
            return when {
                timeDiff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                timeDiff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
                    "$minutes min ago"
                }
                timeDiff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
                    "$hours hr ago"
                }
                timeDiff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(timeDiff)
                    "$days days ago"
                }
                else -> {
                    val days = TimeUnit.MILLISECONDS.toDays(timeDiff)
                    "$days days ago"
                }
            }
        }
    }
    
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LatestView>() {
            override fun areItemsTheSame(oldItem: LatestView, newItem: LatestView): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LatestView, newItem: LatestView): Boolean {
                return oldItem == newItem
            }
        }
    }
}