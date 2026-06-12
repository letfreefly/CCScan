package com.example.ccscan

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 自定义Application类
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupCrashHandler()
    }

    /**
     * 设置全局异常捕获
     */
    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // 记录崩溃日志
                val logFile = getCrashLogFile()
                val writer = FileWriter(logFile, true)
                writer.write("========== ${getCurrentTime()} ==========\n")
                writer.write("Thread: ${thread.name}\n")
                writer.write("Exception: ${throwable.javaClass.name}\n")
                writer.write("Message: ${throwable.message}\n")
                
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                writer.write("Stack Trace:\n${sw.toString()}\n\n")
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                Log.e("CrashHandler", "Failed to write crash log", e)
            } finally {
                // 调用默认处理器
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    /**
     * 获取崩溃日志文件
     */
    private fun getCrashLogFile(): File {
        val logsDir = File(getExternalFilesDir(null), "crash_logs")
        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }
        return File(logsDir, "crash_${getDateString()}.txt")
    }

    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * 获取日期字符串（用于日志文件名）
     */
    private fun getDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        lateinit var instance: MyApplication
            private set

        fun getContext(): Context = instance.applicationContext
    }
}