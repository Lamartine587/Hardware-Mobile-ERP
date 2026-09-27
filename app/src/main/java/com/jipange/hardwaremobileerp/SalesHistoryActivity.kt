package com.jipange.hardwaremobileerp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class SalesHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)
        NavigationHelper.setupNavigation(this, R.id.nav_sales)

        val recyclerView = findViewById<RecyclerView>(R.id.salesRecyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSales)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val api = ApiService.create()

        lifecycleScope.launch {
            try {
                val sales = api.getAllSales()
                progressBar.visibility = View.GONE
                recyclerView.adapter = SalesAdapter(sales) { selectedSale ->
                    showSaleDetailsDialog(selectedSale, api)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@SalesHistoryActivity, "Error loading sales history: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSaleDetailsDialog(sale: Sale, api: ApiService) {
        lifecycleScope.launch {
            try {
                val items = api.getSaleItems(sale.id)
                val itemSummary = items.joinToString("\n") { "• ${it.name} (Qty: ${it.quantity}) - KES ${it.subtotal}" }

                AlertDialog.Builder(this@SalesHistoryActivity)
                    .setTitle("Receipt #${sale.receiptNumber}")
                    .setMessage(
                        "Cashier: ${sale.cashierUsername}\n" +
                        "Payment Method: ${sale.paymentMethod}\n" +
                        "Date: ${sale.saleDate ?: "N/A"}\n" +
                        "Total: KES ${sale.totalAmount}\n\n" +
                        "Line Items:\n" + if (items.isEmpty()) "No items found." else itemSummary
                    )
                    .setPositiveButton("Close", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@SalesHistoryActivity, "Error loading details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
