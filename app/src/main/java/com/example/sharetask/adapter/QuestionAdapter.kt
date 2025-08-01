package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharetask.data.model.Question
import com.example.sharetask.databinding.ItemQuestionBinding
import java.util.concurrent.TimeUnit

class QuestionAdapter(private val onItemClicked: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.question = question

            // Set click listener untuk seluruh card
            binding.root.setOnClickListener {
                onItemClicked(question)
            }
            // Set click listener untuk tombol Answer
            binding.btnAnswer.setOnClickListener {
                onItemClicked(question)
            }

            // Load user photo
            Glide.with(binding.root)
                .load(question.uploadedByPhotoUrl)
                .circleCrop()
                .into(binding.ivUserPhoto)

            // Set time ago
            binding.tvTime.text = getTimeAgo(question.timestamp)
            binding.tvSubject.text = question.subjectName
            binding.tvDescription.text = question.description

            binding.executePendingBindings()
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "• recently"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "• $minutes minute ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "• $hours hour ago"
                }
                else -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "• $days day ago"
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Question>() {
            override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
                return oldItem == newItem
            }
        }
    }
} 