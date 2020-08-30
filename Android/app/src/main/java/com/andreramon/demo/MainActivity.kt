package com.andreramon.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.andreramon.demo.databinding.ActivityMainBinding
import com.andreramon.demo.ui.FlowFragment
import com.andreramon.demo.ui.RxFragment
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewPager()
    }

    private fun initViewPager() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pagerAdapter.fragments[position]?.second!!
        }.attach()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }

    private inner class ScreenSlidePagerAdapter(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

        val fragments = mapOf(
            0 to (FlowFragment.newInstance() to getString(R.string.fragment_flow_title)),
            1 to (RxFragment.newInstance() to getString(R.string.fragment_rx_title)),
        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]?.first!!
        }
    }
}