package com.example.ccscan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ccscan.database.entity.Parcel

/**
 * 包裹数据访问接口
 */
@Dao
interface ParcelDao {

    @Insert
    suspend fun insert(parcel: Parcel): Long

    @Query("SELECT * FROM parcels ORDER BY registrationTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getParcels(limit: Int, offset: Int): List<Parcel>

    @Query("SELECT COUNT(*) FROM parcels")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM parcels WHERE registrationTime >= :startOfDay")
    suspend fun getTodayCount(startOfDay: Long): Int

    @Query("SELECT * FROM parcels WHERE registrationTime >= :startOfDay ORDER BY registrationTime DESC")
    suspend fun getTodayParcels(startOfDay: Long): List<Parcel>

    @Query("SELECT * FROM parcels WHERE registrationTime >= :startTime AND registrationTime < :endTime ORDER BY registrationTime DESC")
    suspend fun getParcelsByTimeRange(startTime: Long, endTime: Long): List<Parcel>

    @Query("DELETE FROM parcels WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM parcels")
    suspend fun deleteAll()
}