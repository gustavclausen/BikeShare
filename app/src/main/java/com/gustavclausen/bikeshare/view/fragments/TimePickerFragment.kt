package com.gustavclausen.bikeshare.view.fragments

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import java.util.*

class TimePickerFragment : DialogFragment() {

    companion object {
        private const val ARG_TIME = "time"
        const val EXTRA_TIME = "com.gustavclausen.bikeshare.time"

        fun newInstance(calendar: Calendar): TimePickerFragment {
            val args = Bundle()
            args.putSerializable(ARG_TIME, calendar)

            val fragment = TimePickerFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = arguments!!.getSerializable(TimePickerFragment.ARG_TIME) as Calendar

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(activity, { _, hourOfDay, minuteOfDay ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minuteOfDay)

            sendResult(Activity.RESULT_OK, calendar)
        }, hour, minute, DateFormat.is24HourFormat(activity))
    }

    private fun sendResult(resultCode: Int, calendar: Calendar) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(TimePickerFragment.EXTRA_TIME, calendar)

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, intent)
    }
}