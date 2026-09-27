package com.jipange.hardwaremobileerp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class SupplierAdapter(
    private val supplierList: List<Supplier>,
    private val onEditClick: (Supplier) -> Unit,
    private val onPayClick: (Supplier) -> Unit
) : RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    class SupplierViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSupplierName: TextView = view.findViewById(R.id.tvSupplierName)
        val tvSupplierStatus: TextView = view.findViewById(R.id.tvSupplierStatus)
        val tvSupplierContact: TextView = view.findViewById(R.id.tvSupplierContact)
        val tvSupplierBalance: TextView = view.findViewById(R.id.tvSupplierBalance)
        val btnEditSupplier: MaterialButton = view.findViewById(R.id.btnEditSupplier)
        val btnPaySupplier: MaterialButton = view.findViewById(R.id.btnPaySupplier)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_supplier, parent, false)
        return SupplierViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val s = supplierList[position]
        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        holder.tvSupplierName.text = s.supplierName
        holder.tvSupplierStatus.text = s.status
        holder.tvSupplierContact.text = "Contact: ${s.contactPerson} (${s.phoneNumber})"
        holder.tvSupplierBalance.text = "We Owe: KES ${fmt.format(s.outstandingBalance)}"

        holder.btnEditSupplier.setOnClickListener { onEditClick(s) }
        holder.btnPaySupplier.setOnClickListener { onPayClick(s) }
    }

    override fun getItemCount(): Int = supplierList.size
}
