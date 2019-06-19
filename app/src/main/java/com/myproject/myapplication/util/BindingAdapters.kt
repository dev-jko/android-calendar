package com.myproject.myapplication.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myproject.myapplication.dailyCalendar.DailyAdapter

@BindingAdapter("replaceAll")
fun RecyclerView.replaceAll(list: List<DailyAdapter.Item>?) {
    (this.adapter as DailyAdapter).run {
        replaceAll(list)
        notifyDataSetChanged()
    }

}