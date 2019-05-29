package com.myproject.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.myproject.myapplication.fragments.DailyCalendarFragment
import com.myproject.myapplication.fragments.MonthlyCalendarFragment
import com.myproject.myapplication.fragments.WeeklyCalendarFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Date

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DataBaseOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DataBaseOpenHelper(this)

        pager.adapter = MyPagerAdapter(supportFragmentManager)
        pager.currentItem = this.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("pagerItem", 0)

        val movePageListener: View.OnClickListener = View.OnClickListener {
            val tag: Int = it.tag as Int
            pager.currentItem = tag
        }

        btn_monthly_tab.setOnClickListener(movePageListener)
        btn_monthly_tab.tag = 0
        btn_daily_tab.setOnClickListener(movePageListener)
        btn_daily_tab.tag = 1
        btn_weekly_tab.setOnClickListener(movePageListener)
        btn_weekly_tab.tag = 2
    }

    override fun onPause() {
        super.onPause()

        this.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putInt("pagerItem", pager.currentItem)
            .apply()
    }

    class MyPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "11111111111111111122222222222222222  ${data.toString()}")
        if (requestCode == resultCode){
            val calendarData = CalendarData(
                data!!.getIntExtra("id", 0),
                data.getSerializableExtra("startDate") as Date,
                data.getSerializableExtra("endDate") as Date,
                data.getStringExtra("content")
            )
            (supportFragmentManager.findFragmentByTag("1") as DailyCalendarFragment).updateList(calendarData)

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
