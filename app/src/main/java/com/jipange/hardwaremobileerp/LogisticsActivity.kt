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

class LogisticsActivity : AppCompatActivity() {

    private lateinit var vehiclesRecyclerView: RecyclerView
    private lateinit var deliveriesRecyclerView: RecyclerView

    private val api = ApiService.create()
    private val vehicleTypes = arrayOf("Pickup Truck (1 Ton)", "Flatbed Truck (3 Ton)", "Lorry (7 Ton)", "Heavy Tipper (15 Ton)", "Delivery Van")
    private val vehicleStatuses = arrayOf("Available", "Dispatched", "Under Maintenance")

    private var availableVehiclesList = listOf<Vehicle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistics)
        NavigationHelper.setupNavigation(this, R.id.nav_logistics)

        vehiclesRecyclerView = findViewById(R.id.vehiclesRecyclerView)
        deliveriesRecyclerView = findViewById(R.id.deliveriesRecyclerView)

        val btnAddVehicle = findViewById<MaterialButton>(R.id.btnAddVehicle)
        val btnDispatchDelivery = findViewById<MaterialButton>(R.id.btnDispatchDelivery)

        vehiclesRecyclerView.layoutManager = LinearLayoutManager(this)
        deliveriesRecyclerView.layoutManager = LinearLayoutManager(this)

        btnAddVehicle.setOnClickListener { showAddVehicleDialog() }
        btnDispatchDelivery.setOnClickListener { showDispatchDeliveryDialog() }

        loadLogisticsData()
    }

    private fun loadLogisticsData() {
        lifecycleScope.launch {
            try {
                val vehicles = api.getVehicles()
                vehiclesRecyclerView.adapter = VehicleAdapter(vehicles)
                availableVehiclesList = vehicles.filter { it.status.equals("Available", ignoreCase = true) }
            } catch (e: Exception) {
                Toast.makeText(this@LogisticsActivity, "Failed to load vehicles: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            try {
                val deliveries = api.getDeliveries()
                deliveriesRecyclerView.adapter = DeliveryAdapter(deliveries) { deliveryToComplete ->
                    markDeliveryCompleted(deliveryToComplete)
                }
            } catch (e: Exception) {
                Toast.makeText(this@LogisticsActivity, "Failed to load deliveries: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddVehicleDialog() {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (plateField, etPlate) = ModernDialogHelper.createStyledInputField(context, "License Plate *", "e.g. KDA 123A")
        val (typeField, spType) = ModernDialogHelper.createStyledSpinnerField(context, "Vehicle Type", vehicleTypes.toList(), 0)
        val (capField, etCap) = ModernDialogHelper.createStyledInputField(context, "Cargo Capacity", "e.g. 7 Tonnes")
        val (statusField, spStatus) = ModernDialogHelper.createStyledSpinnerField(context, "Initial Status", vehicleStatuses.toList(), 0)

        contentLayout.addView(plateField)
        contentLayout.addView(typeField)
        contentLayout.addView(capField)
        contentLayout.addView(statusField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "🚚", "Register Fleet Vehicle", "Add transport vehicle to fleet inventory")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Register Vehicle") { _, _ ->
                val plate = etPlate.text.toString().trim().uppercase()
                val type = spType.selectedItem.toString()
                val cap = etCap.text.toString().trim()
                val status = spStatus.selectedItem.toString()

                if (plate.isEmpty()) {
                    Toast.makeText(this, "License Plate is required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val v = Vehicle(licensePlate = plate, vehicleType = type, capacity = cap, status = status)
                        val resp = api.createVehicle(v)
                        if (resp.isSuccessful) {
                            Toast.makeText(this@LogisticsActivity, "Vehicle registered successfully!", Toast.LENGTH_SHORT).show()
                            loadLogisticsData()
                        } else {
                            Toast.makeText(this@LogisticsActivity, "Registration failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LogisticsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDispatchDeliveryDialog() {
        if (availableVehiclesList.isEmpty()) {
            Toast.makeText(this, "No available trucks found to dispatch. Register an available truck first.", Toast.LENGTH_LONG).show()
            return
        }

        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (invField, etInv) = ModernDialogHelper.createStyledInputField(context, "Invoice / Sale No. *", "e.g. RCPT-1001")
        val (destField, etDest) = ModernDialogHelper.createStyledInputField(context, "Destination Site *", "e.g. Westlands Construction Site")
        val (driverField, etDriver) = ModernDialogHelper.createStyledInputField(context, "Driver Full Name *", "e.g. Samuel Kamau")

        val vehiclePlates = availableVehiclesList.map { "${it.licensePlate} (${it.vehicleType} - ${it.capacity})" }
        val (vehicleField, vehicleSpinner) = ModernDialogHelper.createStyledSpinnerField(context, "Assign Available Vehicle", vehiclePlates, 0)

        contentLayout.addView(invField)
        contentLayout.addView(destField)
        contentLayout.addView(driverField)
        contentLayout.addView(vehicleField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)
        val headerView = ModernDialogHelper.createHeaderView(context, "📦", "Dispatch Delivery Load", "Assign driver and truck for material delivery order")

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Dispatch Delivery") { _, _ ->
                val inv = etInv.text.toString().trim()
                val dest = etDest.text.toString().trim()
                val driver = etDriver.text.toString().trim()
                val selectedIdx = vehicleSpinner.selectedItemPosition

                if (inv.isEmpty() || dest.isEmpty() || driver.isEmpty() || selectedIdx < 0) {
                    Toast.makeText(this, "Invoice, Destination, Driver, and Vehicle are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedVehicle = availableVehiclesList[selectedIdx]

                lifecycleScope.launch {
                    try {
                        val body = mapOf(
                            "invoiceNumber" to inv,
                            "destination" to dest,
                            "driverName" to driver,
                            "licensePlate" to selectedVehicle.licensePlate
                        )

                        val resp = api.dispatchDelivery(body)
                        if (resp.isSuccessful) {
                            Toast.makeText(this@LogisticsActivity, "Delivery load dispatched!", Toast.LENGTH_SHORT).show()
                            loadLogisticsData()
                        } else {
                            Toast.makeText(this@LogisticsActivity, "Dispatch failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LogisticsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markDeliveryCompleted(delivery: Delivery) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Delivery Completion")
            .setMessage("Mark delivery #${delivery.invoiceNumber} to ${delivery.destination} as Delivered?")
            .setPositiveButton("Mark Delivered") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val resp = api.markDelivered(delivery.id, mapOf("status" to "Delivered"))
                        if (resp.isSuccessful) {
                            Toast.makeText(this@LogisticsActivity, "Delivery marked as Delivered!", Toast.LENGTH_SHORT).show()
                            loadLogisticsData()
                        } else {
                            Toast.makeText(this@LogisticsActivity, "Status update failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LogisticsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
