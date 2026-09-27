package com.jipange.hardwaremobileerp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    private var logsList: List<SystemLog>,
    private val onLogClick: (SystemLog) -> Unit
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLogModule: TextView = view.findViewById(R.id.tvLogModule)
        val tvLogAction: TextView = view.findViewById(R.id.tvLogAction)
        val tvLogUser: TextView = view.findViewById(R.id.tvLogUser)
        val tvLogDescription: TextView = view.findViewById(R.id.tvLogDescription)
        val tvLogTime: TextView = view.findViewById(R.id.tvLogTime)
    }

    fun updateLogs(newList: List<SystemLog>) {
        if (this.logsList != newList) {
            this.logsList = newList
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logsList[position]
        holder.tvLogModule.text = log.moduleName.uppercase()
        holder.tvLogAction.text = log.actionType.replace("_", " ")
        holder.tvLogUser.text = "@${log.username}"
        holder.tvLogDescription.text = log.description
        holder.tvLogTime.text = log.logTime

        holder.itemView.setOnClickListener {
            onLogClick(log)
        }

        // High-contrast module badge color scheme
        val colorHex = getModuleColorHex(log.moduleName)

        val badgeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.parseColor(colorHex))
        }
        holder.tvLogModule.background = badgeDrawable
    }

    override fun getItemCount(): Int = logsList.size

    companion object {
        fun getModuleColorHex(moduleName: String): String {
            return when (moduleName.uppercase()) {
                "SECURITY", "AUTH" -> "#B91C1C"   // Dark Crimson Red (White text ratio > 6:1)
                "POS", "SALES" -> "#047857"        // Dark Emerald (White text ratio > 5.5:1)
                "INVENTORY", "STOCK" -> "#4338CA"  // Deep Indigo (White text ratio > 7.5:1)
                "HR", "STAFF" -> "#0369A1"         // Deep Blue (White text ratio > 5.5:1)
                "LOGISTICS", "FLEET" -> "#B45309"  // Dark Amber / Orange (White text ratio > 4.8:1)
                "CONTRACTOR" -> "#6D28D9"          // Deep Purple (White text ratio > 6.5:1)
                "SUPPLIER" -> "#BE185D"            // Deep Magenta (White text ratio > 5:1)
                "FINANCIAL", "LEDGER" -> "#0F766E"  // Deep Teal (White text ratio > 5:1)
                else -> "#334155"                  // Dark Slate (White text ratio > 7.5:1)
            }
        }
    }
}
