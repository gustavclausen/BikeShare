package com.gustavclausen.bikeshare.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.DatePicker
import com.gustavclausen.bikeshare.R
import java.util.*

class DatePickerFragment : DialogFragment() {

    companion object {
        private const val ARG_DATE = "date"
        const val EXTRA_DATE = "com.gustavclausen.bikeshare.date"

        fun newInstance(calendar: Calendar): DatePickerFragment {
            val args = Bundle()
            args.putSerializable(ARG_DATE, calendar)

            val fragment = DatePickerFragment()
            fragment.arguments = args

            return fragment
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = arguments!!.getSerializable(ARG_DATE) as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_date, null)

        val datePicker: DatePicker = view.findViewById(R.id.dialog_date_picker)
        datePicker.init(year, month, day, null)

        return AlertDialog.Builder(context!!)
            .setView(view)
            .setTitle(R.string.date_picker_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                calendar.set(Calendar.YEAR, datePicker.year)
                calendar.set(Calendar.MONTH, datePicker.month)
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)

                sendResult(Activity.RESULT_OK, calendar)
            }.create()
    }

    private fun sendResult(resultCode: Int, calendar: Calendar) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(EXTRA_DATE, calendar)

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, intent)
    }
}