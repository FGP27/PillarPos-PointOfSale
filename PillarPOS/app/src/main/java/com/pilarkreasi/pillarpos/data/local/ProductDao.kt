package com.pilarkreasi.pillarpos.data.local

import androidx.room.*
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM product ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE idCategory = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE idProduct = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT COUNT(*) FROM product")
    suspend fun countProducts(): Int

    @Query("DELETE FROM product WHERE name = :name")
    suspend fun deleteAllWithName(name: String)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE product SET stock = stock - :quantity WHERE idProduct = :productId")
    suspend fun reduceStock(productId: Int, quantity: Int)

    @Query("UPDATE product SET stock = stock + :quantity WHERE idProduct = :productId")
    suspend fun addStock(productId: Int, quantity: Int)

    @Query("UPDATE product SET stock = :newStock WHERE idProduct = :productId")
    suspend fun updateStock(productId: Int, newStock: Int)
}