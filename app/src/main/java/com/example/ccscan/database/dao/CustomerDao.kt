package com.example.ccscan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ccscan.database.entity.Customer

/**
 * 客户唛头数据访问接口
 */
@Dao
interface CustomerDao {

    @Insert
    suspend fun insert(customer: Customer): Long

    @Insert
    suspend fun insertAll(customers: List<Customer>)

    @Update
    suspend fun update(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM customers ORDER BY fullName ASC")
    suspend fun getAll(): List<Customer>

    @Query("SELECT * FROM customers WHERE fullName LIKE :query OR abbreviation LIKE :query OR numericCode LIKE :query")
    suspend fun search(query: String): List<Customer>

    /**
     * 模糊搜索：匹配全称、缩写、数字代码（包含匹配）
     */
    @Query("SELECT * FROM customers WHERE fullName LIKE '%' || :keyword || '%' OR abbreviation LIKE '%' || :keyword || '%' OR numericCode LIKE '%' || :keyword || '%' ORDER BY fullName ASC")
    suspend fun fuzzySearch(keyword: String): List<Customer>

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCount(): Int

    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}