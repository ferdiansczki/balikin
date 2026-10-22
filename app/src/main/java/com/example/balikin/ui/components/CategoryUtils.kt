package com.example.balikin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.balikin.data.local.ItemCategory

object CategoryUtils {

    fun getIcon(categoryName: String): ImageVector {
        return when (categoryName) {
            ItemCategory.ELECTRONICS.name -> Icons.Outlined.Devices
            ItemCategory.BOOKS.name -> Icons.Outlined.Book
            ItemCategory.CLOTHING.name -> Icons.Outlined.Checkroom
            ItemCategory.TOOLS.name -> Icons.Outlined.Handyman
            ItemCategory.VEHICLES.name -> Icons.Outlined.DirectionsCar
            ItemCategory.DOCUMENTS.name -> Icons.Outlined.Description
            ItemCategory.HOUSEHOLD.name -> Icons.Outlined.Home
            ItemCategory.MONEY.name -> Icons.Outlined.Payments
            else -> Icons.Outlined.Inventory2
        }
    }

    fun getDisplayName(categoryName: String): String {
        return try {
            ItemCategory.valueOf(categoryName).displayName
        } catch (e: Exception) {
            "Lainnya"
        }
    }

    fun getColor(categoryName: String): Color {
        return when (categoryName) {
            ItemCategory.ELECTRONICS.name -> Color(0xFF1976D2)
            ItemCategory.BOOKS.name -> Color(0xFF7B1FA2)
            ItemCategory.CLOTHING.name -> Color(0xFFC2185B)
            ItemCategory.TOOLS.name -> Color(0xFFF57C00)
            ItemCategory.VEHICLES.name -> Color(0xFF00796B)
            ItemCategory.DOCUMENTS.name -> Color(0xFF5D4037)
            ItemCategory.HOUSEHOLD.name -> Color(0xFF388E3C)
            ItemCategory.MONEY.name -> Color(0xFF2E7D32)
            else -> Color(0xFF616161)
        }
    }
}
