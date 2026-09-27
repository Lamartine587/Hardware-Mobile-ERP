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

class ContractorsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val api = ApiService.create()

    private val contractorStatuses = arrayOf("Active", "Suspended", "On Hold")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractors)
        NavigationHelper.setupNavigation(this, R.id.nav_contractors)

        recyclerView = findViewById(R.id.contractorsRecyclerView)
        progressBar = findViewById(R.id.progressBarContractors)
        val btnAddContractor = findViewById<MaterialButton>(R.id.btnAddContractor)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddContractor.setOnClickListener {
            showContractorFormDialog(null)
        }

        loadContractors()
    }

    private fun loadContractors() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val contractors = api.getAllContractors()
                progressBar.visibility = View.GONE
                recyclerView.adapter = ContractorAdapter(
                    contractors,
                    onEditClick = { c -> showContractorFormDialog(c) },
                    onPayClick = { c -> showPaymentDialog(c) }
                )
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ContractorsActivity, "Failed to load contractors: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showContractorFormDialog(contractorToEdit: Contractor?) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (companyField, etCompany) = ModernDialogHelper.createStyledInputField(context, "Company / Firm Name *", "e.g. Apex Builders Ltd", contractorToEdit?.companyName ?: "")
        val (contactField, etContact) = ModernDialogHelper.createStyledInputField(context, "Contact Person *", "e.g. Eng. David Ndung'u", contractorToEdit?.contactPerson ?: "")
        val (phoneField, etPhone) = ModernDialogHelper.createStyledInputField(context, "Phone Number *", "07...", contractorToEdit?.phoneNumber ?: "", inputType = InputType.TYPE_CLASS_PHONE)
        val (limitField, etLimit) = ModernDialogHelper.createStyledInputField(context, "Credit Limit (KES)", "500000", contractorToEdit?.creditLimit?.toString() ?: "500000", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val (balanceField, etBalance) = ModernDialogHelper.createStyledInputField(context, "Opening Tab Balance (KES)", "0.0", contractorToEdit?.currentBalance?.toString() ?: "0.0", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val statusIdx = if (contractorToEdit != null) contractorStatuses.indexOfFirst { it.equals(contractorToEdit.status, ignoreCase = true) }.coerceAtLeast(0) else 0
        val (statusField, spStatus) = ModernDialogHelper.createStyledSpinnerField(context, "Account Status", contractorStatuses.toList(), statusIdx)

        contentLayout.addView(companyField)
        contentLayout.addView(contactField)
        contentLayout.addView(phoneField)
        contentLayout.addView(limitField)
        contentLayout.addView(balanceField)
        contentLayout.addView(statusField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val isEdit = contractorToEdit != null
        val title = if (!isEdit) "Add Contractor Client" else "Edit Contractor Details"
        val subtitle = if (!isEdit) "Register trade account client for credit sales & project tabs" else "Update contractor profile and credit limit"
        val headerView = ModernDialogHelper.createHeaderView(context, "🏗️", title, subtitle)

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Save Contractor") { _, _ ->
                val company = etCompany.text.toString().trim()
                val contact = etContact.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val limit = etLimit.text.toString().toDoubleOrNull() ?: 0.0
                val balance = etBalance.text.toString().toDoubleOrNull() ?: 0.0
                val status = spStatus.selectedItem.toString()

                if (company.isEmpty() || contact.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(context, "Company, Contact Person, and Phone are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val c = Contractor(
                            id = contractorToEdit?.id ?: 0,
                            companyName = company,
                            contactPerson = contact,
                            phoneNumber = phone,
                            creditLimit = limit,
                            currentBalance = balance,
                            status = status
                        )

                        val resp = if (contractorToEdit == null) api.createContractor(c) else api.updateContractor(c)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Contractor saved successfully!", Toast.LENGTH_SHORT).show()
                            loadContractors()
                        } else {
                            Toast.makeText(context, "Save failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPaymentDialog(contractor: Contractor) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (amountField, etAmount) = ModernDialogHelper.createStyledInputField(
            context, "Payment Amount (KES) *", "0.0", "", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        contentLayout.addView(amountField)
        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "💳", "Record Contractor Tab Payment", "Client: ${contractor.companyName}\nCurrent Outstanding Balance: KES ${contractor.currentBalance}")

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
                        val resp = api.recordContractorPayment(contractor.id, mapOf("amount" to amount))
                        if (resp.isSuccessful) {
                            Toast.makeText(this@ContractorsActivity, "Payment of KES $amount recorded!", Toast.LENGTH_SHORT).show()
                            loadContractors()
                        } else {
                            Toast.makeText(this@ContractorsActivity, "Payment recording failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ContractorsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
