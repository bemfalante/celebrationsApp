package com.celebrations.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.celebrations.app.databinding.ItemTributeBinding
import com.celebrations.app.model.Tribute
import java.text.SimpleDateFormat
import java.util.*

class TributeAdapter(private val onItemClick: (Tribute) -> Unit) :
    ListAdapter<Tribute, TributeAdapter.TributeViewHolder>(TributeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TributeViewHolder {
        val binding = ItemTributeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TributeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TributeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TributeViewHolder(private val binding: ItemTributeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(tribute: Tribute) {
            binding.textTitle.text = tribute.title
            binding.textDate.text = dateFormat.format(Date(tribute.date))
            binding.textStatus.text = if (tribute.isDownloaded) "Disponível offline" else "Aguardando download..."

            binding.root.setOnClickListener {
                if (tribute.isDownloaded) {
                    onItemClick(tribute)
                }
            }
        }
    }

    class TributeDiffCallback : DiffUtil.ItemCallback<Tribute>() {
        override fun areItemsTheSame(oldItem: Tribute, newItem: Tribute): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tribute, newItem: Tribute): Boolean = oldItem == newItem
    }
}
