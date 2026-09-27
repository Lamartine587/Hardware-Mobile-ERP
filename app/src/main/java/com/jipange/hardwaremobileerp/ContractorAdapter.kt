package com.jipange.hardwaremobileerp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class ContractorAdapter(
    private val contractorList: List<Contractor>,
    private val onEditClick: (Contractor) -> Unit,
    private val onPayClick: (Contractor) -> Unit
) : RecyclerView.Adapter<ContractorAdapter.ContractorViewHolder>() {

    class ContractorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCompanyName: TextView = view.findViewById(R.id.tvCompanyName)
        val tvContractorStatus: TextView = view.findViewById(R.id.tvContractorStatus)
        val tvContactPerson: TextView = view.findViewById(R.id.tvContactPerson)
        val tvCreditLimit: TextView = view.findViewById(R.id.tvCreditLimit)
        val tvCurrentBalance: TextView = view.findViewById(R.id.tvCurrentBalance)
        val btnEditContractor: MaterialButton = view.findViewById(R.id.btnEditContractor)
        val btnPayContractor: MaterialButton = view.findViewById(R.id.btnPayContractor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contractor, parent, false)
        return ContractorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContractorViewHolder, position: Int) {
        val c = contractorList[position]
        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        holder.tvCompanyName.text = c.companyName
        holder.tvContractorStatus.text = c.status
        holder.tvContactPerson.text = "Contact: ${c.contactPerson} (${c.phoneNumber})"
        holder.tvCreditLimit.text = "Limit: KES ${fmt.format(c.creditLimit)}"
        holder.tvCurrentBalance.text = "Debt: KES ${fmt.format(c.currentBalance)}"

        holder.btnEditContractor.setOnClickListener { onEditClick(c) }
        holder.btnPayContractor.setOnClickListener { onPayClick(c) }
    }

    override fun getItemCount(): Int = contractorList.size
}
