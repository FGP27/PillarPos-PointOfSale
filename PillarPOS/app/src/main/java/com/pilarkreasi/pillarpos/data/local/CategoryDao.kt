package com.pilarkreasi.pillarpos.data.local

import androidx.room.*
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM category")
    suspend fun countCategories(): Int

    @Query("SELECT idCategory FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryIdByName(name: String): Int?

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
