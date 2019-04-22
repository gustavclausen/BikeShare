package com.gustavclausen.bikeshare.view.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.activities.BikeDetailActivity
import com.gustavclausen.bikeshare.view.utils.DateFormatter
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import kotlinx.android.synthetic.main.fragment_ride_detail.*

class RideDetailFragment : Fragment() {

    private lateinit var mRideId: String
    private lateinit var mRideVM: RideViewModel

    companion object {
        private const val ARG_RIDE_ID = "com.gustavclausen.bikeshare.arg_detail_ride_id"

        fun newInstance(bikeLockId: String): RideDetailFragment {
            val args = Bundle()
            args.putString(ARG_RIDE_ID, bikeLockId)

            val fragment = RideDetailFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRideId = arguments!!.getString(ARG_RIDE_ID)
        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ride = mRideVM.getById(mRideId)!!

        view_bike_button.setOnClickListener {
            // Open detail view of bike
            startActivity(BikeDetailActivity.newIntent(context!!, ride.bike!!.lockId))
        }
        rider.text = ride.rider?.fullName
        start_address.text = ride.startPositionAddress
        start_time.text = DateFormatter.fullTimestamp(ride.startTime)
        end_address.text = ride.endPositionAddress
        end_time.text = DateFormatter.fullTimestamp(ride.endTime)
        ride_distance.text = getString(R.string.distance_km_text, ride.distanceKm)
        ride_price.text = getString(R.string.money_amount_text, ride.finalPrice)
        ride_id.text = ride.id
    }
}