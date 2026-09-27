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

class InventoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val api = ApiService.create()

    private val categories = arrayOf(
        "Roofing", "Fasteners", "Cement & Masonry", "Plumbing",
        "Electrical", "Paints & Finishings", "Steel & Iron",
        "Timber & Wood", "Tools & Hardware", "General"
    )

    private val units = arrayOf(
        "Pieces", "Kgs", "Meters", "Liters", "Bags", "Boxes", "Rolls", "Pairs", "Sets"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)
        NavigationHelper.setupNavigation(this, R.id.nav_inventory)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        val btnAddProduct = findViewById<MaterialButton>(R.id.btnAddProduct)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddProduct.setOnClickListener {
            showProductFormDialog(null)
        }

        loadInventory()
    }

    private fun loadInventory() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val products = api.getAllProducts()
                progressBar.visibility = View.GONE
                recyclerView.adapter = ProductAdapter(products) { selectedProduct ->
                    showProductFormDialog(selectedProduct)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@InventoryActivity, "Failed to load inventory: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showProductFormDialog(productToEdit: Product?) {
        val context = this
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val (skuField, etSku) = ModernDialogHelper.createStyledInputField(
            context, "SKU / Barcode *", "e.g. SHT-G28", productToEdit?.sku ?: "", enabled = productToEdit == null
        )
        val (nameField, etName) = ModernDialogHelper.createStyledInputField(
            context, "Product Name *", "e.g. Corrugated Iron Sheet", productToEdit?.name ?: ""
        )

        val catIdx = if (productToEdit != null) categories.indexOfFirst { it.equals(productToEdit.category, ignoreCase = true) }.coerceAtLeast(0) else 0
        val (catField, spCategory) = ModernDialogHelper.createStyledSpinnerField(
            context, "Category", categories.toList(), catIdx
        )

        val (specField, etSpec) = ModernDialogHelper.createStyledInputField(
            context, "Specification", "e.g. Gauge 28 / 4 Inch", productToEdit?.specification ?: ""
        )

        val unitIdx = if (productToEdit != null) units.indexOfFirst { it.equals(productToEdit.unitOfMeasure, ignoreCase = true) }.coerceAtLeast(0) else 0
        val (unitField, spUnit) = ModernDialogHelper.createStyledSpinnerField(
            context, "Unit of Measure", units.toList(), unitIdx
        )

        val (costField, etCostPrice) = ModernDialogHelper.createStyledInputField(
            context, "Cost Price (KES)", "0.0", productToEdit?.costPrice?.toString() ?: "0.0", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        val (priceField, etPrice) = ModernDialogHelper.createStyledInputField(
            context, "Selling Price (KES) *", "0.0", productToEdit?.sellingPrice?.toString() ?: "0.0", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        val (stockField, etStock) = ModernDialogHelper.createStyledInputField(
            context, "Stock Quantity", "100", productToEdit?.stockQuantity?.toString() ?: "100", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        val (reorderField, etReorder) = ModernDialogHelper.createStyledInputField(
            context, "Reorder Stock Level", "10", productToEdit?.reorderLevel?.toString() ?: "10", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        contentLayout.addView(skuField)
        contentLayout.addView(nameField)
        contentLayout.addView(catField)
        contentLayout.addView(specField)
        contentLayout.addView(unitField)
        contentLayout.addView(costField)
        contentLayout.addView(priceField)
        contentLayout.addView(stockField)
        contentLayout.addView(reorderField)

        val scrollView = ModernDialogHelper.createFormScrollView(context, contentLayout)

        val isEdit = productToEdit != null
        val title = if (!isEdit) "Add New Product" else "Edit Product [${productToEdit?.sku}]"
        val subtitle = if (!isEdit) "Fill out stock item fields to register in master inventory" else "Update product details and pricing"
        val headerView = ModernDialogHelper.createHeaderView(context, "📦", title, subtitle)

        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headerView)
            addView(scrollView)
        }

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton(if (!isEdit) "Save Product" else "Update Product") { _, _ ->
                val sku = etSku.text.toString().trim()
                val name = etName.text.toString().trim()
                val cat = spCategory.selectedItem.toString()
                val spec = etSpec.text.toString().trim()
                val unit = spUnit.selectedItem.toString()
                val cost = etCostPrice.text.toString().toDoubleOrNull() ?: 0.0
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val stock = etStock.text.toString().toDoubleOrNull() ?: 0.0
                val reorder = etReorder.text.toString().toDoubleOrNull() ?: 10.0

                if (sku.isEmpty() || name.isEmpty()) {
                    Toast.makeText(context, "SKU and Product Name are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val product = Product(
                            id = productToEdit?.id ?: 0,
                            sku = sku,
                            name = name,
                            category = cat,
                            specification = spec,
                            unitOfMeasure = unit,
                            costPrice = cost,
                            sellingPrice = price,
                            stockQuantity = stock,
                            reorderLevel = reorder
                        )
                        val resp = if (!isEdit) api.createProduct(product) else api.updateProduct(product)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, if (!isEdit) "Product saved successfully!" else "Product updated!", Toast.LENGTH_SHORT).show()
                            loadInventory()
                        } else {
                            Toast.makeText(context, "Operation failed.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}