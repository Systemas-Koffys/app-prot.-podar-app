package com.example.data.repository

import com.example.data.dao.RequestDao
import com.example.data.model.RequestEntity
import kotlinx.coroutines.flow.Flow

class RequestRepository(private val requestDao: RequestDao) {
    val allRequests: Flow<List<RequestEntity>> = requestDao.getAllRequests()

    fun getRequestById(id: Int): Flow<RequestEntity?> {
        return requestDao.getRequestById(id)
    }

    suspend fun insert(request: RequestEntity): Long {
        return requestDao.insertRequest(request)
    }

    suspend fun update(request: RequestEntity) {
        requestDao.updateRequest(request)
    }

    suspend fun delete(request: RequestEntity) {
        requestDao.deleteRequest(request)
    }

    suspend fun deleteById(id: Int) {
        requestDao.deleteRequestById(id)
    }
}
