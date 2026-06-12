package com.example.ccscan.fragment

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ccscan.R
import com.example.ccscan.ScanActivity
import com.example.ccscan.adapter.CustomerAutoCompleteAdapter
import com.example.ccscan.adapter.ProductAutoCompleteAdapter
import com.example.ccscan.database.AppDatabase
import com.example.ccscan.database.entity.Customer
import com.example.ccscan.database.entity.Parcel
import com.example.ccscan.database.entity.Product
import com.example.ccscan.databinding.FragmentRegisterBinding
import com.example.ccscan.util.DateUtils
import com.example.ccscan.util.TTSManager
import com.guolindev.permissionx.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 登记页Fragment
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private var customers = listOf<Customer>()
    private var products = listOf<Product>()
    private var customerAdapter: CustomerAutoCompleteAdapter? = null
    private var productAdapter: ProductAutoCompleteAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化数量默认值为1
        binding.etQuantity.setText("1")

        // 设置数量输入框为数字键盘
        binding.etQuantity.inputType = InputType.TYPE_CLASS_NUMBER

        // 扫码按钮点击事件
        binding.ivScan.setOnClickListener {
            requestCameraPermissionAndScan()
        }

        // 单号输入框点击也触发扫码
        binding.etTrackingNumber.setOnClickListener {
            requestCameraPermissionAndScan()
        }

        // 客户唛头自动完成
        binding.etCustomerMark.setOnItemClickListener { _, _, position, _ ->
            val customer = customerAdapter?.getItem(position)
            customer?.let { binding.etCustomerMark.setText(it.fullName) }
        }

        // 商品名称自动完成
        binding.etProductName.setOnItemClickListener { _, _, position, _ ->
            val product = productAdapter?.getItem(position)
            product?.let { binding.etProductName.setText(it.fullName) }
        }

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener {
            saveParcel()
        }

        // 加载资料库数据
        loadDatabases()

        // 更新今日计数
        updateTodayCount()
    }

    override fun onResume() {
        super.onResume()
        // 刷新资料库数据
        loadDatabases()
        updateTodayCount()
    }

    /**
     * 请求相机权限并启动扫码
     */
    private fun requestCameraPermissionAndScan() {
        PermissionX.init(this)
            .permissions(android.Manifest.permission.CAMERA)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    val intent = Intent(requireContext(), ScanActivity::class.java)
                    startActivityForResult(intent, REQUEST_SCAN)
                } else {
                    Toast.makeText(requireContext(), "需要相机权限", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * 处理扫码结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCAN && resultCode == ScanActivity.RESULT_OK) {
            val barcode = data?.getStringExtra(ScanActivity.EXTRA_BARCODE)
            barcode?.let {
                // 播放语音提示
                TTSManager.speak(getString(R.string.scan_success))
                // 填入单号
                binding.etTrackingNumber.setText(it)
                // 光标跳转到客户唛头
                binding.etCustomerMark.requestFocus()
            }
        }
    }

    /**
     * 加载客户和商品资料库
     */
    private fun loadDatabases() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = AppDatabase.getInstance(requireContext()).customerDao().getAll()
            products = AppDatabase.getInstance(requireContext()).productDao().getAll()

            withContext(Dispatchers.Main) {
                customerAdapter = CustomerAutoCompleteAdapter(requireContext(), customers)
                binding.etCustomerMark.setAdapter(customerAdapter)

                productAdapter = ProductAutoCompleteAdapter(requireContext(), products)
                binding.etProductName.setAdapter(productAdapter)
            }
        }
    }

    /**
     * 更新今日登记数量
     */
    private fun updateTodayCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val count = AppDatabase.getInstance(requireContext()).parcelDao()
                .getTodayCount(DateUtils.getStartOfToday())

            withContext(Dispatchers.Main) {
                binding.tvTodayCount.text = getString(R.string.today_registered, count)
            }
        }
    }

    /**
     * 保存包裹信息
     */
    private fun saveParcel() {
        val trackingNumber = binding.etTrackingNumber.text.toString().trim()
        val customerMark = binding.etCustomerMark.text.toString().trim()
        val productName = binding.etProductName.text.toString().trim()
        val quantityStr = binding.etQuantity.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()

        // 验证输入
        if (trackingNumber.isEmpty()) {
            Toast.makeText(requireContext(), "请输入快递单号", Toast.LENGTH_SHORT).show()
            return
        }
        if (customerMark.isEmpty()) {
            Toast.makeText(requireContext(), "请输入客户唛头", Toast.LENGTH_SHORT).show()
            return
        }
        if (productName.isEmpty()) {
            Toast.makeText(requireContext(), "请输入商品名称", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantityStr.isEmpty()) {
            Toast.makeText(requireContext(), "请输入数量", Toast.LENGTH_SHORT).show()
            return
        }
        if (weightStr.isEmpty()) {
            Toast.makeText(requireContext(), "请输入重量", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = try {
            quantityStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "数量格式错误", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = try {
            weightStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "重量格式错误", Toast.LENGTH_SHORT).show()
            return
        }

        // 保存到数据库
        lifecycleScope.launch(Dispatchers.IO) {
            val parcel = Parcel(
                trackingNumber = trackingNumber,
                customerMark = customerMark,
                productName = productName,
                quantity = quantity,
                weight = weight,
                registrationTime = System.currentTimeMillis()
            )
            AppDatabase.getInstance(requireContext()).parcelDao().insert(parcel)

            withContext(Dispatchers.Main) {
                // 清空输入框
                binding.etTrackingNumber.text.clear()
                binding.etCustomerMark.text.clear()
                binding.etProductName.text.clear()
                binding.etQuantity.setText("1")
                binding.etWeight.text.clear()

                // 光标回到单号输入框
                binding.etTrackingNumber.requestFocus()

                // 更新今日计数
                updateTodayCount()

                Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_SCAN = 1001
    }
}