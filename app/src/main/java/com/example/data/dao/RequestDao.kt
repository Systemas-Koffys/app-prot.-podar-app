package com.example.data.dao

import androidx.room.*
import com.example.data.model.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM pruning_requests ORDER BY fechaRegistro DESC")
    fun getAllRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM pruning_requests WHERE id = :id")
    fun getRequestById(id: Int): Flow<RequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity): Long

    @Update
    suspend fun updateRequest(request: RequestEntity)

    @Delete
    suspend fun deleteRequest(request: RequestEntity)

    @Query("DELETE FROM pruning_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Int)
}
