package com.example.ccscan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 包裹实体类
 */
@Entity(tableName = "parcels")
data class Parcel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackingNumber: String,      // 快递单号
    val customerMark: String,        // 客户唛头
    val productName: String,         // 商品名称
    val quantity: Int,               // 数量
    val weight: Double,              // 重量(kg)
    val registrationTime: Long       // 登记时间(时间戳)
)