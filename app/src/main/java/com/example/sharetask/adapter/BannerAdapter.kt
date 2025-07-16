package com.example.sharetask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharetask.databinding.ItemBannerBinding

class BannerAdapter(ints: List<Int>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private val items = mutableListOf<Int>()

    fun submitList(data: List<Int>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    inner class BannerViewHolder(val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgRes: Int) {
            binding.imgBanner.setImageResource(imgRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
