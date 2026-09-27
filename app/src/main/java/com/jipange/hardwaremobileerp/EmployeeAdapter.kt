package com.jipange.hardwaremobileerp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class EmployeeAdapter(
    private val employeeList: List<Employee>,
    private val onEditClick: ((Employee) -> Unit)? = null,
    private val onResetPassClick: ((Employee) -> Unit)? = null
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    class EmployeeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmpName: TextView = view.findViewById(R.id.tvEmpName)
        val tvEmpRole: TextView = view.findViewById(R.id.tvEmpRole)
        val tvEmpStatus: TextView = view.findViewById(R.id.tvEmpStatus)
        val tvEmpUsername: TextView = view.findViewById(R.id.tvEmpUsername)
        val btnEditEmp: MaterialButton = view.findViewById(R.id.btnEditEmp)
        val btnResetPass: MaterialButton = view.findViewById(R.id.btnResetPass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val emp = employeeList[position]
        holder.tvEmpName.text = emp.fullName
        holder.tvEmpRole.text = "Role: ${emp.role}"
        holder.tvEmpStatus.text = emp.employmentStatus
        holder.tvEmpUsername.text = "@${emp.username}"

        holder.btnEditEmp.setOnClickListener { onEditClick?.invoke(emp) }
        holder.btnResetPass.setOnClickListener { onResetPassClick?.invoke(emp) }
    }

    override fun getItemCount() = employeeList.size
}