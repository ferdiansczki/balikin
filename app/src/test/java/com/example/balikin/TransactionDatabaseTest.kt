package com.example.balikin

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.balikin.data.local.AppDatabase
import com.example.balikin.data.local.ItemCategory
import com.example.balikin.data.local.TransactionDao
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.data.repository.TransactionRepository
import com.example.balikin.util.DateFormatter
import com.example.balikin.util.StatusHelper
import com.example.balikin.util.TransactionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
        repository = TransactionRepository(dao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testT1_insertAndRetrieveTransaction() = runTest {
        val now = System.currentTimeMillis()
        val transaction = TransactionEntity(
            itemName = "Kamera Mirrorless Sony A7",
            direction = TransactionDirection.LENT.name,
            category = ItemCategory.ELECTRONICS.name,
            personName = "Andi Wijaya",
            contactInfo = "08123456789",
            lentOrBorrowedDate = now,
            dueDate = now + (7 * 24 * 60 * 60 * 1000L),
            photoPath = "item_sample.jpg",
            notes = "Lensa 28-70mm kit"
        )

        val id = repository.insert(transaction)
        assertTrue(id > 0)

        val retrieved = repository.getTransactionById(id)
        assertNotNull(retrieved)
        assertEquals("Kamera Mirrorless Sony A7", retrieved?.itemName)
        assertEquals(TransactionDirection.LENT.name, retrieved?.direction)
        assertEquals("Andi Wijaya", retrieved?.personName)
        assertEquals(ItemCategory.ELECTRONICS.name, retrieved?.category)
        assertNull(retrieved?.returnedDate)
    }

    @Test
    fun testT2_activeVsCompletedFlowSegregation() = runTest {
        val now = System.currentTimeMillis()

        // 1. Insert active lent item
        repository.insert(
            TransactionEntity(
                itemName = "Buku Pemrograman Kotlin",
                direction = TransactionDirection.LENT.name,
                category = ItemCategory.BOOKS.name,
                personName = "Budi Pratama",
                lentOrBorrowedDate = now,
                dueDate = now + (3 * 24 * 60 * 60 * 1000L)
            )
        )

        // 2. Insert active borrowed item
        repository.insert(
            TransactionEntity(
                itemName = "Bor Listrik Bosch",
                direction = TransactionDirection.BORROWED.name,
                category = ItemCategory.TOOLS.name,
                personName = "Pak RT",
                lentOrBorrowedDate = now,
                dueDate = now + (2 * 24 * 60 * 60 * 1000L)
            )
        )

        // 3. Insert completed item
        repository.insert(
            TransactionEntity(
                itemName = "Jaket Gunung Eiger",
                direction = TransactionDirection.BORROWED.name,
                category = ItemCategory.CLOTHING.name,
                personName = "Rian",
                lentOrBorrowedDate = now - (10 * 24 * 60 * 60 * 1000L),
                dueDate = now - (3 * 24 * 60 * 60 * 1000L),
                returnedDate = now - (2 * 24 * 60 * 60 * 1000L)
            )
        )

        val activeList = repository.activeTransactions.first()
        val completedList = repository.completedTransactions.first()

        assertEquals(2, activeList.size)
        assertEquals(1, completedList.size)
        assertEquals("Jaket Gunung Eiger", completedList[0].itemName)
    }

    @Test
    fun testT3_statusCalculationLogic() {
        val now = System.currentTimeMillis()
        val tomorrow = now + (24 * 60 * 60 * 1000L)
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)

        // 1. Returned -> COMPLETED
        val statusCompleted = StatusHelper.calculateStatus(
            returnedDate = now,
            dueDate = tomorrow,
            currentTime = now
        )
        assertEquals(TransactionStatus.COMPLETED, statusCompleted)

        // 2. Unreturned and future due date -> ACTIVE
        val statusActive = StatusHelper.calculateStatus(
            returnedDate = null,
            dueDate = tomorrow,
            currentTime = now
        )
        assertEquals(TransactionStatus.ACTIVE, statusActive)

        // 3. Unreturned and past due date -> OVERDUE
        val statusOverdue = StatusHelper.calculateStatus(
            returnedDate = null,
            dueDate = twoDaysAgo,
            currentTime = now
        )
        assertEquals(TransactionStatus.OVERDUE, statusOverdue)
    }

    @Test
    fun testT4_calendarDayDateValidation() {
        val now = System.currentTimeMillis()
        val tomorrow = now + (24 * 60 * 60 * 1000L)
        val yesterday = now - (24 * 60 * 60 * 1000L)

        // Same day -> valid
        assertTrue(StatusHelper.isDueDateValid(now, now))

        // Due date after lending date -> valid
        assertTrue(StatusHelper.isDueDateValid(now, tomorrow))

        // Due date before lending date -> invalid
        assertFalse(StatusHelper.isDueDateValid(now, yesterday))
    }

    @Test
    fun testT5_markAsReturnedFlow() = runTest {
        val now = System.currentTimeMillis()
        val id = repository.insert(
            TransactionEntity(
                itemName = "Helm KYT",
                direction = TransactionDirection.LENT.name,
                category = ItemCategory.VEHICLES.name,
                personName = "Dedi",
                lentOrBorrowedDate = now,
                dueDate = now + (5 * 24 * 60 * 60 * 1000L)
            )
        )

        // Verify active first
        val activeBefore = repository.activeTransactions.first()
        assertTrue(activeBefore.any { it.id == id })

        // Mark as returned
        val returnTime = now + (1 * 24 * 60 * 60 * 1000L)
        repository.markAsReturned(id, returnTime)

        // Verify moved from active to completed
        val activeAfter = repository.activeTransactions.first()
        val completedAfter = repository.completedTransactions.first()

        assertFalse(activeAfter.any { it.id == id })
        assertTrue(completedAfter.any { it.id == id })
        assertEquals(returnTime, completedAfter.first { it.id == id }.returnedDate)

        // Unmark returned (revert back to active)
        repository.unmarkReturned(id)
        val activeReverted = repository.activeTransactions.first()
        val completedReverted = repository.completedTransactions.first()

        assertTrue(activeReverted.any { it.id == id })
        assertFalse(completedReverted.any { it.id == id })
        assertNull(repository.getTransactionById(id)?.returnedDate)
    }

    @Test
    fun testT9_timezoneDateConversionSafety() {
        val testUtcMillis = 1792108800000L // 2026-10-16 00:00:00 UTC
        val localMiddayMillis = DateFormatter.utcMillisToLocalDateMillis(testUtcMillis)

        // Formatting local timestamp must always yield "16 Okt 2026" regardless of system timezone
        val formatted = DateFormatter.formatStandard(localMiddayMillis)
        assertTrue("Formatted date should start with '16'", formatted.startsWith("16"))

        // Roundtrip back to UTC
        val convertedUtc = DateFormatter.localDateMillisToUtcMillis(localMiddayMillis)
        assertEquals(testUtcMillis, convertedUtc)
    }

    @Test
    fun testT8_photoReferenceSafety() = runTest {
        val now = System.currentTimeMillis()
        val photoPath = "item_unique_123.jpg"

        val id1 = repository.insert(
            TransactionEntity(
                itemName = "Tenda Camping",
                direction = TransactionDirection.LENT.name,
                category = ItemCategory.OTHER.name,
                personName = "Fajar",
                lentOrBorrowedDate = now,
                dueDate = now + (7 * 24 * 60 * 60 * 1000L),
                photoPath = photoPath
            )
        )

        // When only id1 uses this photo, checking excludeId = id1 should return 0 other references
        val otherReferencesCount1 = repository.countOtherReferencesToPhoto(photoPath, excludeId = id1)
        assertEquals(0, otherReferencesCount1)

        // If another transaction shares the same path
        val id2 = repository.insert(
            TransactionEntity(
                itemName = "Sleeping Bag",
                direction = TransactionDirection.LENT.name,
                category = ItemCategory.OTHER.name,
                personName = "Fajar",
                lentOrBorrowedDate = now,
                dueDate = now + (7 * 24 * 60 * 60 * 1000L),
                photoPath = photoPath
            )
        )

        // Now checking excludeId = id1 should find 1 other reference (id2)
        val otherReferencesCount2 = repository.countOtherReferencesToPhoto(photoPath, excludeId = id1)
        assertEquals(1, otherReferencesCount2)
    }
}
