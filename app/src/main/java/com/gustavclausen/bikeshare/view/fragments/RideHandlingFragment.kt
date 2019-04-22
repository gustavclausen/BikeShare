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
import com.gustavclausen.bikeshare.services.FetchAddressIntentService
import com.gustavclausen.bikeshare.view.activities.BikeShareActivity
import com.gustavclausen.bikeshare.view.activities.LocationPickerActivity
import com.gustavclausen.bikeshare.view.utils.DateFormatter
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import com.gustavclausen.bikeshare.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_ride_handling.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class RideHandlingFragment : Fragment() {

    private lateinit var mRideId: String
    private lateinit var mRideVM: RideViewModel
    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mUserVM: UserViewModel

    private var mEndDateTime = Calendar.getInstance()
    private var mEndLocation: Coordinate? = null
    private var mEndLocationAddress: String? = null

    companion object {
        private const val SAVED_END_DATE_TIME = "savedEndDateTime"
        private const val SAVED_END_LOCATION  = "savedEndLocation"
        private const val SAVED_END_LOCATION_ADDRESS  = "savedEndLocationAddress"
        private const val ARG_RIDE_ID = "com.gustavclausen.bikeshare.arg_handling_ride_id"

        private const val DIALOG_DATE = "DialogDate"
        private const val DIALOG_TIME = "DialogTime"
        private const val DATE_REQUEST_CODE = 0
        private const val TIME_REQUEST_CODE = 1
        private const val END_LOCATION_REQUEST_CODE = 2

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
            mEndLocation = savedInstanceState.getSerializable(SAVED_END_LOCATION) as Coordinate?
            mEndLocationAddress = savedInstanceState.getString(SAVED_END_LOCATION_ADDRESS)
        }

        activity?.title = getString(R.string.title_ride) // Set toolbar title of parent activity

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
            val picker: DatePickerFragment = DatePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, DATE_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_DATE)
        }

        ride_end_time_button.setOnClickListener {
            val picker: TimePickerFragment = TimePickerFragment.newInstance(mEndDateTime)
            picker.setTargetFragment(this, TIME_REQUEST_CODE)
            picker.show(fragmentManager, DIALOG_TIME)
        }

        pick_end_location_button.setOnClickListener {
            val intent = Intent(context, LocationPickerActivity::class.java)
            intent.putExtra(LocationPickerActivity.ARG_START_LOCATION, Coordinate(ride.startPositionLat, ride.startPositionLong))
            startActivityForResult(intent, END_LOCATION_REQUEST_CODE)
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
                endRide()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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
            END_LOCATION_REQUEST_CODE -> {
                mEndLocation = data!!.getSerializableExtra(LocationPickerActivity.EXTRA_END_LOCATION) as Coordinate
                fetchLocationAddress(mEndLocation!!)
            }
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(SAVED_END_DATE_TIME, mEndDateTime)
        outState.putSerializable(SAVED_END_LOCATION, mEndLocation)
        outState.putSerializable(SAVED_END_LOCATION_ADDRESS, mEndLocationAddress)
    }

    private fun updateUI() {
        ride_end_date_button.text = DateFormatter.fullDate(mEndDateTime.time)
        ride_end_time_button.text = DateFormatter.hourMinute(mEndDateTime.time)

        // Disable location picker button if end location has been selected
        pick_end_location_button.isEnabled = mEndLocation == null

        if (mEndLocationAddress != null) {
            end_location_address.visibility = View.VISIBLE
            end_location_address.text = mEndLocationAddress
        }
    }

    private fun fetchLocationAddress(coordinate: Coordinate) {
        val intent = Intent(context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.EXTRA_RECEIVER, AddressResultReceiver())
            putExtra(FetchAddressIntentService.Constants.EXTRA_LOCATION_DATA, coordinate)
        }

        context?.startService(intent)
    }

    private fun endRide() {
        val ride = mRideVM.getById(mRideId)!!

        // Validate that end date time is after start date time
        if (mEndDateTime.time < ride.startTime) {
            Toast.makeText(context!!, getString(R.string.date_time_input_error), Toast.LENGTH_SHORT).show()
            return
        }

        // Validate that end location is selected
        if (mEndLocation == null) {
            Toast.makeText(context!!, getString(R.string.missing_end_location_error), Toast.LENGTH_SHORT).show()
            return
        }

        val startLocation = Location("start")
        startLocation.latitude = ride.startPositionLat
        startLocation.longitude = ride.startPositionLong

        val endLocation = Location("end")
        endLocation.latitude = mEndLocation!!.lat
        endLocation.longitude = mEndLocation!!.long

        val distanceKm = startLocation.distanceTo(endLocation).toDouble() / 1000

        val hourDiff = hourDiff(ride.startTime, mEndDateTime.time)
        val finalPrice = hourDiff * ride.bike!!.priceHour.toDouble()

        // Make payment dialog
        val paymentDialog = AlertDialog.Builder(context!!).setTitle(R.string.payment_dialog_title)

        val dialogContentView = layoutInflater.inflate(R.layout.dialog_payment, null)

        val rideDuration = dialogContentView.findViewById<TextView>(R.id.ride_duration)
        rideDuration.text = getString(R.string.duration_placeholder_text, getString(R.string.duration_hours_text, hourDiff))

        val rideDistance = dialogContentView.findViewById<TextView>(R.id.ride_distance)
        rideDistance.text = getString(R.string.distance_placeholder_text, getString(R.string.distance_km_text, distanceKm))

        val paymentAmount = dialogContentView.findViewById<TextView>(R.id.ride_payment_amount)
        paymentAmount.text = getString(R.string.payment_amount_placeholder_text, getString(R.string.money_amount_text, finalPrice))

        paymentDialog.setView(dialogContentView)
        paymentDialog.setPositiveButton(R.string.button_dialog_pay) { _, _ ->
            // Update states
            mRideVM.endRide(
                id = mRideId,
                endPositionLat = mEndLocation!!.lat,
                endPositionLong = mEndLocation!!.long,
                endPositionAddress = mEndLocationAddress!!,
                distanceKm = distanceKm,
                finalPrice = finalPrice,
                endTime = mEndDateTime.time
            )

            val bikeLockId = ride.bike!!.lockId

            // Update status of bike
            mBikeVM.updateAvailability(bikeLockId, inUse = false)
            mBikeVM.updateLastKnownLocation(bikeLockId, mEndLocation!!, mEndLocationAddress!!)

            // Update user balance
            mUserVM.substractFromBalance(ride.rider!!.id, finalPrice)

            val bikeShareActivity = (activity as BikeShareActivity)
            bikeShareActivity.updateLastRide(null)
            bikeShareActivity.loadRideFragment()
        }

        paymentDialog.create()
        paymentDialog.show()
    }

    private fun hourDiff(from: Date, to: Date) : Double {
        val secondsDiff = TimeUnit.MILLISECONDS.toSeconds(to.time - from.time)
        return secondsDiff.toDouble() / 60.0 / 60.0
    }

    internal inner class AddressResultReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            val resultMessage = resultData?.getString(FetchAddressIntentService.Constants.EXTRA_RESULT_DATA_KEY) ?: ""

            if (resultCode == FetchAddressIntentService.Constants.EXTRA_FAILURE_RESULT) {
                Toast.makeText(context!!, resultMessage, Toast.LENGTH_SHORT).show()
            } else if (resultCode == FetchAddressIntentService.Constants.EXTRA_SUCCESS_RESULT) {
                mEndLocationAddress = resultMessage
                updateUI()
            }
        }
    }
}