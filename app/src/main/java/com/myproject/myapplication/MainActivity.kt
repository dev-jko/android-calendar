package com.myproject.myapplication

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.myproject.myapplication.fragments.DailyCalendarFragment
import com.myproject.myapplication.fragments.MonthlyCalendarFragment
import com.myproject.myapplication.fragments.WeeklyCalendarFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DataBaseOpenHelper
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DataBaseOpenHelper(this)

        pager.adapter = MyPagerAdapter(supportFragmentManager)
        pager.currentItem = this.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("pagerItem", 0)

        RxView.clicks(btn_monthly_tab)
            .subscribe { pager.currentItem = 0 }
            .apply { disposables.add(this) }

        RxView.clicks(btn_daily_tab)
            .subscribe { pager.currentItem = 1 }
            .apply { disposables.add(this) }

        RxView.clicks(btn_weekly_tab)
            .subscribe { pager.currentItem = 2 }
            .apply { disposables.add(this) }
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

    override fun isDestroyed(): Boolean {
        disposables.clear()
        return super.isDestroyed()
    }
}
