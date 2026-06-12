package com.example.ccscan.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ccscan.database.AppDatabase
import com.example.ccscan.util.DateUtils
import com.example.ccscan.util.ExcelUtils
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * 自动备份Worker
 */
class BackupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val TAG = "BackupWorker"

    override fun doWork(): Result {
        try {
            // 获取今天新增的记录
            val startOfToday = DateUtils.getStartOfToday()
            val endTime = System.currentTimeMillis()

            val parcels = runBlocking {
                AppDatabase.getInstance(applicationContext).parcelDao()
                    .getParcelsByTimeRange(startOfToday, endTime)
            }

            if (parcels.isEmpty()) {
                Log.d(TAG, "No new parcels to backup")
                return Result.success()
            }

            // 导出到Excel
            val outputFile = ExcelUtils.exportParcelsToExcel(parcels, applicationContext)
            if (outputFile != null) {
                // 移动到Backup文件夹
                val backupDir = File(applicationContext.getExternalFilesDir(null), "Backup")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                val backupFile = File(backupDir, "backup_${ExcelUtils.getDateString()}.xlsx")
                outputFile.copyTo(backupFile, overwrite = true)
                outputFile.delete()

                Log.d(TAG, "Backup completed successfully: ${backupFile.path}")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            return Result.failure()
        }
    }
}