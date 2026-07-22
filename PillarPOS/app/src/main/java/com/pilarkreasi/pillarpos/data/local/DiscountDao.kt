package com.pilarkreasi.pillarpos.data.local

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import kotlinx.coroutines.flow.Flow

@androidx.room.Dao
interface DiscountDao {

    @Query("SELECT * FROM discount ORDER BY idDiscount DESC")
    fun getAllDiscounts(): Flow<List<DiscountEntity>>

    @Query("SELECT * FROM discount WHERE isActive = 1")
    fun getActiveDiscounts(): Flow<List<DiscountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountEntity)

    @Update
    suspend fun updateDiscount(discount: DiscountEntity)

    @Delete
    suspend fun deleteDiscount(discount: DiscountEntity)
}