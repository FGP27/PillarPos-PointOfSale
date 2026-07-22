package com.pilarkreasi.pillarpos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(users: List<UserEntity>)

    

    @Query(
        "SELECT * FROM user WHERE username = :username AND password = :password " +
                "AND peran = :peran AND status = 1 LIMIT 1"
    )
    suspend fun login(username: String, password: String, peran: Role): UserEntity?

    @Query("SELECT COUNT(*) FROM user")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM user WHERE status = 1")
    fun getAllUsers(): kotlinx.coroutines.flow.Flow<List<UserEntity>>

    @Query("DELETE FROM user WHERE idUser = :userId")
    suspend fun deleteUser(userId: Int)
}