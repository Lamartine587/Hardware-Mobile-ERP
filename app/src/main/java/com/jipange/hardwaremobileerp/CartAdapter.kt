package com.jipange.hardwaremobileerp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: List<MobileSaleItem>,
    private val allProducts: List<Product>,
    private val onRemoveClick: (MobileSaleItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCartProductName: TextView = view.findViewById(R.id.tvCartProductName)
        val tvCartDetails: TextView = view.findViewById(R.id.tvCartDetails)
        val tvCartSubtotal: TextView = view.findViewById(R.id.tvCartSubtotal)
        val btnRemoveCartItem: ImageButton = view.findViewById(R.id.btnRemoveCartItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        val product = allProducts.find { it.sku.equalsIgnoreCase(item.sku) }

        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val name = product?.name ?: item.sku
        val unitPrice = product?.sellingPrice ?: 0.0
        val subtotal = unitPrice * item.quantity

        holder.tvCartProductName.text = "${item.sku} - $name"
        holder.tvCartDetails.text = "Qty: ${item.quantity} x KES ${fmt.format(unitPrice)}"
        holder.tvCartSubtotal.text = "KES ${fmt.format(subtotal)}"

        holder.btnRemoveCartItem.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    private fun String.equalsIgnoreCase(other: String): Boolean = this.equals(other, ignoreCase = true)
}
