package com.example.ccscan.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.database.entity.Parcel
import com.example.ccscan.database.entity.Product
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Excel工具类
 */
object ExcelUtils {

    private const val TAG = "ExcelUtils"

    /**
     * 导出包裹数据到Excel
     */
    fun exportParcelsToExcel(parcels: List<Parcel>, context: Context): File? {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("包裹记录")

            // 创建表头样式
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
                fillPattern = FillPatternType.SOLID_FOREGROUND
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
                alignment = HorizontalAlignment.CENTER
            }
            val headerFont = workbook.createFont().apply {
                bold = true
            }
            headerStyle.setFont(headerFont)

            // 创建表头
            val headers = arrayOf("快递单号", "客户唛头", "商品名称", "数量", "重量(kg)", "登记时间")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // 创建数据样式
            val dataStyle = workbook.createCellStyle().apply {
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }

            // 填充数据
            parcels.forEachIndexed { index, parcel ->
                val row = sheet.createRow(index + 1)

                val cell0 = row.createCell(0)
                cell0.setCellValue(parcel.trackingNumber)
                cell0.cellStyle = dataStyle

                val cell1 = row.createCell(1)
                cell1.setCellValue(parcel.customerMark)
                cell1.cellStyle = dataStyle

                val cell2 = row.createCell(2)
                cell2.setCellValue(parcel.productName)
                cell2.cellStyle = dataStyle

                val cell3 = row.createCell(3)
                cell3.setCellValue(parcel.quantity.toDouble())
                cell3.cellStyle = dataStyle

                val cell4 = row.createCell(4)
                cell4.setCellValue(String.format("%.2f", parcel.weight))
                cell4.cellStyle = dataStyle

                val cell5 = row.createCell(5)
                cell5.setCellValue(formatTime(parcel.registrationTime))
                cell5.cellStyle = dataStyle
            }

            // 自动调整列宽
            headers.indices.forEach { sheet.autoSizeColumn(it) }

            // 创建输出文件
            val fileName = "parcels_${getDateString()}.xlsx"
            val outputDir = context.getExternalFilesDir("exports") ?: context.filesDir
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { fos ->
                workbook.write(fos)
            }

            workbook.close()
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Export to Excel failed", e)
            return null
        }
    }

    /**
     * 读取Excel文件获取列名列表
     */
    fun getExcelColumns(context: Context, uri: Uri): List<String>? {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(0)

            val columns = mutableListOf<String>()
            if (headerRow != null) {
                for (i in 0 until headerRow.lastCellNum) {
                    val cell = headerRow.getCell(i)
                    columns.add(getCellValueAsString(cell))
                }
            }

            workbook.close()
            inputStream.close()
            return columns
        } catch (e: Exception) {
            Log.e(TAG, "Get Excel columns failed", e)
            return null
        }
    }

    /**
     * 从Excel导入客户数据
     */
    fun importCustomersFromExcel(
        context: Context,
        uri: Uri,
        fullNameColIndex: Int,
        abbreviationColIndex: Int,
        numericCodeColIndex: Int
    ): List<Customer> {
        val customers = mutableListOf<Customer>()
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return customers
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue

                val fullName = getCellValueAsString(row.getCell(fullNameColIndex))
                val abbreviation = getCellValueAsString(row.getCell(abbreviationColIndex))
                val numericCode = getCellValueAsString(row.getCell(numericCodeColIndex))

                if (fullName.isNotEmpty()) {
                    customers.add(Customer(fullName = fullName, abbreviation = abbreviation, numericCode = numericCode))
                }
            }

            workbook.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Import customers failed", e)
        }
        return customers
    }

    /**
     * 从Excel导入商品数据
     */
    fun importProductsFromExcel(
        context: Context,
        uri: Uri,
        fullNameColIndex: Int,
        abbreviationColIndex: Int,
        numericCodeColIndex: Int
    ): List<Product> {
        val products = mutableListOf<Product>()
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return products
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue

                val fullName = getCellValueAsString(row.getCell(fullNameColIndex))
                val abbreviation = getCellValueAsString(row.getCell(abbreviationColIndex))
                val numericCode = getCellValueAsString(row.getCell(numericCodeColIndex))

                if (fullName.isNotEmpty()) {
                    products.add(Product(fullName = fullName, abbreviation = abbreviation, numericCode = numericCode))
                }
            }

            workbook.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Import products failed", e)
        }
        return products
    }

    /**
     * 获取单元格值为字符串
     */
    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val value = cell.numericCellValue
                if (value == value.toLong().toDouble()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue.trim()
                } catch (e: Exception) {
                    cell.numericCellValue.toString()
                }
            }
            else -> ""
        }
    }

    /**
     * 格式化时间戳
     */
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * 获取日期字符串
     */
    fun getDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}