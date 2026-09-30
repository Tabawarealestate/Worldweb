package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.InstrumentDao
import com.example.data.local.dao.LedgerDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.QuoteDao
import com.example.data.local.dao.SurgeDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.InstrumentEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QuoteEntity
import com.example.data.local.entity.SurgeEntryEntity
import com.example.data.local.entity.SurgeRoundEntity
import com.example.data.local.entity.WalletEntity

@Database(
    entities = [
        InstrumentEntity::class,
        QuoteEntity::class,
        OrderEntity::class,
        SurgeRoundEntity::class,
        SurgeEntryEntity::class,
        WalletEntity::class,
        LedgerEntryEntity::class,
        ProductEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instrumentDao(): InstrumentDao
    abstract fun quoteDao(): QuoteDao
    abstract fun orderDao(): OrderDao
    abstract fun surgeDao(): SurgeDao
    abstract fun walletDao(): WalletDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun productDao(): ProductDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexis_markets.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
