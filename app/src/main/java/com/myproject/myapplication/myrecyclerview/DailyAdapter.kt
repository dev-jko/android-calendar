package com.myproject.myapplication.myrecyclerview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.myproject.myapplication.CalendarData
import com.myproject.myapplication.R
import com.myproject.myapplication.TodoEditingActivity
import kotlinx.android.synthetic.main.recyclerview_item_daily.view.*
import kotlinx.android.synthetic.main.recyclerview_item_daily_todo.view.*
import java.sql.Date

class DailyAdapter(
    private val dataList: ArrayList<Item>,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val DATE = 0
        const val TODO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View
        return if(viewType == DATE) {
            view = inflater.inflate(R.layout.recyclerview_item_daily, parent, false)
            DateViewHolder(view)
        } else {
            view = inflater.inflate(R.layout.recyclerview_item_daily_todo, null, false)
            TodoViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        if (item.type == DATE) {
            val dateHolder = holder as DateViewHolder
            dateHolder.dateTextView.text = item.content.toString()
            dateHolder.dateTextView.setOnClickListener {
                if (item.invisibleChildren!!.size == 0) {
                    val pos = dataList.indexOf(item)
                    var count = 0
                    while (dataList.size > pos + 1 && dataList[pos + 1].type == TODO) {
                        item.invisibleChildren.add(dataList.removeAt(pos + 1).content as CalendarData)
                        count++
                    }
                    notifyItemRangeRemoved(pos + 1, count)
                } else {
                    val pos = dataList.indexOf(item)
                    var index = pos + 1
                    item.invisibleChildren.forEach { dataList.add(index++, Item(TODO, it)) }
                    notifyItemRangeInserted(pos + 1, index - pos - 1)
                    item.invisibleChildren.clear()
                }
            }
            dateHolder.todoAddBtn.setOnClickListener {
                val intent = Intent(context, TodoEditingActivity::class.java)
                intent.putExtra("startDate", item.content as Date)
                intent.putExtra("endDate", item.content)
                (context as Activity).startActivityForResult(intent, Activity.RESULT_OK)
            }
        } else {
            val todoHolder = holder as TodoViewHolder
            todoHolder.todoTextView.text = (item.content as CalendarData).content
        }
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.daily_item_date_text_view
        val todoAddBtn: Button = itemView.btn_daily_todo_add
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoTextView: TextView = itemView.daily_todo_text_view
        val todoDeleteBtn: Button = itemView.btn_daily_todo_delete
    }

    class Item(val type: Int, val content: Any, val invisibleChildren: ArrayList<CalendarData>? = null)

}