package com.example.ccscan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ccscan.database.entity.Product

/**
 * 商品名称数据访问接口
 */
@Dao
interface ProductDao {

    @Insert
    suspend fun insert(product: Product): Long

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM products ORDER BY fullName ASC")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE fullName LIKE :query OR abbreviation LIKE :query OR numericCode LIKE :query")
    suspend fun search(query: String): List<Product>

    /**
     * 模糊搜索：匹配全称、缩写、数字代码（包含匹配）
     */
    @Query("SELECT * FROM products WHERE fullName LIKE '%' || :keyword || '%' OR abbreviation LIKE '%' || :keyword || '%' OR numericCode LIKE '%' || :keyword || '%' ORDER BY fullName ASC")
    suspend fun fuzzySearch(keyword: String): List<Product>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}