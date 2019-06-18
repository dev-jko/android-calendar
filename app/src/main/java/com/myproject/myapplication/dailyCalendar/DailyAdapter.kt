package com.myproject.myapplication.dailyCalendar

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.myproject.myapplication.CalendarData
import com.myproject.myapplication.R
import com.myproject.myapplication.TodoEditingActivity
import kotlinx.android.synthetic.main.recyclerview_item_daily.view.*
import kotlinx.android.synthetic.main.recyclerview_item_daily_todo.view.*
import java.sql.Date

class DailyAdapter(private val fragment: androidx.fragment.app.Fragment) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        const val DATE = 0
        const val TODO = 1
    }

    val dataList: ArrayList<Item> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val context = parent.context
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View
        return if (viewType == DATE) {
            view = inflater.inflate(R.layout.recyclerview_item_daily, parent, false)
            DateViewHolder(view)
        } else {
            view = inflater.inflate(R.layout.recyclerview_item_daily_todo, parent, false)
            TodoViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        if (item.type == DATE) {
            bindDateHolder(holder, item, item.invisibleChildren!!, item.content as Date)
        } else {
            bindTodoHolder(holder, item, item.content as CalendarData, fragment as DailyCalendarFragment)
        }
    }

    private fun bindTodoHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        item: Item,
        content: CalendarData,
        fragment: DailyCalendarFragment
    ) {
        val todoHolder = holder as TodoViewHolder
        todoHolder.todoTextView.text = (item.content as CalendarData).content
        todoHolder.todoDeleteBtn.setOnClickListener {
            if (fragment.deleteData(content.id) == 1) {
                fragment.deleteList(content.id)
                Toast.makeText(fragment.context, "삭제 완료", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(fragment.context, "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindDateHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        item: Item,
        invisibleChildren: ArrayList<CalendarData>,
        content: Date
    ) {
        val dateHolder = holder as DateViewHolder
        dateHolder.dateTextView.text = item.content.toString()
        dateHolder.dateTextView.setOnClickListener {
            if (item.invisibleChildren!!.size == 0) {
                val pos = dataList.indexOf(item)
                var count = 0
                while (dataList.size > pos + 1 && dataList[pos + 1].type == TODO) {
                    invisibleChildren.add(dataList.removeAt(pos + 1).content as CalendarData)
                    count++
                }
                notifyItemRangeRemoved(pos + 1, count)
            } else {
                val pos = dataList.indexOf(item)
                var index = pos + 1
                invisibleChildren.forEach { dataList.add(index++,
                    Item(
                        TODO,
                        it
                    )
                ) }
                notifyItemRangeInserted(pos + 1, index - pos - 1)
                invisibleChildren.clear()
            }
        }
        dateHolder.todoAddBtn.setOnClickListener {
            val intent = Intent(fragment.context, TodoEditingActivity::class.java)
            intent.putExtra("startDate", item.content as Date)
            intent.putExtra("endDate", content)
            fragment.startActivityForResult(intent, 50)
        }
    }


    fun addItem(item: Item, position: Int) {
        dataList.add(position, item)
        notifyItemInserted(position)
    }

    class DateViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.daily_item_date_text_view
        val todoAddBtn: Button = itemView.btn_daily_todo_add
    }

    class TodoViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val todoTextView: TextView = itemView.daily_todo_text_view
        val todoDeleteBtn: Button = itemView.btn_daily_todo_delete
    }

    class Item(val type: Int, val content: Any, val invisibleChildren: ArrayList<CalendarData>? = null)

}