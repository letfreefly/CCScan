package com.example.ccscan.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ccscan.R
import com.example.ccscan.adapter.ParcelListAdapter
import com.example.ccscan.database.AppDatabase
import com.example.ccscan.database.entity.Parcel
import com.example.ccscan.databinding.FragmentRecordsBinding
import com.example.ccscan.util.DateUtils
import com.example.ccscan.util.ExcelUtils
import com.guolindev.permissionx.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 记录页Fragment
 */
class RecordsFragment : Fragment() {

    private lateinit var binding: FragmentRecordsBinding
    private val parcels = mutableListOf<Parcel>()
    private lateinit var adapter: ParcelListAdapter
    private var offset = 0
    private val limit = 100
    private var isLoading = false
    private var hasMore = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化RecyclerView
        adapter = ParcelListAdapter(parcels)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 滚动监听 - 分页加载
        binding.recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && hasMore) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastVisibleItemPosition == parcels.size - 1) {
                        loadMoreParcels()
                    }
                }
            }
        })

        // 导出Excel按钮
        binding.btnExportExcel.setOnClickListener {
            requestStoragePermissionAndExport()
        }

        // 首次加载数据
        loadParcels()

        // 更新今日计数
        updateTodayCount()
    }

    override fun onResume() {
        super.onResume()
        // 刷新数据
        refreshData()
    }

    /**
     * 刷新数据
     */
    private fun refreshData() {
        offset = 0
        hasMore = true
        parcels.clear()
        adapter.notifyDataSetChanged()
        loadParcels()
        updateTodayCount()
    }

    /**
     * 加载包裹列表
     */
    private fun loadParcels() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val newParcels = AppDatabase.getInstance(requireContext()).parcelDao()
                .getParcels(limit, offset)

            withContext(Dispatchers.Main) {
                if (newParcels.isEmpty()) {
                    hasMore = false
                    if (parcels.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    parcels.addAll(newParcels)
                    adapter.notifyDataSetChanged()
                    offset += limit
                    binding.tvEmpty.visibility = View.GONE
                }

                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * 加载更多
     */
    private fun loadMoreParcels() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val newParcels = AppDatabase.getInstance(requireContext()).parcelDao()
                .getParcels(limit, offset)

            withContext(Dispatchers.Main) {
                if (newParcels.isEmpty()) {
                    hasMore = false
                } else {
                    parcels.addAll(newParcels)
                    adapter.notifyDataSetChanged()
                    offset += limit
                }

                isLoading = false
                binding.progressBar.visibility = View.GONE
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
     * 请求存储权限并导出Excel
     */
    private fun requestStoragePermissionAndExport() {
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    exportTodayParcelsToExcel()
                } else {
                    Toast.makeText(requireContext(), "需要存储权限", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * 导出今日记录到Excel
     */
    private fun exportTodayParcelsToExcel() {
        lifecycleScope.launch(Dispatchers.IO) {
            val todayParcels = AppDatabase.getInstance(requireContext()).parcelDao()
                .getTodayParcels(DateUtils.getStartOfToday())

            if (todayParcels.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "今日暂无记录", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val file = ExcelUtils.exportParcelsToExcel(todayParcels, requireContext())

            withContext(Dispatchers.Main) {
                if (file != null) {
                    shareFile(file)
                } else {
                    Toast.makeText(requireContext(), "导出失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 分享文件
     */
    private fun shareFile(file: File) {
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(Intent.createChooser(intent, "分享文件"))
    }
}