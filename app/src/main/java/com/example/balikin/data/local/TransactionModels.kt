package com.example.balikin.data.local

/**
 * Direction of the transaction:
 * - LENT: User lends their item to someone else (Dipinjamkan)
 * - BORROWED: User borrows someone else's item (Dipinjam)
 */
enum class TransactionDirection(val displayName: String) {
    LENT("Dipinjamkan"),
    BORROWED("Dipinjam")
}

/**
 * 8 Predefined categories as specified in issue.md.
 * Note: BOOKS is labeled as "Buku" to keep it distinct from DOCUMENTS ("Surat & Dokumen Berharga").
 */
enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Elektronik"),
    BOOKS("Buku"),
    CLOTHING("Pakaian & Aksesori"),
    TOOLS("Peralatan & Perkakas"),
    VEHICLES("Kendaraan"),
    DOCUMENTS("Surat & Dokumen Berharga"),
    HOUSEHOLD("Perlengkapan Rumah"),
    MONEY("Uang"),
    OTHER("Lainnya")
}
