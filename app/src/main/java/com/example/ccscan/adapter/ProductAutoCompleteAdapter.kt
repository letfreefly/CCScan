package com.example.ccscan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.ccscan.R
import com.example.ccscan.database.entity.Product
import java.util.Locale

/**
 * 商品名称自动完成适配器
 */
class ProductAutoCompleteAdapter(
    context: Context,
    private val products: List<Product>
) : ArrayAdapter<Product>(context, R.layout.item_autocomplete, products) {

    private val filteredList = mutableListOf<Product>()

    override fun getCount(): Int = filteredList.size

    override fun getItem(position: Int): Product = filteredList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_autocomplete, parent, false)

        val product = getItem(position)
        view.findViewById<TextView>(R.id.tvFullName).text = product.fullName
        view.findViewById<TextView>(R.id.tvAbbreviation).text = product.abbreviation
        view.findViewById<TextView>(R.id.tvNumericCode).text = product.numericCode

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
                    products.forEach { product ->
                        val fullNameMatch = product.fullName.lowercase(Locale.getDefault()).contains(keyword)
                        val abbreviationMatch = product.abbreviation.lowercase(Locale.getDefault()).contains(keyword)
                        val numericCodeMatch = product.numericCode.contains(keyword)
                        val fullNameStartMatch = product.fullName.lowercase(Locale.getDefault()).startsWith(keyword)
                        val abbreviationStartMatch = product.abbreviation.lowercase(Locale.getDefault()).startsWith(keyword)

                        if (fullNameMatch || abbreviationMatch || numericCodeMatch || fullNameStartMatch || abbreviationStartMatch) {
                            filteredList.add(product)
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
                    filteredList.addAll(results.values as List<Product>)
                }
                notifyDataSetChanged()
            }
        }
    }
}