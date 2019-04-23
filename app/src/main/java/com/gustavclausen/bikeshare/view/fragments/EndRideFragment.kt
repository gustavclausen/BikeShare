package com.gustavclausen.bikeshare.view.fragments

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.data.entities.Coordinate
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.services.FetchAddressIntentService
import com.gustavclausen.bikeshare.view.activities.BikeShareActivity
import com.gustavclausen.bikeshare.view.activities.EndPositionPickerActivity
import com.gustavclausen.bikeshare.view.utils.DateFormatter
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import com.gustavclausen.bikeshare.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_ride_handling.*
import java.util.*
import java.util.concurrent.TimeUnit

class EndRideFragment : Fragment() {

    private lateinit var mRideId: String
    private var mEndDateTime = Calendar.getInstance()
    private var mEndPosition: Coordinate? = null
    private var mEndPositionAddress: String? = null

    private lateinit var mRideVM: RideViewModel
    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mUserVM: UserViewModel

    companion object {
        private const val SAVED_END_DATE_TIME = "savedEndDateTime"
        private const val SAVED_END_POSITION = "savedEndPosition"
        private const val SAVED_END_POSITION_ADDRESS = "savedEndPositionAddress"
        private const val ARG_RIDE_ID = "com.gustavclausen.bikeshare.arg_end_ride_ride_id"

        private const val DIALOG_DATE_TAG = "DialogDate"
        private const val DIALOG_TIME_TAG = "DialogTime"
        private const val DATE_REQUEST_CODE = 0
        private const val TIME_REQUEST_CODE = 1
        private const val END_POSITION_REQUEST_CODE = 2

        fun newInstance(rideId: String): EndRideFragment {
            val args = Bundle()
            args.putString(ARG_RIDE_ID, rideId)

            val fragment = EndRideFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        activity?.title = getString(R.string.title_ride) // Set toolbar title of parent activity

        if (savedInstanceState != null) {
            mEndDateTime = savedInstanceState.getSerializable(SAVED_END_DATE_TIME) as Calendar
            mEndPosition = savedInstanceState.getSerializable(SAVED_END_POSITION) as Coordinate?
            mEndPositionAddress = savedInstanceState.getString(SAVED_END_POSITION_ADDRESS)
        }

        mRideId = arguments!!.getString(ARG_RIDE_ID)

        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
        mBikeVM = ViewModelProviders.of(this).get(BikeViewModel::class.java)
        mUserVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
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
            // Show date picker
            val picker: DatePickerFragment = DatePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, DATE_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_DATE_TAG)
        }

        ride_end_time_button.setOnClickListener {
            // Show time picker
            val picker: TimePickerFragment = TimePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, TIME_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_TIME_TAG)
        }

        pick_end_position_button.setOnClickListener {
            // Open end position picker
            val intent = Intent(context, EndPositionPickerActivity::class.java)
            intent.putExtra(
                EndPositionPickerActivity.EXTRA_START_POSITION,
                Coordinate(ride.startPositionLatitude, ride.startPositionLongitude)
            )
            startActivityForResult(intent, END_POSITION_REQUEST_CODE)
        }

        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.fragment_ride_handling, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.end_ride_button -> {
                submit()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            DATE_REQUEST_CODE -> {
                mEndDateTime = data!!.getSerializableExtra(DatePickerFragment.EXTRA_CALENDAR) as Calendar
            }
            TIME_REQUEST_CODE -> {
                mEndDateTime = data!!.getSerializableExtra(TimePickerFragment.EXTRA_TIME) as Calendar
            }
            END_POSITION_REQUEST_CODE -> {
                mEndPosition = data!!.getSerializableExtra(EndPositionPickerActivity.EXTRA_END_POSITION) as Coordinate
                fetchPositionAddress(mEndPosition!!)
            }
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(SAVED_END_DATE_TIME, mEndDateTime)
        outState.putSerializable(SAVED_END_POSITION, mEndPosition)
        outState.putSerializable(SAVED_END_POSITION_ADDRESS, mEndPositionAddress)
    }

    private fun updateUI() {
        ride_end_date_button.text = DateFormatter.fullDate(mEndDateTime.time)
        ride_end_time_button.text = DateFormatter.hourMinute(mEndDateTime.time)

        // Disable location picker button if end location has been selected
        pick_end_position_button.isEnabled = mEndPosition == null

        if (mEndPositionAddress != null) {
            end_location_address.visibility = View.VISIBLE
            end_location_address.text = mEndPositionAddress
        }
    }

    private fun fetchPositionAddress(coordinate: Coordinate) {
        val intent = Intent(context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.EXTRA_RECEIVER, AddressResultReceiver())
            putExtra(FetchAddressIntentService.Constants.EXTRA_LOCATION_DATA, coordinate)
        }

        context?.startService(intent)
    }

    private fun submit() {
        val ride = mRideVM.getById(mRideId)!!

        // Validate that end date time is after start date time
        if (mEndDateTime.time < ride.startTime) {
            Toast.makeText(context!!, getString(R.string.date_time_input_error), Toast.LENGTH_SHORT).show()
            return
        }

        // Validate that end location is selected
        if (mEndPosition == null) {
            Toast.makeText(context!!, getString(R.string.missing_end_location_error), Toast.LENGTH_SHORT).show()
            return
        }

        val durationHours = calculateDurationInHours(ride.startTime, mEndDateTime.time)
        val distanceKm = calculateDistanceInKm(ride)
        val ridePrice = durationHours * ride.bike!!.priceHour.toDouble()

        displayPaymentDialog(durationHours, distanceKm, ridePrice)
    }

    private fun calculateDistanceInKm(ride: Ride): Double {
        val startPosition = Location("gps")
        startPosition.latitude = ride.startPositionLatitude
        startPosition.longitude = ride.startPositionLongitude

        val endPosition = Location("gps")
        endPosition.latitude = mEndPosition!!.latitude
        endPosition.longitude = mEndPosition!!.longitude

        return startPosition.distanceTo(endPosition).toDouble() / 1000
    }

    private fun calculateDurationInHours(from: Date, to: Date): Double {
        val secondsDiff = TimeUnit.MILLISECONDS.toSeconds(to.time - from.time)
        return secondsDiff.toDouble() / 60.0 / 60.0
    }

    private fun displayPaymentDialog(durationHours: Double, distanceKm: Double, ridePrice: Double) {
        val paymentDialog = AlertDialog.Builder(context!!).setTitle(R.string.title_payment_dialog)

        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)

        val rideDurationField = dialogView.findViewById<TextView>(R.id.ride_duration)
        rideDurationField.text = getString(R.string.duration_placeholder_text, durationHours)

        val rideDistanceField = dialogView.findViewById<TextView>(R.id.ride_distance)
        rideDistanceField.text = getString(R.string.distance_placeholder_text, distanceKm)

        val paymentAmountField = dialogView.findViewById<TextView>(R.id.ride_payment_amount)
        paymentAmountField.text = getString(R.string.payment_amount_placeholder_text, ridePrice)

        paymentDialog.setPositiveButton(R.string.button_dialog_pay) { _, _ ->
            endRide(distanceKm, ridePrice)
        }
        paymentDialog.setView(dialogView)
        paymentDialog.create()
        paymentDialog.show()
    }

    private fun endRide(distanceKm: Double, ridePrice: Double) {
        mRideVM.endRide(
            id = mRideId,
            endPositionLatitude = mEndPosition!!.latitude,
            endPositionLongitude = mEndPosition!!.longitude,
            endPositionAddress = mEndPositionAddress ?: "N/A",
            distanceKm = distanceKm,
            finalPrice = ridePrice,
            endTime = mEndDateTime.time
        )

        val ride = mRideVM.getById(mRideId)!!
        val bikeLockId = ride.bike!!.lockId

        // Update status of bike
        mBikeVM.updateAvailability(bikeLockId, inUse = false)
        mBikeVM.updatePosition(bikeLockId, mEndPosition!!, mEndPositionAddress!!)

        // Update user balance
        mUserVM.subtractFromBalance(ride.rider!!.id, ridePrice)

        // Show user bike map
        val bikeShareActivity = (activity as BikeShareActivity)
        bikeShareActivity.updateLastRide(null)
        bikeShareActivity.loadRideFragment()
    }


    internal inner class AddressResultReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            val resultMessage = resultData?.getString(FetchAddressIntentService.Constants.EXTRA_RESULT_DATA_KEY) ?: ""

            if (resultCode == FetchAddressIntentService.Constants.EXTRA_FAILURE_RESULT) {
                Toast.makeText(context!!, resultMessage, Toast.LENGTH_SHORT).show()
            } else if (resultCode == FetchAddressIntentService.Constants.EXTRA_SUCCESS_RESULT) {
                mEndPositionAddress = resultMessage
                updateUI()
            }
        }
    }
}