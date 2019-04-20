package com.gustavclausen.bikeshare.view.fragments

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.utils.DateFormatter
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import kotlinx.android.synthetic.main.fragment_ride_handling.*
import java.util.*

class RideHandlingFragment : Fragment() {

    private lateinit var mRideId: String
    private lateinit var mRideVM: RideViewModel

    private var mEndDateTime = Calendar.getInstance()

    companion object {
        private const val SAVED_END_DATE_TIME = "savedEndDateTime"
        private const val ARG_RIDE_ID = "com.gustavclausen.bikeshare.arg_handling_ride_id"

        private const val DIALOG_DATE = "DialogDate"
        private const val DIALOG_TIME = "DialogTime"
        private const val DATE_REQUEST_CODE = 0
        private const val TIME_REQUEST_CODE = 1

        fun newInstance(rideId: String): RideHandlingFragment {
            val args = Bundle()
            args.putString(ARG_RIDE_ID, rideId)

            val fragment = RideHandlingFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            mEndDateTime = savedInstanceState.getSerializable(SAVED_END_DATE_TIME) as Calendar
        }

        activity?.title = getString(R.string.title_ride) // Set toolbar title of parent activity

        mRideId = arguments!!.getString(ARG_RIDE_ID)

        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride_handling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ride = mRideVM.getById(mRideId)!!

        ride_start_address.text = ride.startPositionAddress
        ride_start_time.text = DateFormatter.fullTimestamp(ride.startTime)

        ride_end_date_button.setOnClickListener {
            val picker: DatePickerFragment = DatePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, DATE_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_DATE)
        }

        ride_end_time_button.setOnClickListener {
            val picker: TimePickerFragment = TimePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, TIME_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_TIME)
        }

        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.fragment_ride_handling, menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            DATE_REQUEST_CODE -> {
                mEndDateTime = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Calendar
            }
            TIME_REQUEST_CODE -> {
                mEndDateTime = data!!.getSerializableExtra(TimePickerFragment.EXTRA_TIME) as Calendar
            }
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(SAVED_END_DATE_TIME, mEndDateTime)
    }

    private fun updateUI() {
        ride_end_date_button.text = DateFormatter.fullDate(mEndDateTime.time)
        ride_end_time_button.text = DateFormatter.hourMinute(mEndDateTime.time)
    }
}