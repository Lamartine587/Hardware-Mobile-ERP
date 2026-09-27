package com.jipange.hardwaremobileerp

import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PosActivity : AppCompatActivity() {

    private lateinit var posProductSpinner: Spinner
    private lateinit var spPaymentMethod: Spinner
    private lateinit var etPosQty: EditText
    private lateinit var tvPosTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var recyclerView: RecyclerView

    private val api = ApiService.create()
    private var allProductsList: List<Product> = emptyList()
    private var allContractorsList: List<Contractor> = emptyList()
    private val cartItems = mutableListOf<MobileSaleItem>()
    private var grandTotalWithVat = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)
        NavigationHelper.setupNavigation(this, R.id.nav_pos)

        posProductSpinner = findViewById(R.id.posProductSpinner)
        spPaymentMethod = findViewById(R.id.spPaymentMethod)
        etPosQty = findViewById(R.id.etPosQty)
        tvPosTotal = findViewById(R.id.tvPosTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        recyclerView = findViewById(R.id.posRecyclerView)
        val btnAddToCart = findViewById<MaterialButton>(R.id.btnAddToCart)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val paymentMethods = arrayOf("Cash", "M-Pesa (Paystack STK Push)", "Credit/Card (Paystack)", "Contractor Tab")
        val pmAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        pmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPaymentMethod.adapter = pmAdapter

        btnAddToCart.setOnClickListener { addItemToCart() }
        btnCheckout.setOnClickListener { handleCheckout() }

        loadPosData()
    }

    private fun loadPosData() {
        lifecycleScope.launch {
            try {
                allProductsList = api.getAllProducts()
                val productNames = allProductsList.map { "${it.name} (${it.sku}) - KES ${it.sellingPrice}" }
                val pAdapter = ArrayAdapter(this@PosActivity, android.R.layout.simple_spinner_item, productNames)
                pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                posProductSpinner.adapter = pAdapter
            } catch (e: Exception) {
                Toast.makeText(this@PosActivity, "Error loading products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            try {
                allContractorsList = api.getAllContractors()
            } catch (e: Exception) {
                // Silent catch
            }
        }
    }

    private fun addItemToCart() {
        if (allProductsList.isEmpty()) return
        val selectedIdx = posProductSpinner.selectedItemPosition
        if (selectedIdx < 0 || selectedIdx >= allProductsList.size) return

        val product = allProductsList[selectedIdx]
        val qty = etPosQty.text.toString().toDoubleOrNull() ?: 1.0

        if (qty <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        val existing = cartItems.find { it.sku == product.sku }
        if (existing != null) {
            val updatedQty = existing.quantity + qty
            cartItems.remove(existing)
            cartItems.add(MobileSaleItem(product.sku, updatedQty))
        } else {
            cartItems.add(MobileSaleItem(product.sku, qty))
        }

        etPosQty.setText("1")
        updateCartTotals()
    }

    private fun updateCartTotals() {
        var subtotal = 0.0
        for (item in cartItems) {
            val prod = allProductsList.find { it.sku == item.sku }
            if (prod != null) {
                subtotal += prod.sellingPrice * item.quantity
            }
        }
        grandTotalWithVat = subtotal * 1.16

        val format = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        tvPosTotal.text = "Total Due (inc 16% VAT): ${format.format(grandTotalWithVat)}"

        recyclerView.adapter = CartAdapter(cartItems, allProductsList) { itemToRemove ->
            cartItems.remove(itemToRemove)
            updateCartTotals()
        }
    }

    private fun handleCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty. Add items first.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMethod = spPaymentMethod.selectedItem.toString()

        if (selectedMethod.contains("M-Pesa")) {
            showMpesaPhoneDialog(selectedMethod)
        } else if (selectedMethod.contains("Contractor Tab")) {
            showContractorTabDialog(selectedMethod)
        } else {
            executeCheckoutRequest(selectedMethod, null)
        }
    }

    private fun showMpesaPhoneDialog(paymentMethod: String) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (phoneField, etPhone) = ModernDialogHelper.createStyledInputField(
            context, "M-Pesa Phone Number *", "e.g. 0797428075", "0797428075", InputType.TYPE_CLASS_PHONE
        )

        contentLayout.addView(phoneField)
        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "📲", "Paystack M-Pesa STK Push", "Total Payable: KES $grandTotalWithVat\nEnter customer phone number to send STK prompt:")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Send STK Push") { _, _ ->
                val phone = etPhone.text.toString().trim()
                if (phone.isEmpty()) return@setPositiveButton

                lifecycleScope.launch {
                    try {
                        val stkBody = mapOf("phone" to phone, "amount" to grandTotalWithVat)
                        val stkResp = api.triggerStkPush(stkBody)
                        if (stkResp.isSuccessful) {
                            Toast.makeText(this@PosActivity, "M-Pesa STK Push sent to $phone!", Toast.LENGTH_LONG).show()
                            executeCheckoutRequest(paymentMethod, null)
                        } else {
                            Toast.makeText(this@PosActivity, "STK Push failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@PosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showContractorTabDialog(paymentMethod: String) {
        if (allContractorsList.isEmpty()) {
            Toast.makeText(this, "No contractors registered in CRM.", Toast.LENGTH_LONG).show()
            return
        }

        val contractorNames = allContractorsList.map { "${it.companyName} (Debt: KES ${it.currentBalance} / Limit: KES ${it.creditLimit})" }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("🏗️ Select Contractor Account")
            .setItems(contractorNames) { _, which ->
                val selectedContractor = allContractorsList[which]
                executeCheckoutRequest(paymentMethod, selectedContractor.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeCheckoutRequest(paymentMethod: String, contractorId: Int?) {
        btnCheckout.isEnabled = false
        btnCheckout.text = "Processing Checkout..."

        lifecycleScope.launch {
            try {
                val req = MobileCheckoutRequest(
                    paymentMethod = paymentMethod,
                    contractorId = contractorId,
                    items = cartItems
                )

                val response = api.processCheckout(req)
                if (response.isSuccessful) {
                    val body = response.body()
                    val receiptNo = body?.receiptNumber ?: "RCPT-OK"
                    Toast.makeText(this@PosActivity, "Sale Complete! Receipt: $receiptNo", Toast.LENGTH_LONG).show()
                    cartItems.clear()
                    updateCartTotals()
                    loadPosData()
                } else {
                    Toast.makeText(this@PosActivity, "Checkout Failed (Check stock or credit limit).", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PosActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnCheckout.isEnabled = true
                btnCheckout.text = "✓ Complete Sale Checkout"
            }
        }
    }
}