package com.example.ccscan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.database.entity.Product
import com.example.ccscan.databinding.ItemDatabaseBinding

/**
 * 资料库列表适配器（通用）
 */
class DatabaseListAdapter<T>(
    private val items: List<T>,
    private val onEditClick: (T) -> Unit,
    private val onDeleteClick: (T) -> Unit
) : RecyclerView.Adapter<DatabaseListAdapter<T>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDatabaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDatabaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @Suppress("UNCHECKED_CAST")
        fun bind(item: T) {
            when (item) {
                is Customer -> {
                    binding.tvFullName.text = item.fullName
                    binding.tvAbbreviation.text = item.abbreviation
                    binding.tvNumericCode.text = item.numericCode
                }
                is Product -> {
                    binding.tvFullName.text = item.fullName
                    binding.tvAbbreviation.text = item.abbreviation
                    binding.tvNumericCode.text = item.numericCode
                }
            }

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}