package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.InstrumentEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QuoteEntity
import com.example.data.local.entity.SurgeEntryEntity
import com.example.data.local.entity.SurgeRoundEntity
import com.example.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {
    @Query("SELECT * FROM instruments")
    fun getAllInstruments(): Flow<List<InstrumentEntity>>

    @Query("SELECT * FROM instruments WHERE category = :category")
    fun getInstrumentsByCategory(category: String): Flow<List<InstrumentEntity>>

    @Query("SELECT * FROM instruments WHERE symbol = :symbol LIMIT 1")
    suspend fun getInstrumentBySymbol(symbol: String): InstrumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruments(instruments: List<InstrumentEntity>)
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE symbol = :symbol LIMIT 1")
    fun getQuoteFlow(symbol: String): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes WHERE symbol = :symbol LIMIT 1")
    suspend fun getQuote(symbol: String): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status IN ('OPEN', 'PENDING') ORDER BY createdAt DESC")
    fun getOpenOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'SETTLED' ORDER BY settledAt DESC")
    fun getSettledOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getOrderByFieldIdempotencyKey(key: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface SurgeDao {
    @Query("SELECT * FROM surge_rounds ORDER BY startTime DESC LIMIT 1")
    fun getLatestRound(): Flow<SurgeRoundEntity?>

    @Query("SELECT * FROM surge_rounds WHERE roundId = :roundId LIMIT 1")
    suspend fun getRoundById(roundId: String): SurgeRoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRound(round: SurgeRoundEntity)

    @Query("SELECT * FROM surge_entries WHERE roundId = :roundId")
    fun getEntriesForRound(roundId: String): Flow<List<SurgeEntryEntity>>

    @Query("SELECT * FROM surge_entries WHERE roundId = :roundId AND userId = :userId LIMIT 1")
    suspend fun getUserEntry(roundId: String, userId: String): SurgeEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurgeEntry(entry: SurgeEntryEntity)

    @Update
    suspend fun updateSurgeEntry(entry: SurgeEntryEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE currency = :currency LIMIT 1")
    fun getWallet(currency: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE currency = :currency LIMIT 1")
    suspend fun getWalletDirect(currency: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: WalletEntity)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE referenceId = :referenceId")
    suspend fun getEntriesForReference(referenceId: String): List<LedgerEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntries(entries: List<LedgerEntryEntity>)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM financial_products WHERE active = 1")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM financial_products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLog(log: AuditLogEntity)
}
