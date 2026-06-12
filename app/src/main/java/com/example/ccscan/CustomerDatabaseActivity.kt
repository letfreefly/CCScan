package com.example.ccscan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ccscan.adapter.DatabaseListAdapter
import com.example.ccscan.database.AppDatabase
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.databinding.ActivityDatabaseBinding
import com.guolindev.permissionx.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 客户唛头资料库Activity
 */
class CustomerDatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseBinding
    private val customers = mutableListOf<Customer>()
    private lateinit var adapter: DatabaseListAdapter<Customer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.customer_database)

        // 初始化RecyclerView
        adapter = DatabaseListAdapter(customers,
            onEditClick = { editCustomer(it) },
            onDeleteClick = { deleteCustomer(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 添加按钮
        binding.fabAdd.setOnClickListener {
            showEditDialog(null)
        }

        // 导入按钮
        binding.btnImport.setOnClickListener {
            requestStoragePermissionAndImport()
        }

        // 加载数据
        loadCustomers()
    }

    /**
     * 加载客户列表
     */
    private fun loadCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers.clear()
            customers.addAll(AppDatabase.getInstance(this@CustomerDatabaseActivity).customerDao().getAll())

            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * 显示编辑对话框
     */
    private fun showEditDialog(customer: Customer?) {
        val fullName = customer?.fullName ?: ""
        val abbreviation = customer?.abbreviation ?: ""
        val numericCode = customer?.numericCode ?: ""

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (customer == null) getString(R.string.add) else getString(R.string.edit))
            .setView(R.layout.dialog_edit_database)
            .setPositiveButton(R.string.save) { _, _ ->
                val etFullName = binding.root.findViewById<android.widget.EditText>(R.id.etFullName)
                val etAbbreviation = binding.root.findViewById<android.widget.EditText>(R.id.etAbbreviation)
                val etNumericCode = binding.root.findViewById<android.widget.EditText>(R.id.etNumericCode)

                val newFullName = etFullName.text.toString().trim()
                val newAbbreviation = etAbbreviation.text.toString().trim()
                val newNumericCode = etNumericCode.text.toString().trim()

                if (newFullName.isEmpty()) {
                    Toast.makeText(this, "请输入全称", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    if (customer == null) {
                        // 添加
                        AppDatabase.getInstance(this@CustomerDatabaseActivity).customerDao().insert(
                            Customer(fullName = newFullName, abbreviation = newAbbreviation, numericCode = newNumericCode)
                        )
                    } else {
                        // 更新
                        AppDatabase.getInstance(this@CustomerDatabaseActivity).customerDao().update(
                            Customer(id = customer.id, fullName = newFullName, abbreviation = newAbbreviation, numericCode = newNumericCode)
                        )
                    }

                    withContext(Dispatchers.Main) {
                        loadCustomers()
                        Toast.makeText(this@CustomerDatabaseActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()

        // 设置初始值
        dialog.findViewById<android.widget.EditText>(R.id.etFullName)?.setText(fullName)
        dialog.findViewById<android.widget.EditText>(R.id.etAbbreviation)?.setText(abbreviation)
        dialog.findViewById<android.widget.EditText>(R.id.etNumericCode)?.setText(numericCode)
    }

    /**
     * 编辑客户
     */
    private fun editCustomer(customer: Customer) {
        showEditDialog(customer)
    }

    /**
     * 删除客户
     */
    private fun deleteCustomer(customer: Customer) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance(this@CustomerDatabaseActivity).customerDao().deleteById(customer.id)

                    withContext(Dispatchers.Main) {
                        loadCustomers()
                        Toast.makeText(this@CustomerDatabaseActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 请求存储权限并导入
     */
    private fun requestStoragePermissionAndImport() {
        PermissionX.init(this)
            .permissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    selectExcelFile()
                } else {
                    Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * 选择Excel文件
     */
    private fun selectExcelFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.ms-excel"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_IMPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val intent = Intent(this, ImportMappingActivity::class.java).apply {
                    putExtra(ImportMappingActivity.EXTRA_URI, uri.toString())
                    putExtra(ImportMappingActivity.EXTRA_TYPE, ImportMappingActivity.TYPE_CUSTOMER)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomers()
    }

    companion object {
        private const val REQUEST_IMPORT = 2001
    }
}