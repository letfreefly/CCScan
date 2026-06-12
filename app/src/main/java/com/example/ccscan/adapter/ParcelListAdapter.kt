package com.example.ccscan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ccscan.database.entity.Parcel
import com.example.ccscan.databinding.ItemParcelBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 包裹列表适配器
 */
class ParcelListAdapter(
    private val parcels: List<Parcel>
) : RecyclerView.Adapter<ParcelListAdapter.ParcelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParcelViewHolder {
        val binding = ItemParcelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParcelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParcelViewHolder, position: Int) {
        holder.bind(parcels[position])
    }

    override fun getItemCount(): Int = parcels.size

    inner class ParcelViewHolder(private val binding: ItemParcelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(parcel: Parcel) {
            binding.tvTrackingNumber.text = parcel.trackingNumber
            binding.tvCustomerMark.text = parcel.customerMark
            binding.tvProductName.text = parcel.productName
            binding.tvQuantity.text = parcel.quantity.toString()
            binding.tvWeight.text = String.format("%.2f", parcel.weight)
            binding.tvTime.text = formatTime(parcel.registrationTime)
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }
}