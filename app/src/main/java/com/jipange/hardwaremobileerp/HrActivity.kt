package com.jipange.hardwaremobileerp

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HrActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val api = ApiService.create()

    private val roles = arrayOf("Cashier", "InventoryClerk", "LogisticsOfficer", "Manager", "Admin")
    private val statuses = arrayOf("Active", "On Leave", "Suspended", "Terminated")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hr)
        NavigationHelper.setupNavigation(this, R.id.nav_hr)

        recyclerView = findViewById(R.id.hrRecyclerView)
        val btnAddStaff = findViewById<MaterialButton>(R.id.btnAddStaff)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddStaff.setOnClickListener {
            showAddStaffDialog()
        }

        loadStaff()
    }

    private fun loadStaff() {
        lifecycleScope.launch {
            try {
                val staffList = api.getAllStaff()
                recyclerView.adapter = EmployeeAdapter(
                    staffList,
                    onEditClick = { emp -> showEditStaffDialog(emp) },
                    onResetPassClick = { emp -> showResetPasswordDialog(emp) }
                )
            } catch (e: Exception) {
                Toast.makeText(this@HrActivity, "Error loading staff: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddStaffDialog() {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (nameField, etName) = ModernDialogHelper.createStyledInputField(context, "Full Name *", "e.g. John Doe")
        val (userField, etUser) = ModernDialogHelper.createStyledInputField(context, "Username *", "e.g. jdoe")
        val (idField, etId) = ModernDialogHelper.createStyledInputField(context, "National ID *", "e.g. 12345678", inputType = InputType.TYPE_CLASS_NUMBER)
        val (kraField, etKra) = ModernDialogHelper.createStyledInputField(context, "KRA PIN", "e.g. A000000000Z")
        val (nssfField, etNssf) = ModernDialogHelper.createStyledInputField(context, "NSSF Member No.", "NSSF Number", inputType = InputType.TYPE_CLASS_NUMBER)
        val (nhifField, etNhif) = ModernDialogHelper.createStyledInputField(context, "NHIF/SHIF Member No.", "NHIF Number", inputType = InputType.TYPE_CLASS_NUMBER)
        val (phoneField, etPhone) = ModernDialogHelper.createStyledInputField(context, "Phone Number", "07...", inputType = InputType.TYPE_CLASS_PHONE)
        val (salaryField, etSalary) = ModernDialogHelper.createStyledInputField(context, "Basic Salary (KES)", "45000", "45000", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val (roleField, spRole) = ModernDialogHelper.createStyledSpinnerField(context, "System Role", roles.toList(), 0)
        val (statusField, spStatus) = ModernDialogHelper.createStyledSpinnerField(context, "Employment Status", statuses.toList(), 0)

        contentLayout.addView(nameField)
        contentLayout.addView(userField)
        contentLayout.addView(idField)
        contentLayout.addView(kraField)
        contentLayout.addView(nssfField)
        contentLayout.addView(nhifField)
        contentLayout.addView(phoneField)
        contentLayout.addView(salaryField)
        contentLayout.addView(roleField)
        contentLayout.addView(statusField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "👤", "Onboard New Staff Member", "Fill out staff profile details and access rights")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Register Staff") { _, _ ->
                val name = etName.text.toString().trim()
                val user = etUser.text.toString().trim()
                val natId = etId.text.toString().trim()
                val kra = etKra.text.toString().trim()
                val nssf = etNssf.text.toString().trim()
                val nhif = etNhif.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val salary = etSalary.text.toString().toDoubleOrNull() ?: 0.0
                val role = spRole.selectedItem.toString()
                val status = spStatus.selectedItem.toString()

                if (name.isEmpty() || user.isEmpty() || natId.isEmpty()) {
                    Toast.makeText(context, "Full Name, Username, and National ID are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val body = mapOf(
                            "fullName" to name,
                            "username" to user,
                            "nationalId" to natId,
                            "kraPin" to kra,
                            "nssfNumber" to nssf,
                            "nhifNumber" to nhif,
                            "phoneNumber" to phone,
                            "basicSalary" to salary,
                            "role" to role,
                            "employmentStatus" to status,
                            "permissions" to if ("Manager".contains(role, true) || "Admin".contains(role, true)) "HOME,POS,INVENTORY,SALES,CONTRACTORS,SUPPLIERS,LOGISTICS,HR,AUDIT" else "HOME,POS"
                        )
                        val resp = api.createStaff(body)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Staff registered! Password set to National ID.", Toast.LENGTH_LONG).show()
                            loadStaff()
                        } else {
                            Toast.makeText(context, "Registration failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditStaffDialog(emp: Employee) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (phoneField, etPhone) = ModernDialogHelper.createStyledInputField(context, "Phone Number", "07...", emp.phoneNumber ?: "", inputType = InputType.TYPE_CLASS_PHONE)
        val (salaryField, etSalary) = ModernDialogHelper.createStyledInputField(context, "Basic Salary (KES)", "0.0", emp.basicSalary.toString(), inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val roleIdx = roles.indexOfFirst { it.equals(emp.role, ignoreCase = true) }.coerceAtLeast(0)
        val (roleField, spRole) = ModernDialogHelper.createStyledSpinnerField(context, "System Role", roles.toList(), roleIdx)

        val statusIdx = statuses.indexOfFirst { it.equals(emp.employmentStatus, ignoreCase = true) }.coerceAtLeast(0)
        val (statusField, spStatus) = ModernDialogHelper.createStyledSpinnerField(context, "Employment Status", statuses.toList(), statusIdx)

        contentLayout.addView(phoneField)
        contentLayout.addView(salaryField)
        contentLayout.addView(roleField)
        contentLayout.addView(statusField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "👤", "Edit Staff: ${emp.fullName}", "Update contact, salary, role, or status")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Update Staff") { _, _ ->
                val phone = etPhone.text.toString().trim()
                val salary = etSalary.text.toString().toDoubleOrNull() ?: emp.basicSalary
                val role = spRole.selectedItem.toString()
                val status = spStatus.selectedItem.toString()

                lifecycleScope.launch {
                    try {
                        val updatedEmp = Employee(
                            id = emp.id,
                            fullName = emp.fullName,
                            nationalId = emp.nationalId,
                            kraPin = emp.kraPin,
                            nssfNumber = emp.nssfNumber,
                            nhifNumber = emp.nhifNumber,
                            phoneNumber = phone,
                            basicSalary = salary,
                            role = role,
                            username = emp.username,
                            employmentStatus = status,
                            permissions = emp.permissions,
                            forcePasswordChange = emp.forcePasswordChange
                        )

                        val resp = api.updateStaff(emp.id, updatedEmp)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Staff updated!", Toast.LENGTH_SHORT).show()
                            loadStaff()
                        } else {
                            Toast.makeText(context, "Update failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetPasswordDialog(emp: Employee) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Force reset password for ${emp.fullName} (@${emp.username}) to: 123456?")
            .setPositiveButton("Reset Password") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val resp = api.resetStaffPassword(emp.id)
                        if (resp.isSuccessful) {
                            Toast.makeText(this@HrActivity, "Password reset to: 123456", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@HrActivity, "Reset failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@HrActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}