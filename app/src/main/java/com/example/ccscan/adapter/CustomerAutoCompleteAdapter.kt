package com.example.ccscan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.ccscan.R
import com.example.ccscan.database.entity.Customer
import java.util.Locale

/**
 * 客户唛头自动完成适配器
 */
class CustomerAutoCompleteAdapter(
    context: Context,
    private val customers: List<Customer>
) : ArrayAdapter<Customer>(context, R.layout.item_autocomplete, customers) {

    private val filteredList = mutableListOf<Customer>()

    override fun getCount(): Int = filteredList.size

    override fun getItem(position: Int): Customer = filteredList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_autocomplete, parent, false)

        val customer = getItem(position)
        view.findViewById<TextView>(R.id.tvFullName).text = customer.fullName
        view.findViewById<TextView>(R.id.tvAbbreviation).text = customer.abbreviation
        view.findViewById<TextView>(R.id.tvNumericCode).text = customer.numericCode

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val keyword = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                if (keyword.isEmpty()) {
                    filteredList.clear()
                } else {
                    filteredList.clear()
                    customers.forEach { customer ->
                        val fullNameMatch = customer.fullName.lowercase(Locale.getDefault()).contains(keyword)
                        val abbreviationMatch = customer.abbreviation.lowercase(Locale.getDefault()).contains(keyword)
                        val numericCodeMatch = customer.numericCode.contains(keyword)
                        val fullNameStartMatch = customer.fullName.lowercase(Locale.getDefault()).startsWith(keyword)
                        val abbreviationStartMatch = customer.abbreviation.lowercase(Locale.getDefault()).startsWith(keyword)

                        if (fullNameMatch || abbreviationMatch || numericCodeMatch || fullNameStartMatch || abbreviationStartMatch) {
                            filteredList.add(customer)
                        }
                    }
                }

                results.values = filteredList
                results.count = filteredList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                if (results?.values is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    filteredList.addAll(results.values as List<Customer>)
                }
                notifyDataSetChanged()
            }
        }
    }
}