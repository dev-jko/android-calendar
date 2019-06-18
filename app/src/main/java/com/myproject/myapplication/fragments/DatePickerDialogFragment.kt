package com.myproject.myapplication.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.myproject.myapplication.TodoEditingActivity
import java.sql.Date
import java.util.*


class DatePickerDialogFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable("date") as Date?
        val c = GregorianCalendar.getInstance()
        if (date != null) {
            c.time = date
        }
        val year = c.get(GregorianCalendar.YEAR)
        val month = c.get(GregorianCalendar.MONDAY)
        val day = c.get(GregorianCalendar.DAY_OF_MONTH)
        return DatePickerDialog(context!!, activity as TodoEditingActivity, year, month, day)
    }

}
