package com.jipange.hardwaremobileerp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

object NavigationHelper {

    fun setupNavigation(
        activity: Activity,
        currentNavId: Int
    ) {
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawerLayout) ?: return
        val navigationView = activity.findViewById<NavigationView>(R.id.navigationView) ?: return
        val btnNavMenu = activity.findViewById<MaterialButton>(R.id.btnNavMenu)

        val prefs = activity.getSharedPreferences("ErpSession", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "Staff") ?: "Staff"
        val role = prefs.getString("role", "User") ?: "User"
        val perms = prefs.getString("permissions", "HOME,POS,INVENTORY,SALES,CONTRACTORS,SUPPLIERS,LOGISTICS,HR") ?: ""

        // Update Drawer Header
        val headerView = navigationView.getHeaderView(0)
        if (headerView != null) {
            val tvNavUsername = headerView.findViewById<TextView>(R.id.tvNavUsername)
            val tvNavRole = headerView.findViewById<TextView>(R.id.tvNavRole)
            val btnCloseDrawer = headerView.findViewById<ImageButton>(R.id.btnCloseDrawer)

            tvNavUsername?.text = username
            tvNavRole?.text = "Role: $role"

            btnCloseDrawer?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // Toggle button click listener (opens if closed, closes if open)
        btnNavMenu?.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Apply RBAC permissions
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

        // Highlight current menu item
        if (currentNavId != 0) {
            menu.findItem(currentNavId)?.isChecked = true
        }

        // Menu item click listener
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            if (item.itemId == currentNavId) {
                return@setNavigationItemSelectedListener true
            }

            when (item.itemId) {
                R.id.nav_dashboard -> {
                    activity.startActivity(Intent(activity, DashboardActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_inventory -> {
                    activity.startActivity(Intent(activity, InventoryActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_pos -> {
                    activity.startActivity(Intent(activity, PosActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_sales -> {
                    activity.startActivity(Intent(activity, SalesHistoryActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_contractors -> {
                    activity.startActivity(Intent(activity, ContractorsActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_suppliers -> {
                    activity.startActivity(Intent(activity, SuppliersActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_logistics -> {
                    activity.startActivity(Intent(activity, LogisticsActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_hr -> {
                    activity.startActivity(Intent(activity, HrActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_reports -> {
                    activity.startActivity(Intent(activity, FinancialsActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_logs -> {
                    activity.startActivity(Intent(activity, LogsActivity::class.java))
                    if (activity !is DashboardActivity) activity.finish()
                }
                R.id.nav_logout -> {
                    prefs.edit().clear().apply()
                    Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
            true
        }
    }
}
