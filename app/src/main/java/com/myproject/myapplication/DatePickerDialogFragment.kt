package com.myproject.myapplication

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import java.sql.Date
import java.util.*


class DatePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable("date") as Date?
        val c = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        if (date != null) {
            c.time = date
        }
        val year = c.get(GregorianCalendar.YEAR)
        val month = c.get(GregorianCalendar.MONDAY)
        val day = c.get(GregorianCalendar.DAY_OF_MONTH)
        return DatePickerDialog(context!!, activity as TodoEditingActivity, year, month, day)
    }

}
