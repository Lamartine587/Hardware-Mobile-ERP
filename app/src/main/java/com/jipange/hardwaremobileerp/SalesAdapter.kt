package com.jipange.hardwaremobileerp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class SalesAdapter(
    private val salesList: List<Sale>,
    private val onItemClick: (Sale) -> Unit
) : RecyclerView.Adapter<SalesAdapter.SaleViewHolder>() {

    class SaleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReceiptNo: TextView = view.findViewById(R.id.tvReceiptNo)
        val tvTotalAmount: TextView = view.findViewById(R.id.tvTotalAmount)
        val tvCashier: TextView = view.findViewById(R.id.tvCashier)
        val tvPaymentMethod: TextView = view.findViewById(R.id.tvPaymentMethod)
        val tvSaleDate: TextView = view.findViewById(R.id.tvSaleDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val sale = salesList[position]
        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        holder.tvReceiptNo.text = sale.receiptNumber
        holder.tvTotalAmount.text = "KES ${fmt.format(sale.totalAmount)}"
        holder.tvCashier.text = "Cashier: ${sale.cashierUsername}"
        holder.tvPaymentMethod.text = "Method: ${sale.paymentMethod}"
        holder.tvSaleDate.text = "Date: ${sale.saleDate ?: "N/A"}"

        holder.itemView.setOnClickListener { onItemClick(sale) }
    }

    override fun getItemCount(): Int = salesList.size
}
