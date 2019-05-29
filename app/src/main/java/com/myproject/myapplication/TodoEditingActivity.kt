package com.myproject.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.Toast
import com.myproject.myapplication.fragments.DatePickerDialogFragment
import kotlinx.android.synthetic.main.activity_todo_editing.*
import java.sql.Date
import java.util.*


class TodoEditingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    lateinit var startDate: Date
    lateinit var endDate: Date
    var datePickerFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_editing)

        startDate = intent.getSerializableExtra("startDate") as Date
        endDate = intent.getSerializableExtra("endDate") as Date

        text_view_todo_editing_start_date.text = startDate.toString()
        text_view_todo_editing_end_date.text = endDate.toString()


        linear_layout_todo_editing_start.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("date", startDate)
            dialog.arguments = bundle
            datePickerFlag = 0
            dialog.show(this.supportFragmentManager, "startDatePicker")
        }

        linear_layout_todo_editing_end.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("date", endDate)
            dialog.arguments = bundle
            datePickerFlag = 1
            dialog.show(this.supportFragmentManager, "endDatePicker")
        }

        btn_todo_editing_save.setOnClickListener {
            val db = DataBaseOpenHelper(this).writableDatabase
            val values = ContentValues().apply {
                put(CalendarDBContract.COLUMN_START_DATE, startDate.time)
                put(CalendarDBContract.COLUMN_END_DATE, endDate.time)
                put(CalendarDBContract.COLUMN_CONTENT, edit_text_todo_editing_content.text.toString())
            }
            val result = db.insert(CalendarDBContract.TABLE_NAME, null, values)
            if (result == -1L) {
                Toast.makeText(this, "오류가 발생했습니다. 다시 저장해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent()
                intent.putExtra("id", result)
                intent.putExtra("startDate", startDate)
                intent.putExtra("endDate", endDate)
                intent.putExtra("content", edit_text_todo_editing_content.text.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        btn_todo_editing_cancel.setOnClickListener {
            onBackPressed()
        }


    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        c.set(year, month, dayOfMonth)
        if (datePickerFlag == 0) {
            startDate = Date(c.time.time)
            text_view_todo_editing_start_date.text = startDate.toString()
        } else {
            endDate = Date(c.time.time)
            text_view_todo_editing_end_date.text = endDate.toString()
        }
    }


}
