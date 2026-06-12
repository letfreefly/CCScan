package com.example.ccscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.ccscan.databinding.ActivityMainBinding
import com.example.ccscan.util.TTSManager
import com.example.ccscan.workmanager.BackupScheduler
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化TTS
        TTSManager.init(this)

        // 启动自动备份
        BackupScheduler.startBackupScheduler(this)

        // 设置ViewPager
        binding.viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 3

        // 设置底部导航
        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_register -> binding.viewPager.currentItem = 0
                R.id.navigation_records -> binding.viewPager.currentItem = 1
                R.id.navigation_database -> binding.viewPager.currentItem = 2
            }
            true
        }

        // ViewPager切换时更新底部导航
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onDestroy() {
        TTSManager.shutdown()
        super.onDestroy()
    }

    /**
     * ViewPager适配器
     */
    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = listOf(
            RegisterFragment(),
            RecordsFragment(),
            DatabaseFragment()
        )

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size
    }
}