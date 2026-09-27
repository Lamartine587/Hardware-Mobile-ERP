package com.jipange.hardwaremobileerp

data class Product(
    val id: Int = 0,
    val sku: String,
    val name: String,
    val category: String = "General",
    val specification: String = "",
    val unitOfMeasure: String = "Pieces",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val reorderLevel: Double = 10.0
)