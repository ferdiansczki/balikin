package com.example.balikin.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["returned_date"]),
        Index(value = ["due_date"]),
        Index(value = ["person_name"]),
        Index(value = ["category"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "item_name")
    val itemName: String,

    @ColumnInfo(name = "direction")
    val direction: String, // TransactionDirection enum name: "LENT" or "BORROWED"

    @ColumnInfo(name = "category")
    val category: String, // ItemCategory enum name: "ELECTRONICS", "BOOKS", etc.

    @ColumnInfo(name = "person_name")
    val personName: String,

    @ColumnInfo(name = "contact_info")
    val contactInfo: String? = null,

    @ColumnInfo(name = "lent_or_borrowed_date")
    val lentOrBorrowedDate: Long,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "returned_date")
    val returnedDate: Long? = null,

    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
