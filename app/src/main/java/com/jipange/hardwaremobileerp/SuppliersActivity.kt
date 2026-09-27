package com.jipange.hardwaremobileerp

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SuppliersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val api = ApiService.create()

    private val supplierStatuses = arrayOf("Active", "Inactive", "Blacklisted")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suppliers)
        NavigationHelper.setupNavigation(this, R.id.nav_suppliers)

        recyclerView = findViewById(R.id.suppliersRecyclerView)
        progressBar = findViewById(R.id.progressBarSuppliers)
        val btnAddSupplier = findViewById<MaterialButton>(R.id.btnAddSupplier)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddSupplier.setOnClickListener {
            showSupplierFormDialog(null)
        }

        loadSuppliers()
    }

    private fun loadSuppliers() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val suppliers = api.getAllSuppliers()
                progressBar.visibility = View.GONE
                recyclerView.adapter = SupplierAdapter(
                    suppliers,
                    onEditClick = { s -> showSupplierFormDialog(s) },
                    onPayClick = { s -> showSupplierPaymentDialog(s) }
                )
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@SuppliersActivity, "Failed to load suppliers: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSupplierFormDialog(supplierToEdit: Supplier?) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (companyField, etCompany) = ModernDialogHelper.createStyledInputField(context, "Company / Vendor Name *", "e.g. Bamburi Cement Ltd", supplierToEdit?.supplierName ?: "")
        val (contactField, etContact) = ModernDialogHelper.createStyledInputField(context, "Contact Person", "e.g. Mary Wanjiku", supplierToEdit?.contactPerson ?: "")
        val (phoneField, etPhone) = ModernDialogHelper.createStyledInputField(context, "Phone Number", "07...", supplierToEdit?.phoneNumber ?: "", inputType = InputType.TYPE_CLASS_PHONE)
        val (emailField, etEmail) = ModernDialogHelper.createStyledInputField(context, "Email Address", "vendor@domain.com", supplierToEdit?.email ?: "", inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val (balanceField, etBalance) = ModernDialogHelper.createStyledInputField(context, "Outstanding Payable Balance (KES)", "0.0", supplierToEdit?.outstandingBalance?.toString() ?: "0.0", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val statusIdx = if (supplierToEdit != null) supplierStatuses.indexOfFirst { it.equals(supplierToEdit.status, ignoreCase = true) }.coerceAtLeast(0) else 0
        val (statusField, spStatus) = ModernDialogHelper.createStyledSpinnerField(context, "Vendor Status", supplierStatuses.toList(), statusIdx)

        contentLayout.addView(companyField)
        contentLayout.addView(contactField)
        contentLayout.addView(phoneField)
        contentLayout.addView(emailField)
        contentLayout.addView(balanceField)
        contentLayout.addView(statusField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val isEdit = supplierToEdit != null
        val title = if (!isEdit) "Add New Supplier" else "Edit Supplier Details"
        val subtitle = if (!isEdit) "Register vendor account to manage purchases & payables" else "Update supplier contact and account status"
        val headerView = ModernDialogHelper.createHeaderView(context, "🏭", title, subtitle)

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Save Supplier") { _, _ ->
                val company = etCompany.text.toString().trim()
                val contact = etContact.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val balance = etBalance.text.toString().toDoubleOrNull() ?: 0.0
                val status = spStatus.selectedItem.toString()

                if (company.isEmpty()) {
                    Toast.makeText(context, "Supplier Name is required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val s = Supplier(
                            id = supplierToEdit?.id ?: 0,
                            supplierName = company,
                            contactPerson = contact,
                            phoneNumber = phone,
                            email = email,
                            outstandingBalance = balance,
                            status = status
                        )

                        val resp = if (supplierToEdit == null) api.createSupplier(s) else api.updateSupplier(s)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Vendor saved successfully!", Toast.LENGTH_SHORT).show()
                            loadSuppliers()
                        } else {
                            Toast.makeText(context, "Vendor save failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSupplierPaymentDialog(supplier: Supplier) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (amountField, etAmount) = ModernDialogHelper.createStyledInputField(
            context, "Amount Paid (KES) *", "0.0", "", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        contentLayout.addView(amountField)
        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "💳", "Record Supplier Payment", "Vendor: ${supplier.supplierName}\nCurrent Balance Owed: KES ${supplier.outstandingBalance}")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Record Payment") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val resp = api.recordSupplierPayment(supplier.id, mapOf("amount" to amount))
                        if (resp.isSuccessful) {
                            Toast.makeText(this@SuppliersActivity, "Payment of KES $amount recorded!", Toast.LENGTH_SHORT).show()
                            loadSuppliers()
                        } else {
                            Toast.makeText(this@SuppliersActivity, "Payment recording failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SuppliersActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
