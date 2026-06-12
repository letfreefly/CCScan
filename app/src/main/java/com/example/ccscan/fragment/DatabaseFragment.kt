package com.example.ccscan.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ccscan.CustomerDatabaseActivity
import com.example.ccscan.ProductDatabaseActivity
import com.example.ccscan.databinding.FragmentDatabaseBinding

/**
 * 资料库Fragment
 */
class DatabaseFragment : Fragment() {

    private lateinit var binding: FragmentDatabaseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 客户唛头资料库入口
        binding.btnCustomerDatabase.setOnClickListener {
            val intent = Intent(requireContext(), CustomerDatabaseActivity::class.java)
            startActivity(intent)
        }

        // 商品名称资料库入口
        binding.btnProductDatabase.setOnClickListener {
            val intent = Intent(requireContext(), ProductDatabaseActivity::class.java)
            startActivity(intent)
        }
    }
}