package com.pilarkreasi.pillarpos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pilarkreasi.pillarpos.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionDetailEntity::class,
        DiscountEntity::class,
    ],
    version = 7, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PillarPosDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun discountDao(): DiscountDao

    companion object {
        @Volatile
        private var INSTANCE: PillarPosDatabase? = null

        fun getInstance(context: Context): PillarPosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PillarPosDatabase::class.java,
                    "pillar_pos.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedCallback(context: Context) = object : Callback() {
            override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    val userDao = database.userDao()
                    if (userDao.countUsers() == 0) {
                        userDao.insertAll(
                            listOf(
                                UserEntity(
                                    idOutlet = 1,
                                    nama = "Febrian",
                                    username = "admin",
                                    password = "admin123",
                                    peran = Role.ADMIN
                                ),
                                UserEntity(
                                    idOutlet = 1,
                                    nama = "Bobby",
                                    username = "kasir",
                                    password = "kasir123",
                                    peran = Role.KASIR
                                )
                            )
                        )
                    }

                    val categoryDao = database.categoryDao()
                    val productDao = database.productDao()

                    if (categoryDao.countCategories() == 0) {
                        categoryDao.insertAll(
                            listOf(
                                CategoryEntity(name = "Makanan"),
                                CategoryEntity(name = "Minuman")
                            )
                        )
                    }

                    if (productDao.countProducts() == 0) {
                        val idMakanan = categoryDao.getCategoryIdByName("Makanan")
                        val idMinuman = categoryDao.getCategoryIdByName("Minuman")

                        productDao.insertAll(
                            listOf(
                                ProductEntity(
                                    idCategory = idMinuman,
                                    name = "Espresso",
                                    price = 20000.0,
                                    stock = 10,
                                    imageUrl = "drawable://espresso",
                                    description = "Espresso single shot"
                                ),
                                ProductEntity(
                                    idCategory = idMinuman,
                                    name = "Latte",
                                    price = 25000.0,
                                    stock = 10,
                                    imageUrl = "drawable://latte",
                                    description = "Kopi susu latte dengan latte art"
                                ),
                                ProductEntity(
                                    idCategory = idMakanan,
                                    name = "Gedang Goreng",
                                    price = 15000.0,
                                    stock = 15,
                                    imageUrl = "drawable://gedang_goreng",
                                    description = "Pisang goreng khas Kedai Kopi Nusantara"
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
