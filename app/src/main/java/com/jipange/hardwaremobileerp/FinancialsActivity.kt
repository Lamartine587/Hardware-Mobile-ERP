package com.jipange.hardwaremobileerp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class FinancialsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financials)
        NavigationHelper.setupNavigation(this, R.id.nav_reports)

        val tvRevenue = findViewById<TextView>(R.id.tvRevenue)
        val tvCogs = findViewById<TextView>(R.id.tvCogs)
        val tvGrossProfit = findViewById<TextView>(R.id.tvGrossProfit)
        val tvPending = findViewById<TextView>(R.id.tvPending)
        val ledgerRecyclerView = findViewById<RecyclerView>(R.id.ledgerRecyclerView)

        ledgerRecyclerView?.layoutManager = LinearLayoutManager(this)

        val api = ApiService.create()
        val currencyFormatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        lifecycleScope.launch {
            try {
                val pnl = api.getProfitLoss()
                val ledger = api.getGeneralLedger()

                tvRevenue?.text = "KES ${currencyFormatter.format(pnl.totalSalesRevenue)}"
                tvCogs?.text = "KES ${currencyFormatter.format(pnl.totalCostOfGoods)}"
                tvGrossProfit?.text = "KES ${currencyFormatter.format(pnl.grossProfit)} (${String.format("%.1f", pnl.grossMarginPercent)}%)"
                tvPending?.text = "KES ${currencyFormatter.format(pnl.netProfit)}"

                ledgerRecyclerView?.adapter = LedgerAdapter(ledger)
            } catch (e: Exception) {
                Toast.makeText(this@FinancialsActivity, "Financial load error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}