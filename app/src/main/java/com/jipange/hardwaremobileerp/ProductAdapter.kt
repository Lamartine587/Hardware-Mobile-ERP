package com.jipange.hardwaremobileerp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvName.text = "${product.sku} - ${product.name}"
        holder.tvStock.text = "Stock: ${product.stockQuantity} ${product.unitOfMeasure}"
        holder.tvPrice.text = "KES ${product.sellingPrice}"

        if (product.stockQuantity < 10) {
            holder.tvStock.setTextColor(Color.parseColor("#EF4444"))
        } else {
            holder.tvStock.setTextColor(Color.parseColor("#10B981"))
        }

        holder.itemView.setOnClickListener { onItemClick?.invoke(product) }
    }

    override fun getItemCount() = productList.size
}