package com.jipange.hardwaremobileerp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class DeliveryAdapter(
    private val deliveryList: List<Delivery>,
    private val onMarkDeliveredClick: (Delivery) -> Unit
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInvoiceNo: TextView = view.findViewById(R.id.tvInvoiceNo)
        val tvDeliveryStatus: TextView = view.findViewById(R.id.tvDeliveryStatus)
        val tvDestination: TextView = view.findViewById(R.id.tvDestination)
        val tvDriverTruck: TextView = view.findViewById(R.id.tvDriverTruck)
        val btnMarkDelivered: MaterialButton = view.findViewById(R.id.btnMarkDelivered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val d = deliveryList[position]
        holder.tvInvoiceNo.text = d.invoiceNumber
        holder.tvDeliveryStatus.text = d.deliveryStatus
        holder.tvDestination.text = "Destination: ${d.destination}"
        holder.tvDriverTruck.text = "Driver: ${d.driverName} (${d.vehicleLicensePlate ?: "Truck"})"

        if ("Delivered".equals(d.deliveryStatus, ignoreCase = true)) {
            holder.tvDeliveryStatus.setTextColor(Color.parseColor("#10B981"))
            holder.btnMarkDelivered.visibility = View.GONE
        } else {
            holder.tvDeliveryStatus.setTextColor(Color.parseColor("#F59E0B"))
            holder.btnMarkDelivered.visibility = View.VISIBLE
            holder.btnMarkDelivered.setOnClickListener { onMarkDeliveredClick(d) }
        }
    }

    override fun getItemCount(): Int = deliveryList.size
}
