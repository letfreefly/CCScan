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
import com.example.ccscan.database.entity.Product
import com.example.ccscan.databinding.ActivityDatabaseBinding
import com.guolindev.permissionx.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 商品名称资料库Activity
 */
class ProductDatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseBinding
    private val products = mutableListOf<Product>()
    private lateinit var adapter: DatabaseListAdapter<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.product_database)

        // 初始化RecyclerView
        adapter = DatabaseListAdapter(products,
            onEditClick = { editProduct(it) },
            onDeleteClick = { deleteProduct(it) }
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
        loadProducts()
    }

    /**
     * 加载商品列表
     */
    private fun loadProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            products.clear()
            products.addAll(AppDatabase.getInstance(this@ProductDatabaseActivity).productDao().getAll())

            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * 显示编辑对话框
     */
    private fun showEditDialog(product: Product?) {
        val fullName = product?.fullName ?: ""
        val abbreviation = product?.abbreviation ?: ""
        val numericCode = product?.numericCode ?: ""

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (product == null) getString(R.string.add) else getString(R.string.edit))
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
                    if (product == null) {
                        // 添加
                        AppDatabase.getInstance(this@ProductDatabaseActivity).productDao().insert(
                            Product(fullName = newFullName, abbreviation = newAbbreviation, numericCode = newNumericCode)
                        )
                    } else {
                        // 更新
                        AppDatabase.getInstance(this@ProductDatabaseActivity).productDao().update(
                            Product(id = product.id, fullName = newFullName, abbreviation = newAbbreviation, numericCode = newNumericCode)
                        )
                    }

                    withContext(Dispatchers.Main) {
                        loadProducts()
                        Toast.makeText(this@ProductDatabaseActivity, "保存成功", Toast.LENGTH_SHORT).show()
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
     * 编辑商品
     */
    private fun editProduct(product: Product) {
        showEditDialog(product)
    }

    /**
     * 删除商品
     */
    private fun deleteProduct(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance(this@ProductDatabaseActivity).productDao().deleteById(product.id)

                    withContext(Dispatchers.Main) {
                        loadProducts()
                        Toast.makeText(this@ProductDatabaseActivity, "删除成功", Toast.LENGTH_SHORT).show()
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
                    putExtra(ImportMappingActivity.EXTRA_TYPE, ImportMappingActivity.TYPE_PRODUCT)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    companion object {
        private const val REQUEST_IMPORT = 2002
    }
}