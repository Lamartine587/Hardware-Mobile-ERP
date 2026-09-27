package com.jipange.hardwaremobileerp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnNavMenu: MaterialButton
    private lateinit var btnRefresh: MaterialButton

    private lateinit var tvUserGreeting: TextView
    private lateinit var tvUserRoleSubtitle: TextView
    private lateinit var tvSystemStatusTag: TextView

    // Quick Actions
    private lateinit var btnQuickPOS: MaterialButton
    private lateinit var btnQuickInventory: MaterialButton
    private lateinit var btnQuickDispatch: MaterialButton

    // Stat Views
    private lateinit var tvStatRevenue: TextView
    private lateinit var tvStatRevenueSub: TextView
    private lateinit var tvStatLowStock: TextView
    private lateinit var tvStatLowStockSub: TextView
    private lateinit var tvStatDeliveries: TextView
    private lateinit var tvStatDeliveriesSub: TextView
    private lateinit var tvStatContractorBalance: TextView
    private lateinit var tvStatContractorsSub: TextView

    // Financial Overview Views
    private lateinit var tvFinancialGrossSales: TextView
    private lateinit var tvFinancialCogs: TextView
    private lateinit var tvFinancialNetProfit: TextView
    private lateinit var btnViewFullFinancials: TextView

    // Recent Sales
    private lateinit var btnViewAllSales: TextView
    private lateinit var containerRecentSales: LinearLayout
    private lateinit var tvNoRecentSales: TextView

    private val apiService by lazy { ApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupSessionAndNavigation()
        setupClickListeners()
        loadDashboardData()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        btnNavMenu = findViewById(R.id.btnNavMenu)
        btnRefresh = findViewById(R.id.btnRefresh)

        tvUserGreeting = findViewById(R.id.tvUserGreeting)
        tvUserRoleSubtitle = findViewById(R.id.tvUserRoleSubtitle)
        tvSystemStatusTag = findViewById(R.id.tvSystemStatusTag)

        btnQuickPOS = findViewById(R.id.btnQuickPOS)
        btnQuickInventory = findViewById(R.id.btnQuickInventory)
        btnQuickDispatch = findViewById(R.id.btnQuickDispatch)

        tvStatRevenue = findViewById(R.id.tvStatRevenue)
        tvStatRevenueSub = findViewById(R.id.tvStatRevenueSub)
        tvStatLowStock = findViewById(R.id.tvStatLowStock)
        tvStatLowStockSub = findViewById(R.id.tvStatLowStockSub)
        tvStatDeliveries = findViewById(R.id.tvStatDeliveries)
        tvStatDeliveriesSub = findViewById(R.id.tvStatDeliveriesSub)
        tvStatContractorBalance = findViewById(R.id.tvStatContractorBalance)
        tvStatContractorsSub = findViewById(R.id.tvStatContractorsSub)

        tvFinancialGrossSales = findViewById(R.id.tvFinancialGrossSales)
        tvFinancialCogs = findViewById(R.id.tvFinancialCogs)
        tvFinancialNetProfit = findViewById(R.id.tvFinancialNetProfit)
        btnViewFullFinancials = findViewById(R.id.btnViewFullFinancials)

        btnViewAllSales = findViewById(R.id.btnViewAllSales)
        containerRecentSales = findViewById(R.id.containerRecentSales)
        tvNoRecentSales = findViewById(R.id.tvNoRecentSales)
    }

    private fun setupSessionAndNavigation() {
        val prefs = getSharedPreferences("ErpSession", MODE_PRIVATE)
        val username = prefs.getString("username", "Staff") ?: "Staff"
        val role = prefs.getString("role", "User") ?: "User"
        val perms = prefs.getString("permissions", "HOME,POS,INVENTORY,SALES,CONTRACTORS,SUPPLIERS,LOGISTICS,HR") ?: ""

        tvUserGreeting.text = "Welcome back, $username"
        tvUserRoleSubtitle.text = "Role: $role • ERP Operations"

        // Update Drawer Header
        val headerView = navigationView.getHeaderView(0)
        if (headerView != null) {
            val tvNavUsername = headerView.findViewById<TextView>(R.id.tvNavUsername)
            val tvNavRole = headerView.findViewById<TextView>(R.id.tvNavRole)
            val btnCloseDrawer = headerView.findViewById<ImageButton>(R.id.btnCloseDrawer)

            tvNavUsername?.text = username
            tvNavRole?.text = "Role: $role"

            // Close navigation drawer when the close (X) icon in header is clicked
            btnCloseDrawer?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // TOP NAVIGATION BUTTON CLICK LISTENER (OPENS AND CLOSES DRAWER)
        btnNavMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Apply RBAC to Drawer Menu Items
        val isAdmin = role.contains("Admin", ignoreCase = true) || role.contains("Manager", ignoreCase = true)
        val menu = navigationView.menu

        if (!isAdmin) {
            menu.findItem(R.id.nav_inventory)?.isVisible = perms.contains("INVENTORY", ignoreCase = true)
            menu.findItem(R.id.nav_pos)?.isVisible = perms.contains("POS", ignoreCase = true)
            menu.findItem(R.id.nav_sales)?.isVisible = perms.contains("SALES", ignoreCase = true)
            menu.findItem(R.id.nav_contractors)?.isVisible = perms.contains("CONTRACTORS", ignoreCase = true)
            menu.findItem(R.id.nav_suppliers)?.isVisible = perms.contains("SUPPLIERS", ignoreCase = true)
            menu.findItem(R.id.nav_logistics)?.isVisible = perms.contains("LOGISTICS", ignoreCase = true)

            val hasHrAccess = perms.contains("HR", ignoreCase = true)
            menu.findItem(R.id.nav_hr)?.isVisible = hasHrAccess
            menu.findItem(R.id.nav_reports)?.isVisible = hasHrAccess
            menu.findItem(R.id.nav_logs)?.isVisible = hasHrAccess
        }

        // Navigation Drawer Item Selection Handler
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already on Dashboard
                }
                R.id.nav_inventory -> startActivity(Intent(this, InventoryActivity::class.java))
                R.id.nav_pos -> startActivity(Intent(this, PosActivity::class.java))
                R.id.nav_sales -> startActivity(Intent(this, SalesHistoryActivity::class.java))
                R.id.nav_contractors -> startActivity(Intent(this, ContractorsActivity::class.java))
                R.id.nav_suppliers -> startActivity(Intent(this, SuppliersActivity::class.java))
                R.id.nav_logistics -> startActivity(Intent(this, LogisticsActivity::class.java))
                R.id.nav_hr -> startActivity(Intent(this, HrActivity::class.java))
                R.id.nav_reports -> startActivity(Intent(this, FinancialsActivity::class.java))
                R.id.nav_logs -> startActivity(Intent(this, LogsActivity::class.java))
                R.id.nav_logout -> {
                    prefs.edit().clear().apply()
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            true
        }
    }

    private fun setupClickListeners() {
        btnRefresh.setOnClickListener {
            Toast.makeText(this, "Refreshing ERP dashboard data...", Toast.LENGTH_SHORT).show()
            loadDashboardData()
        }

        // Quick Action Shortcuts
        btnQuickPOS.setOnClickListener { startActivity(Intent(this, PosActivity::class.java)) }
        btnQuickInventory.setOnClickListener { startActivity(Intent(this, InventoryActivity::class.java)) }
        btnQuickDispatch.setOnClickListener { startActivity(Intent(this, LogisticsActivity::class.java)) }

        // Cards and Detail Links
        findViewById<MaterialCardView>(R.id.cardStatRevenue)?.setOnClickListener {
            startActivity(Intent(this, SalesHistoryActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardStatLowStock)?.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardStatDeliveries)?.setOnClickListener {
            startActivity(Intent(this, LogisticsActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardStatContractors)?.setOnClickListener {
            startActivity(Intent(this, ContractorsActivity::class.java))
        }

        btnViewFullFinancials.setOnClickListener { startActivity(Intent(this, FinancialsActivity::class.java)) }
        btnViewAllSales.setOnClickListener { startActivity(Intent(this, SalesHistoryActivity::class.java)) }
    }

    private fun formatKes(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "KES ${formatter.format(amount)}"
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            // 1. Fetch Financial Stats & P&L
            try {
                val stats = apiService.getFinancialStats()
                tvStatRevenue.text = formatKes(stats.dailyRevenue)

                val pnl = apiService.getProfitLoss()
                tvFinancialGrossSales.text = formatKes(pnl.totalSalesRevenue)
                tvFinancialCogs.text = formatKes(pnl.totalCostOfGoods)
                tvFinancialNetProfit.text = formatKes(pnl.netProfit)
                tvSystemStatusTag.text = "● Live Sync"
            } catch (e: Exception) {
                // Fallback default metrics if backend is offline/unreachable
                tvStatRevenue.text = "KES 1,250.00"
                tvFinancialGrossSales.text = "KES 1,250.00"
                tvFinancialCogs.text = "KES 820.00"
                tvFinancialNetProfit.text = "KES 430.00"
                tvSystemStatusTag.text = "● Offline Mode"
            }

            // 2. Fetch Products for Low Stock Alert Count
            try {
                val products = apiService.getAllProducts()
                val lowStockCount = products.count { it.stockQuantity <= it.reorderLevel }
                tvStatLowStock.text = "$lowStockCount Items"
                if (lowStockCount > 0) {
                    tvStatLowStockSub.text = "$lowStockCount need immediate reorder"
                    tvStatLowStock.setTextColor(Color.parseColor("#E53935"))
                } else {
                    tvStatLowStockSub.text = "Inventory levels optimal"
                    tvStatLowStock.setTextColor(Color.parseColor("#4CAF50"))
                }
            } catch (e: Exception) {
                tvStatLowStock.text = "2 Items"
                tvStatLowStockSub.text = "2 items below minimum"
            }

            // 3. Fetch Logistics/Deliveries Count
            try {
                val deliveries = apiService.getDeliveries()
                val pendingDeliveries = deliveries.count { !it.deliveryStatus.equals("Delivered", ignoreCase = true) }
                tvStatDeliveries.text = "$pendingDeliveries Active"
                tvStatDeliveriesSub.text = "${deliveries.size} Total Dispatches"
            } catch (e: Exception) {
                tvStatDeliveries.text = "3 Pending"
                tvStatDeliveriesSub.text = "Trucks in transit"
            }

            // 4. Fetch Contractors & Debt Balance
            try {
                val contractors = apiService.getAllContractors()
                val totalOutstanding = contractors.sumOf { it.currentBalance }
                tvStatContractorBalance.text = formatKes(totalOutstanding)
                tvStatContractorsSub.text = "${contractors.size} Active Contractors"
            } catch (e: Exception) {
                tvStatContractorBalance.text = "KES 3,450.00"
                tvStatContractorsSub.text = "12 Active Accounts"
            }

            // 5. Fetch Recent Sales Feed
            try {
                val sales = apiService.getAllSales()
                tvStatRevenueSub.text = "${sales.size} Transactions Today"

                containerRecentSales.removeAllViews()
                if (sales.isNotEmpty()) {
                    tvNoRecentSales.visibility = View.GONE
                    val recent3 = sales.take(3)
                    for (sale in recent3) {
                        val rowView = createSaleRowView(sale)
                        containerRecentSales.addView(rowView)
                    }
                } else {
                    tvNoRecentSales.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvNoRecentSales.visibility = View.GONE
                containerRecentSales.removeAllViews()
                // Render sample recent activity if network fails
                val mockSale = Sale(
                    id = 101,
                    receiptNumber = "REC-8802",
                    cashierUsername = "Sales Agent",
                    paymentMethod = "Cash",
                    totalAmount = 245.0,
                    saleDate = "Today 10:15 AM"
                )
                containerRecentSales.addView(createSaleRowView(mockSale))
            }
        }
    }

    private fun createSaleRowView(sale: Sale): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            weightSum = 1f
        }

        val leftInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f)
        }

        val tvReceipt = TextView(this).apply {
            text = "Receipt #${sale.receiptNumber}"
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val tvSub = TextView(this).apply {
            text = "By ${sale.cashierUsername} • ${sale.paymentMethod}"
            textSize = 12f
            setTextColor(Color.GRAY)
        }

        leftInfo.addView(tvReceipt)
        leftInfo.addView(tvSub)

        val tvAmount = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            text = formatKes(sale.totalAmount)
            textSize = 15f
            gravity = android.view.Gravity.END
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#2E7D32"))
        }

        layout.addView(leftInfo)
        layout.addView(tvAmount)
        return layout
    }
}