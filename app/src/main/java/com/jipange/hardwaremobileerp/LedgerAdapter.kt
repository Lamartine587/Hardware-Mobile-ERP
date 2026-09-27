package com.jipange.hardwaremobileerp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class LedgerAdapter(
    private val ledgerList: List<FinancialTransaction>
) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    class LedgerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTxRef: TextView = view.findViewById(R.id.tvTxRef)
        val tvTxAmount: TextView = view.findViewById(R.id.tvTxAmount)
        val tvTxCategory: TextView = view.findViewById(R.id.tvTxCategory)
        val tvTxDate: TextView = view.findViewById(R.id.tvTxDate)
        val tvTxPayerPayee: TextView = view.findViewById(R.id.tvTxPayerPayee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val tx = ledgerList[position]
        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val isInflow = "INFLOW".equals(tx.direction, ignoreCase = true)
        holder.tvTxRef.text = "Ref: ${tx.transactionRef}"
        holder.tvTxAmount.text = "${if (isInflow) "+" else "-"} KES ${fmt.format(tx.amount)}"
        holder.tvTxAmount.setTextColor(if (isInflow) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))

        holder.tvTxCategory.text = "Category: ${tx.category} (${tx.paymentMethod})"
        holder.tvTxDate.text = tx.transactionDate
        holder.tvTxPayerPayee.text = "Party: ${tx.payerPayee ?: "N/A"} (by ${tx.createdBy})"
    }

    override fun getItemCount() = ledgerList.size
}
