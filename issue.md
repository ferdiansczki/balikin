# Issue: [Feature] Balikin - Personal Lending and Borrowing Tracker

## 📝 Description
**Balikin** is an offline-first personal lending and borrowing tracker Android application designed to help a single user keep complete, accurate, and easily searchable records of items they lend to others (*Dipinjamkan*) and items they borrow from others (*Dipinjam*).

In daily life, people frequently lend books, gadgets, tools, clothing, money, documents, or vehicles to friends, family, and colleagues, or borrow equipment for short-term use. Without a dedicated tracking system, people forget who holds their items, miss agreed return deadlines, misplace contact details, or lose track of borrowed property, leading to awkward social situations and financial loss.

Balikin solves this problem by providing:
- A unified personal ledger for both lending directions (*Dipinjamkan* and *Dipinjam*).
- Clear tracking of active, overdue, and completed transactions.
- Offline-first local data storage using Android Room (SQLite) with zero login, zero backend, and zero cloud dependencies.
- Local photo storage linking item images saved securely in internal app storage.
- Local notification reminders for upcoming deadlines and overdue items using a stable, throttled strategy.
- Simple, beautiful Material 3 user interface in Indonesian tailored for high usability.

---

## ⚖️ Implementation Clarifications & Conflict Resolution
*(Note: These clarifications take precedence over any conflicting requirements elsewhere in this issue).*

1. **Notification Scheduling**:
   - Use one consistent scheduling strategy for the MVP: Android `WorkManager` periodic background check (interval: 24 hours), accounting for Android battery/Doze optimization (do not promise exact execution at fixed clock times such as 08:00 AM).
   - Use stable notification IDs based on the transaction primary key (`transaction.id.toInt()`) so multiple updates for the same item update the existing notification instead of stacking duplicates.
   - Throttle overdue notifications (e.g., notify on the day it becomes overdue and at most once every 3 days thereafter, or once per status transition) to avoid spamming the user.
   - Cancelling, scheduling, or delivering reminders must never alter transaction database records.
2. **Transaction Status & Reactive UI**:
   - The database stores only `returned_date`, never a statically saved `ACTIVE` or `OVERDUE` string column.
   - All unreturned transactions appear on Beranda (Home), including overdue items.
   - Status must be dynamically computed using one centralized, shared status-calculation function: `calculateStatus(returnedDate, dueDate, currentTime)`.
   - The UI and statistics must refresh time-dependent status when the app resumes (`Lifecycle.Event.ON_RESUME`) and when the local calendar date changes.
   - Statistics on the Statistik screen must derive metrics from this exact same calculation rule.
3. **Photo Storage**:
   - Store all item photos exclusively in one dedicated internal directory: `context.filesDir/item_photos/`.
   - Store consistent relative filenames (e.g., `item_<uuid>.<ext>`) or consistent absolute paths in `photo_path`.
   - Preserve original image format and file extension: determine MIME type from `ContentResolver` (e.g., `.png`, `.jpg`, `.webp`) and never label a copied PNG as JPEG without converting.
   - Handle photo replacement, removal, and database save failures safely (if database write fails, delete the newly copied image file to avoid orphans).
   - Do not delete an existing photo file upon transaction deletion if another transaction still references the same file path.
4. **Build & Environment Verification**:
   - Inspect actual project files, Gradle configuration, and SDK availability before modifying files.
   - Standard build tasks (such as `gradle assembleDebug` or `compile_applet`) should be used as supported by the environment.
   - If compilation or device testing cannot be executed, explicitly report that limitation instead of falsely claiming success.
5. **Database Safety**:
   - `fallbackToDestructiveMigration()` is permitted only during initial rapid MVP prototyping.
   - Document any schema changes that could clear existing tables. Do not treat destructive migration as an undocumented production strategy.
6. **Date & Enum Consistency**:
   - All stored timestamps use epoch milliseconds (`Long`).
   - Date-only comparisons and validations must normalize to the device's local calendar date (Year, Month, Day at midnight), avoiding arbitrary millisecond time comparisons within the same day.
   - Categories use validated enum strings or Room `@TypeConverter`.
   - The Books category label is `"Buku"` (Books), distinct from `"Surat & Dokumen Berharga"` (Documents).
7. **Data-Loss Expectations**:
   - Local records persist across normal app restarts and device reboots.
   - Data does NOT survive app uninstallation or clearing application data in Android OS settings.
   - Clearly distinguish clearing cache (database and photos kept intact) from clearing application data (database and photos erased).
8. **Final Verification**:
   - Never mark an acceptance criterion as complete based solely on generated code.
   - Explicitly report which tests and builds were actually executed, passed, failed, or skipped.

---

## 🎯 Acceptance Criteria (AC)

### 1. Navigation & Screen Flow
- **AC-1.1**: The app provides a persistent Material 3 `NavigationBar` with exactly three destinations:
  1. **Beranda** (Home) — displays active & overdue unreturned transactions, summary metrics, search bar, filters, and a Floating Action Button (FAB) to add a new transaction.
  2. **Riwayat** (History) — displays completed transactions with actual return dates, search bar, and filters.
  3. **Statistik** (Stats / Profile) — displays aggregated transaction metrics, notification preferences, app information, and local data status.
- **AC-1.2**: Tapping a transaction card on either the Beranda or Riwayat screen navigates to the **Transaction Detail Screen**.
- **AC-1.3**: Pressing the Android system Back button or the TopAppBar Back arrow from secondary screens (Detail Screen, Add/Edit Screen) safely navigates back to the originating screen without state loss or application crash.

### 2. Transaction Direction & Categories
- **AC-2.1**: Every transaction has a mandatory direction attribute:
  - `LENT` (*Dipinjamkan*: User lent their item to someone else).
  - `BORROWED` (*Dipinjam*: User borrowed someone else's item).
- **AC-2.2**: The app provides exactly 8 predefined categories with Indonesian labels:
  1. `ELECTRONICS` (*Elektronik*)
  2. `BOOKS` (*Buku*)
  3. `CLOTHING` (*Pakaian & Aksesori*)
  4. `TOOLS` (*Peralatan & Perkakas*)
  5. `VEHICLES` (*Kendaraan*)
  6. `DOCUMENTS` (*Surat & Dokumen Berharga*)
  7. `HOUSEHOLD` (*Perlengkapan Rumah*)
  8. `OTHER` (*Lainnya*)
- **AC-2.3**: Both lending directions share the same underlying database schema and table.

### 3. Transaction Creation & Input Validation
- **AC-3.1**: The Add/Edit form requires the following mandatory fields:
  - Transaction Direction (`Dipinjamkan` vs `Dipinjam`).
  - Item Name (`Nama Barang`): non-blank text string (trimmed length >= 1).
  - Person's Name (`Nama Peminjam / Pemilik`): non-blank text string (trimmed length >= 1).
  - Category (`Kategori`): one of the 8 predefined categories.
  - Transaction Date (`Tanggal Pinjam`): valid epoch date in milliseconds.
  - Due Date (`Batas Pengembalian`): valid epoch date in milliseconds.
- **AC-3.2**: The form provides the following optional fields:
  - Contact Information (`Nomor Kontak / WhatsApp / Catatan Kontak`): nullable string.
  - Item Photo (`Foto Barang`): nullable local image file reference.
  - Notes (`Catatan Tambahan`): nullable string.
- **AC-3.3**: Date validation strictly enforces that calendar `dueDate` must be greater than or equal to calendar `lentOrBorrowedDate` based on the local calendar day. If `dueDate < lentOrBorrowedDate` by calendar day, submission is blocked and an inline error message is displayed: *"Batas tanggal pengembalian tidak boleh sebelum tanggal pinjam."*
- **AC-3.4**: If the item name or person name is blank on submit, the form blocks submission, displays clear validation error indicators under the corresponding text fields, and prevents database insertion.

### 4. Status Determination & Dynamic Lifecycle
- **AC-4.1**: Transaction state must be dynamically calculated based on `returnedDate` and `dueDate`:
  - `COMPLETED` (*Selesai*): `returnedDate != null`
  - `OVERDUE` (*Terlambat*): `returnedDate == null` AND `currentTimestamp > endOfDay(dueDate)`
  - `ACTIVE` (*Aktif*): `returnedDate == null` AND `currentTimestamp <= endOfDay(dueDate)`
- **AC-4.2**: Status must NOT be hardcoded as a static string column in the database. All unreturned transactions appear on Beranda.
- **AC-4.3**: Active transactions on Beranda clearly display visual status chips:
  - `ACTIVE`: Neutral/Primary badge with remaining days (e.g., *"Sisa 3 hari"* or *"Hari ini"*).
  - `OVERDUE`: Prominent Error/Red badge with overdue duration (e.g., *"Terlambat 2 hari"*).
- **AC-4.4**: Status dynamically refreshes whenever the application returns to foreground (`ON_RESUME`) or the date advances.

### 5. Return Processing ("Sudah Kembali")
- **AC-5.1**: On the Transaction Detail Screen or via a quick action button on the card, an active transaction can be marked as returned by tapping **"Tandai Sudah Kembali"**.
- **AC-5.2**: When triggered, a confirmation dialog appears allowing the user to confirm or choose the return date (defaulting to the current date/time).
- **AC-5.3**: Upon confirmation, `returnedDate` is recorded with the chosen timestamp, `updatedAt` is updated, any pending notification for this transaction is cancelled, and the transaction immediately transitions to `COMPLETED`.
- **AC-5.4**: Completed transactions automatically disappear from the Beranda (Home) list and appear in the Riwayat (History) list without data loss.

### 6. Edit & Delete Operations
- **AC-6.1**: An existing transaction can be edited from the Transaction Detail Screen. The form is pre-populated with existing data. On save, changes are persisted, `updatedAt` is refreshed, and reminders are reconciled.
- **AC-6.2**: An existing transaction can be deleted from the Transaction Detail Screen.
- **AC-6.3**: Deletion requires explicit user confirmation via an AlertDialog: *"Apakah Anda yakin ingin menghapus data transaksi ini? Tindakan ini tidak dapat dibatalkan."*
- **AC-6.4**: Upon confirming deletion, the transaction record is deleted from Room, its associated local photo file is safely removed from `context.filesDir/item_photos/` (provided no other transaction references the same file), any scheduled notifications are cancelled, and the user is navigated back to the previous list with a Snackbar confirmation.

### 7. Search & Filtering
- **AC-7.1**: Beranda (Home) provides real-time search filtering by item name or person name (case-insensitive substring match).
- **AC-7.2**: Beranda provides filter chips: **Semua** (All Unreturned), **Dipinjamkan** (Lent), and **Dipinjam** (Borrowed).
- **AC-7.3**: Riwayat (History) provides real-time search by item name or person name, and filter chips: **Semua** (All Completed), **Dipinjamkan**, and **Dipinjam**.
- **AC-7.4**: Filtering and search combinations must be reactive and instant, showing appropriate empty state indicators when no matching items exist.

### 8. Statistics & Summary Cards
- **AC-8.1**: The Beranda dashboard displays summary counters:
  - Total items lent (*Dipinjamkan Aktif*).
  - Total items borrowed (*Dipinjam Aktif*).
  - Total overdue items (*Jatuh Tempo / Terlambat*).
- **AC-8.2**: The Statistik screen displays aggregated metrics derived from actual database records using the shared status-calculation logic:
  - Total all-time transactions recorded.
  - Count of Active, Completed, and Overdue items.
  - Ratio / comparison of Lent vs. Borrowed items.
  - Breakdown of items by category.
- **AC-8.3**: All statistics update automatically via Room reactive queries (`Flow`) when any transaction is created, updated, marked returned, or deleted.

### 9. Local Item Photo Persistence
- **AC-9.1**: The user can pick an item photo using the modern Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- **AC-9.2**: The selected image is copied into `context.filesDir/item_photos/` preserving its native format (JPEG/PNG/WEBP) with a unique filename (e.g., `item_<uuid>.<ext>`).
- **AC-9.3**: The database stores the relative filename or consistent absolute local file path (`photoPath`).
- **AC-9.4**: Photo references remain accessible across application restarts and device reboots.
- **AC-9.5**: If an image file is missing, corrupted, or deleted outside the app, the UI gracefully renders a category-specific fallback vector icon without crashing or throwing unhandled exceptions.
- **AC-9.6**: If an image is replaced or the transaction is deleted, orphaned files are cleaned up safely without deleting files referenced by other items. If database insert/update fails, newly copied image files are cleaned up.

### 10. Local Notifications
- **AC-10.1**: The app schedules local notifications using `WorkManager` (periodic daily check), alerting the user for:
  - Upcoming return deadline (e.g., within 24 hours of `dueDate`).
  - Overdue reminder (when `dueDate` has passed and `returnedDate == null`).
- **AC-10.2**: Notifications use stable notification IDs based on `transaction.id.toInt()` and throttle overdue alerts to avoid redundant spam.
- **AC-10.3**: Notifications use a dedicated Android Notification Channel named *"Pengingat Balikin"* (*Return Reminders*).
- **AC-10.4**: On Android 13+ (API 33+), the app checks and requests `POST_NOTIFICATIONS` runtime permission with a user-friendly rationale. If denied, the app functions completely normally without error, still displaying accurate overdue flags inside the UI.
- **AC-10.5**: Notifications are cancelled or updated when a transaction is completed or deleted, without altering database records.

### 11. Data Persistence & Offline Isolation
- **AC-11.1**: All data is stored locally in a Room SQLite database named `balikin.db`.
- **AC-11.2**: The app requires no internet access (`android.permission.INTERNET` is not required).
- **AC-11.3**: All user records, notes, photos, and history survive app termination and device reboots. (Application uninstallation or user clearing app data in OS settings will erase local data).

---

## 🗄️ Database Schema

### 1. Room Entity: `TransactionEntity`
Table name: `transactions`

| Column Name | SQLite Data Type | Kotlin Type | Constraints / Defaults | Description |
|---|---|---|---|---|
| `id` | `INTEGER` | `Long` | `PRIMARY KEY AUTOINCREMENT` | Unique identifier for transaction |
| `item_name` | `TEXT` | `String` | `NOT NULL` | Name of the lent or borrowed item |
| `direction` | `TEXT` | `String` | `NOT NULL` | `"LENT"` or `"BORROWED"` |
| `category` | `TEXT` | `String` | `NOT NULL` | Category identifier (e.g., `"BOOKS"`) |
| `person_name` | `TEXT` | `String` | `NOT NULL` | Name of borrower or lender |
| `contact_info` | `TEXT` | `String?` | `NULLABLE` | Phone number or contact note |
| `lent_or_borrowed_date` | `INTEGER` | `Long` | `NOT NULL` | Epoch timestamp in milliseconds |
| `due_date` | `INTEGER` | `Long` | `NOT NULL` | Epoch timestamp in milliseconds |
| `returned_date` | `INTEGER` | `Long?` | `NULLABLE`, default `NULL` | Return timestamp if completed |
| `photo_path` | `TEXT` | `String?` | `NULLABLE` | Path relative to `item_photos/` or absolute path |
| `notes` | `TEXT` | `String?` | `NULLABLE` | Additional remarks / conditions |
| `created_at` | `INTEGER` | `Long` | `NOT NULL` | System creation time (epoch ms) |
| `updated_at` | `INTEGER` | `Long` | `NOT NULL` | Last edit time (epoch ms) |

### 2. Indexes
To ensure fast searching, filtering, and counter aggregation:
- `index_transactions_returned_date` on (`returned_date`)
- `index_transactions_due_date` on (`due_date`)
- `index_transactions_person_name` on (`person_name`)
- `index_transactions_category` on (`category`)

### 3. Kotlin Model & Enums
```kotlin
package com.example.balikin.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionDirection {
    LENT,      // Dipinjamkan
    BORROWED   // Dipinjam
}

enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Elektronik"),
    BOOKS("Buku"),
    CLOTHING("Pakaian & Aksesori"),
    TOOLS("Peralatan & Perkakas"),
    VEHICLES("Kendaraan"),
    DOCUMENTS("Surat & Dokumen Berharga"),
    HOUSEHOLD("Perlengkapan Rumah"),
    OTHER("Lainnya")
}

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
    val direction: String, // "LENT" or "BORROWED"

    @ColumnInfo(name = "category")
    val category: String, // ItemCategory enum name e.g. "BOOKS"

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
```

### 4. Room DAO: `TransactionDao`
```kotlin
package com.example.balikin.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun observeTransactionById(id: Long): Flow<TransactionEntity?>

    // Unreturned transactions (returned_date IS NULL) sorted by due_date ascending
    @Query("""
        SELECT * FROM transactions 
        WHERE returned_date IS NULL 
        ORDER BY due_date ASC
    """)
    fun observeActiveTransactions(): Flow<List<TransactionEntity>>

    // Completed transactions (returned_date IS NOT NULL) sorted by returned_date descending
    @Query("""
        SELECT * FROM transactions 
        WHERE returned_date IS NOT NULL 
        ORDER BY returned_date DESC
    """)
    fun observeCompletedTransactions(): Flow<List<TransactionEntity>>

    // All transactions for statistics
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    // Synchronous list for background worker
    @Query("SELECT * FROM transactions WHERE returned_date IS NULL")
    suspend fun getActiveTransactionsSync(): List<TransactionEntity>

    // Check if any other transaction uses the same photo path
    @Query("SELECT COUNT(*) FROM transactions WHERE photo_path = :photoPath AND id != :excludeId")
    suspend fun countOtherReferencesToPhoto(photoPath: String, excludeId: Long): Int

    // Quick mark as returned
    @Query("""
        UPDATE transactions 
        SET returned_date = :returnedDate, updated_at = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markAsReturned(id: Long, returnedDate: Long, updatedAt: Long = System.currentTimeMillis())
}
```

### 5. Room Database Class
```kotlin
package com.example.balikin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "balikin.db"
                )
                // Permitted during initial prototype only; note that schema changes will wipe local test data
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## 🔄 Data and State Management

### 1. Dynamic Status Determination
The application calculates status dynamically without persisting status text in the database.
```kotlin
package com.example.balikin.util

import java.util.Calendar

enum class TransactionStatus {
    ACTIVE,
    OVERDUE,
    COMPLETED
}

object StatusHelper {
    /**
     * Shared status calculation function used across UI, ViewModels, and Statistics.
     */
    fun calculateStatus(
        returnedDate: Long?,
        dueDate: Long,
        currentTime: Long = System.currentTimeMillis()
    ): TransactionStatus {
        if (returnedDate != null) return TransactionStatus.COMPLETED

        // End of the due date in local calendar timezone (23:59:59.999)
        val endOfDueDay = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return if (currentTime > endOfDueDay) {
            TransactionStatus.OVERDUE
        } else {
            TransactionStatus.ACTIVE
        }
    }

    /**
     * Validates that due date calendar day is >= lending date calendar day.
     */
    fun isDueDateValid(lendingDate: Long, dueDate: Long): Boolean {
        val startDay = Calendar.getInstance().apply {
            timeInMillis = lendingDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dueDay = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return dueDay >= startDay
    }
}
```

### 2. Time-Dependent Status Refresh
Because `OVERDUE` status depends on `System.currentTimeMillis()`, the UI state must refresh:
- Whenever the user returns to the app (`Lifecycle.Event.ON_RESUME`).
- Through a reactive clock state in the ViewModel that ticks when crossing midnight or on foregrounding.
- Ensuring the dashboard summary cards and transaction badges instantly reflect the transition to `OVERDUE`.

### 3. Single Source of Truth
- The database emits reactive `Flow` lists via Room.
- ViewModels filter and map these lists with `calculateStatus()`.
- Home, History, and Stats screens all derive their data from the same Room database instances, preventing discrepancies.

---

## 📱 UI and Navigation

### 1. Navigation Flow
- Navigation destinations:
  - `Screen.Home` (`"home"`): Beranda
  - `Screen.History` (`"history"`): Riwayat
  - `Screen.Stats` (`"stats"`): Statistik & Pengaturan
  - `Screen.Detail(transactionId: Long)`: Detail Transaksi
  - `Screen.AddEdit(transactionId: Long?)`: Tambah / Edit Transaksi

### 2. Screen Specifications

#### A. Beranda (Home)
- **Top Header**: Title `"Balikin"` with Indonesian subtitle (*"Catat pinjam-meminjam tanpa ribet"*).
- **Metric Cards**:
  - `Dipinjamkan`: Count of active lent items.
  - `Dipinjam`: Count of active borrowed items.
  - `Terlambat`: Count of overdue unreturned items.
- **Search Bar**: Real-time search filter for item name or person name.
- **Filter Chips**: `Semua`, `Dipinjamkan`, and `Dipinjam`.
- **List**: All unreturned items (both Active and Overdue) sorted by `due_date ASC`.
  - Overdue items are prominently flagged with red warning badges.
  - Active items show remaining days.
- **Floating Action Button (FAB)**: Primary `+` button navigating to Add Transaction.
- **Empty State**: Friendly illustration/icon with helpful copy when no active transactions exist.

#### B. Riwayat (History)
- **Header**: Title `"Riwayat Pengembalian"` with total completed counter badge.
- **Search Bar & Filter Chips**: Same search and direction filter as Home.
- **Completed Card**: Shows item name, person name, category, direction badge, and return date (*"Dikembalikan pada: 14 Okt 2026"*).
- **Empty State**: *"Belum ada riwayat pengembalian barang."*

#### C. Statistik & Pengaturan (Stats / Profile)
- **Summary Metrics**:
  - Total all-time items recorded.
  - Total Selesai (Completed), Aktif (Active), and Terlambat (Overdue).
  - Completion rate percentage.
- **Lent vs. Borrowed Comparison**: Visual representation comparing items lent vs. items borrowed.
- **Category Breakdown**: Count of items per category.
- **Notification Settings**:
  - Toggle: `"Aktifkan Notifikasi Pengingat"` (enables/disables WorkManager reminders).
  - Notification permission banner with request button if permission is ungranted on Android 13+.
- **App Information**:
  - Balikin v1.0.
  - Clarified offline notice: *"100% Data Tersimpan Aman di Perangkat Anda (Data akan hilang jika aplikasi dihapus atau Data Aplikasi dibersihkan di Pengaturan HP)"*.

#### D. Form Tambah / Edit Transaksi (Add/Edit)
- **Direction Selector**: Segmented control for `Dipinjamkan` and `Dipinjam`.
- **Item Name**: Required text field.
- **Person Name**: Required text field (*Nama Peminjam* or *Nama Pemilik*).
- **Category**: Dropdown selecting one of the 8 predefined categories.
- **Dates**:
  - `Tanggal Pinjam`: DatePicker (defaults to today).
  - `Batas Pengembalian`: DatePicker (defaults to today + 7 days).
  - Validates `dueDate >= lentOrBorrowedDate` on calendar day basis.
- **Item Photo**:
  - Thumbnail preview box.
  - Button to open Android Photo Picker.
  - Button to remove selected photo.
- **Optional Fields**: Contact phone number, notes.
- **Submit Button**: `"Simpan Transaksi"` with validation feedback.

#### E. Detail Transaksi (Detail Screen)
- **TopAppBar**: Back button, item title, and Delete action (with confirmation dialog).
- **Photo Display**: Header image with graceful category icon fallback if photo is missing.
- **Status Banner**: Highlighted status chip (Aktif, Terlambat, or Selesai).
- **Details**: Direction, person name, category, transaction date, due date, return date (if completed), contact info (with call/chat intent launcher), and notes.
- **Actions**:
  - For unreturned items: `"Tandai Sudah Kembali"` (opens date confirmation dialog) and `"Edit Transaksi"`.
  - For completed items: `"Edit Transaksi"`.

---

## 🔔 Local Notification Requirements

### 1. WorkManager Scheduling Strategy
- Single consistent strategy for the MVP: A periodic background worker (`OverdueCheckWorker`) scheduled via `WorkManager`:
  ```kotlin
  val workRequest = PeriodicWorkRequestBuilder<OverdueCheckWorker>(24, TimeUnit.HOURS)
      .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
      .build()
  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      "balikin_daily_check",
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
  )
  ```
- Android manages execution timing based on device battery and Doze state (not fixed to an exact minute/hour).

### 2. Notification Throttling & Stable IDs
- Use `transaction.id.toInt()` as the notification ID so subsequent alerts for the same item update the existing notification instead of creating duplicates.
- **Throttling rule**: In `OverdueCheckWorker`, check:
  - Items due in <= 24 hours: show upcoming reminder.
  - Items overdue: show overdue reminder. Store `lastNotifiedDate` (e.g. in SharedPreferences/DataStore) to notify at most once every 3 days for the same overdue transaction.
- When an item is marked as returned or deleted, dismiss any existing active notification using `notificationManager.cancel(transaction.id.toInt())`.

### 3. Notification Channel & Permissions
- Channel ID: `balikin_reminders`
- Channel Name: `Pengingat Pengembalian Balikin`
- Declare `android.permission.POST_NOTIFICATIONS` in `AndroidManifest.xml`.
- Request permission on Android 13+ with graceful fallback if denied (app remains 100% operational with in-app status chips).

---

## 📸 Local Photo Storage Requirements

### 1. Dedicated Directory
- All item photos are stored in: `File(context.filesDir, "item_photos")`.
- If the directory does not exist, create it via `dir.mkdirs()`.

### 2. Preservation of Format & Unique Naming
- Inspect the selected image URI's MIME type using `context.contentResolver.getType(uri)`:
  - `image/png` -> extension `.png`
  - `image/webp` -> extension `.webp`
  - `image/jpeg` or fallback -> extension `.jpg`
- Generate unique filename: `item_${UUID.randomUUID()}.$extension`.
- Copy binary stream directly from `contentResolver.openInputStream(uri)` to destination file.

### 3. Safe Cleanup & Database Failure Handling
- **Database Save Failure**: If saving the transaction to Room fails, catch the exception and delete the newly copied image file to avoid orphans.
- **Photo Replacement / Deletion**: Before deleting an existing photo file from disk, query `countOtherReferencesToPhoto(photoPath, transactionId)`. Only delete the file from disk if the count is 0.
- **Missing File Graceful Fallback**: If a file at `photoPath` is missing or unreadable, the UI renders the default category vector icon without throwing exceptions.

---

## 🧪 Testing Requirements

### 1. Local JVM / Robolectric Test Scenarios (`app/src/test/java/`)
- **T-1: Database Insertion & Retrieval**:
  - Insert a `TransactionEntity` with direction `LENT`. Verify `getTransactionById` returns the exact entity with generated `id > 0`.
- **T-2: Active vs. Completed Flow Segregation**:
  - Insert 2 unreturned transactions (`returnedDate = null`) and 1 completed transaction (`returnedDate = 1700000000L`).
  - Verify `observeActiveTransactions()` emits exactly the 2 unreturned items.
  - Verify `observeCompletedTransactions()` emits exactly the 1 completed item.
- **T-3: Shared Status Calculation Logic**:
  - Test `StatusHelper.calculateStatus()`:
    - `returnedDate != null` -> Assert `COMPLETED`.
    - `returnedDate == null` and `dueDate = tomorrow` -> Assert `ACTIVE`.
    - `returnedDate == null` and `dueDate = yesterday` -> Assert `OVERDUE`.
- **T-4: Calendar Day Date Validation**:
  - Verify `StatusHelper.isDueDateValid()` rejects `dueDate < lentOrBorrowedDate` on calendar day level.
  - Verify it accepts same-day lending and due dates (`dueDate == lentOrBorrowedDate`).
  - Verify it accepts future due dates (`dueDate > lentOrBorrowedDate`).
- **T-5: Mark as Returned Flow**:
  - Insert an unreturned transaction.
  - Call `dao.markAsReturned(id, returnTime)`.
  - Verify `getTransactionById(id)?.returnedDate == returnTime`.
  - Verify the item transitions from `observeActiveTransactions` to `observeCompletedTransactions`.
- **T-6: Search & Filtering Logic**:
  - Verify search filters match item names and person names case-insensitively.
  - Verify direction filter isolates `LENT` vs. `BORROWED`.
- **T-7: Statistics Aggregation**:
  - Verify statistics calculator produces matching counts for total, active, overdue, completed, and category counts using the shared status helper.
- **T-8: Photo Reference Safety**:
  - Verify DAO method `countOtherReferencesToPhoto` accurately returns 0 when no other transaction uses the path, and >0 when shared.

---

## 🛠️ Technical Tasks

### 1. Project Structure & Dependencies
- [ ] Verify `app/build.gradle.kts` dependencies:
  - Compose Material 3 & Extended Icons.
  - Room runtime, compiler (KSP), and Coroutines extension (`room-ktx`).
  - Coil Compose for image loading (`io.coil-kt:coil-compose`).
  - WorkManager runtime KTX (`androidx.work:work-runtime-ktx`).
- [ ] Establish package structure:
  - `com.example.balikin.data.local`
  - `com.example.balikin.data.repository`
  - `com.example.balikin.ui.navigation`
  - `com.example.balikin.ui.home`
  - `com.example.balikin.ui.history`
  - `com.example.balikin.ui.stats`
  - `com.example.balikin.ui.detail`
  - `com.example.balikin.ui.addedit`
  - `com.example.balikin.ui.components`
  - `com.example.balikin.worker`
  - `com.example.balikin.util`

### 2. Room Database Layer
- [ ] Implement `TransactionEntity` with indexes and nullable fields.
- [ ] Implement `TransactionDao` including synchronous methods for WorkManager and photo reference checks.
- [ ] Implement `AppDatabase` singleton.
- [ ] Implement `TransactionRepository` exposing clean reactive flows.

### 3. Local Photo Storage Service
- [ ] Implement `PhotoStorageHelper`:
  - Determine MIME type and copy stream to `context.filesDir/item_photos/item_<uuid>.<ext>`.
  - Safe deletion with reference check.
  - Cleanup routine on transaction save failure.

### 4. Business Logic & Shared Status Helper
- [ ] Implement `StatusHelper` with `calculateStatus()` and `isDueDateValid()`.
- [ ] Build `AddEditViewModel` with input validation, calendar date validation, and photo handling.
- [ ] Build `DetailViewModel` with mark-as-returned, edit navigation, and deletion logic.

### 5. UI Implementation
- [ ] Navigation scaffold with bottom navigation bar (Beranda, Riwayat, Statistik).
- [ ] **Beranda**:
  - Summary metric cards (Dipinjamkan, Dipinjam, Terlambat).
  - Search bar and filter chips (Semua, Dipinjamkan, Dipinjam).
  - Transaction cards with status badges and days remaining/overdue.
  - FAB (+) to Add Transaction.
  - Empty state with Indonesian copy.
- [ ] **Riwayat**:
  - List of completed transactions with return date chips.
  - Search bar and direction filter chips.
  - Empty state.
- [ ] **Statistik & Pengaturan**:
  - Metric counters (Total, Aktif, Selesai, Terlambat, Persentase Selesai).
  - Lent vs. Borrowed ratio progress bar.
  - Category breakdown list.
  - Notification toggle and permission button.
  - App info card with explicit local data storage explanation.
- [ ] **Add/Edit Screen**:
  - Segmented direction selector (`Dipinjamkan` / `Dipinjam`).
  - Text fields for item name and person name with validation error states.
  - Category dropdown with 8 predefined categories (using "Buku" for Books).
  - DatePickers for lending date and due date.
  - Photo picker button with preview and remove button.
  - Contact info and notes fields.
- [ ] **Detail Screen**:
  - Full item view, photo banner with fallback, status banner.
  - Action button: "Tandai Sudah Kembali" with return date dialog.
  - Action buttons: "Edit" and "Hapus" (with confirmation dialog).

### 6. Local Notifications with WorkManager
- [ ] Create notification channel `balikin_reminders`.
- [ ] Implement `OverdueCheckWorker` querying unreturned transactions.
- [ ] Enqueue unique periodic work request with battery constraints.
- [ ] Handle `POST_NOTIFICATIONS` runtime permission on Android 13+.
- [ ] Cancel notifications for completed or deleted transactions.

### 7. Verification & Build
- [ ] Run unit tests for database, status calculation, date validation, and stats.
- [ ] Compile the application with standard Gradle build tasks.
- [ ] Verify full end-to-end user journey.

---

## 🚫 Out of Scope
The following features are **explicitly excluded** from the initial MVP:
- ❌ User registration, login, and cloud authentication.
- ❌ Remote backend, REST APIs, or cloud servers.
- ❌ Cloud synchronization (Firebase, Supabase, AWS, etc.).
- ❌ Multi-user collaboration, contact synchronization, or shared accounts.
- ❌ Automated WhatsApp messaging or SMS dispatch bots.
- ❌ Return-photo condition proof.
- ❌ Custom user-created categories (stick to the 8 predefined categories).
- ❌ Financial interest, late fees, or monetary penalties.
- ❌ In-app subscriptions, ads, or payment systems.
- ❌ Cloud export or Google Drive backup.

---

## 📌 Implementation Notes for the Coding Model
- Inspect the existing repository structure and build configuration before making modifications.
- Do NOT rewrite or delete working Gradle configuration or signing keys.
- Store photos only in `context.filesDir/item_photos/`, preserving file extension.
- Always compute status dynamically using `StatusHelper.calculateStatus()`. Never store a static "OVERDUE" string in the database.
- Use `Modifier.testTag("...")` on all interactive buttons, cards, and text fields for automated testability.
- Touch targets must adhere to >= 48dp with accessible content descriptions.
- Ensure all screens derive data from the Room database (no static mock lists).
- If any build task fails or testing cannot be performed in the environment, report the exact limitation rather than claiming complete verification.

---

## 🏁 Definition of Done
The Balikin MVP is complete and ready for deployment when:
1. **Build Success**: The project compiles cleanly via Gradle without compilation errors.
2. **Navigation**: All three bottom navigation tabs (Beranda, Riwayat, Statistik) function smoothly.
3. **Lent & Borrowed**: Users can create, view, edit, and delete both *Dipinjamkan* and *Dipinjam* records.
4. **Validation**: The form prevents submission for blank names and invalid date ranges (`dueDate < lentOrBorrowedDate`).
5. **Return Flow**: Tapping "Tandai Sudah Kembali" transitions the item to Riwayat with the recorded return timestamp.
6. **Search & Filter**: Real-time searching and direction filtering work accurately on both Beranda and Riwayat.
7. **Photo Persistence**: Photos are copied to `item_photos/` with proper format extensions, persist across restarts, and have fallback icons when absent.
8. **Real Statistics**: All metrics on the Statistik screen are derived from Room database records using the shared status calculation.
9. **Offline Isolation**: The app functions 100% offline without network permissions or cloud dependencies.
10. **Reported Verification**: All test executions and build results are explicitly reported with passed/failed status.
