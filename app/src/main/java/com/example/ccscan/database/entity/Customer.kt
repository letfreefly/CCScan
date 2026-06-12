package com.example.ccscan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 客户唛头实体类
 */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,       // 全称
    val abbreviation: String,   // 字母缩写
    val numericCode: String     // 数字代称
)