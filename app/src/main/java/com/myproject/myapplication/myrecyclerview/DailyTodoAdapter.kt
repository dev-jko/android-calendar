package com.myproject.myapplication.myrecyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.myproject.myapplication.Repository.CalendarData
import com.myproject.myapplication.R


class DailyTodoAdapter(val mData: ArrayList<CalendarData>) : androidx.recyclerview.widget.RecyclerView.Adapter<DailyTodoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.recyclerview_item_daily_todo, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contentTextView.text = mData[position].content
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val contentTextView: TextView = itemView.findViewById(R.id.daily_todo_text_view)
    }
}