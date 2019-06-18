package com.myproject.myapplication.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.myproject.myapplication.DataBaseOpenHelper
import com.myproject.myapplication.R
import com.myproject.myapplication.dailyCalendar.DailyCalendarFragment
import com.myproject.myapplication.fragments.MonthlyCalendarFragment
import com.myproject.myapplication.fragments.WeeklyCalendarFragment
import kotlinx.android.synthetic.main.activity_main.*
import com.myproject.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DataBaseOpenHelper

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        pager.adapter = MyPagerAdapter(supportFragmentManager)

        dbHelper = DataBaseOpenHelper(this)
    }

    override fun onResume() {
        viewModel.setItemIndex(viewModel.getInitPagerItem(this))
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.savePagerItem(this)
    }

    class MyPagerAdapter(fm: androidx.fragment.app.FragmentManager) :
        androidx.fragment.app.FragmentStatePagerAdapter(fm) {
        override fun getItem(p0: Int): androidx.fragment.app.Fragment {
            return when (p0) {
                0 -> MonthlyCalendarFragment()
                1 -> DailyCalendarFragment()
                else -> WeeklyCalendarFragment()
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }
}
