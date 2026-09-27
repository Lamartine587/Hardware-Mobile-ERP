package com.jipange.hardwaremobileerp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VehicleAdapter(
    private val vehicleList: List<Vehicle>
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLicensePlate: TextView = view.findViewById(R.id.tvLicensePlate)
        val tvVehicleStatus: TextView = view.findViewById(R.id.tvVehicleStatus)
        val tvVehicleTypeCap: TextView = view.findViewById(R.id.tvVehicleTypeCap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val v = vehicleList[position]
        holder.tvLicensePlate.text = v.licensePlate
        holder.tvVehicleStatus.text = v.status
        holder.tvVehicleTypeCap.text = "${v.vehicleType} (${v.capacity})"

        if ("Available".equals(v.status, ignoreCase = true)) {
            holder.tvVehicleStatus.setTextColor(Color.parseColor("#10B981"))
        } else if ("On Route".equals(v.status, ignoreCase = true)) {
            holder.tvVehicleStatus.setTextColor(Color.parseColor("#F59E0B"))
        } else {
            holder.tvVehicleStatus.setTextColor(Color.parseColor("#EF4444"))
        }
    }

    override fun getItemCount(): Int = vehicleList.size
}
