package com.example.ccscan

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ccscan.database.AppDatabase
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.database.entity.Product
import com.example.ccscan.databinding.ActivityImportMappingBinding
import com.example.ccscan.util.ExcelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Excel导入列映射Activity
 */
class ImportMappingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportMappingBinding
    private var columns: List<String> = emptyList()
    private var fullNameColIndex = -1
    private var abbreviationColIndex = -1
    private var numericCodeColIndex = -1
    private lateinit var importType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportMappingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.column_mapping)

        // 获取传入参数
        val uriString = intent.getStringExtra(EXTRA_URI)
        importType = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_CUSTOMER

        if (uriString == null) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 加载Excel列名
        lifecycleScope.launch(Dispatchers.IO) {
            columns = ExcelUtils.getExcelColumns(this@ImportMappingActivity, Uri.parse(uriString)) ?: emptyList()

            withContext(Dispatchers.Main) {
                if (columns.isEmpty()) {
                    Toast.makeText(this@ImportMappingActivity, "无法读取文件内容", Toast.LENGTH_SHORT).show()
                    finish()
                    return@withContext
                }

                // 设置下拉列表适配器
                val adapter = ArrayAdapter(this@ImportMappingActivity, android.R.layout.simple_spinner_item, columns)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spFullName.adapter = adapter
                binding.spAbbreviation.adapter = adapter
                binding.spNumericCode.adapter = adapter
            }
        }

        // 设置下拉选择监听
        binding.spFullName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fullNameColIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                fullNameColIndex = -1
            }
        }

        binding.spAbbreviation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                abbreviationColIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                abbreviationColIndex = -1
            }
        }

        binding.spNumericCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                numericCodeColIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                numericCodeColIndex = -1
            }
        }

        // 开始导入按钮
        binding.btnStartImport.setOnClickListener {
            if (fullNameColIndex < 0) {
                Toast.makeText(this, "请选择全称列", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startImport(Uri.parse(uriString))
        }
    }

    /**
     * 开始导入
     */
    private fun startImport(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnStartImport.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val importedCount = if (importType == TYPE_CUSTOMER) {
                // 导入客户数据
                val customers = ExcelUtils.importCustomersFromExcel(
                    this@ImportMappingActivity,
                    uri,
                    fullNameColIndex,
                    abbreviationColIndex,
                    numericCodeColIndex
                )
                if (customers.isNotEmpty()) {
                    AppDatabase.getInstance(this@ImportMappingActivity).customerDao().insertAll(customers)
                }
                customers.size
            } else {
                // 导入商品数据
                val products = ExcelUtils.importProductsFromExcel(
                    this@ImportMappingActivity,
                    uri,
                    fullNameColIndex,
                    abbreviationColIndex,
                    numericCodeColIndex
                )
                if (products.isNotEmpty()) {
                    AppDatabase.getInstance(this@ImportMappingActivity).productDao().insertAll(products)
                }
                products.size
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.btnStartImport.isEnabled = true

                Toast.makeText(this@ImportMappingActivity, getString(R.string.import_success, importedCount), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_URI = "uri"
        const val EXTRA_TYPE = "type"
        const val TYPE_CUSTOMER = "customer"
        const val TYPE_PRODUCT = "product"
    }
}