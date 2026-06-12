package com.example.ccscan.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.ccscan.database.dao.CustomerDao
import com.example.ccscan.database.dao.ParcelDao
import com.example.ccscan.database.dao.ProductDao
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.database.entity.Parcel
import com.example.ccscan.database.entity.Product

/**
 * 应用数据库
 */
@Database(
    entities = [Parcel::class, Customer::class, Product::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parcelDao(): ParcelDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "ccscan.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}