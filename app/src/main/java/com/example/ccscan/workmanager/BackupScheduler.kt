package com.example.ccscan.workmanager

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 备份任务调度器
 */
object BackupScheduler {

    private const val BACKUP_WORKER_TAG = "backup_worker"

    /**
     * 启动自动备份任务（每10分钟执行一次）
     */
    fun startBackupScheduler(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(10, TimeUnit.MINUTES)
            .addTag(BACKUP_WORKER_TAG)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * 停止自动备份任务
     */
    fun stopBackupScheduler(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(BACKUP_WORKER_TAG)
    }
}