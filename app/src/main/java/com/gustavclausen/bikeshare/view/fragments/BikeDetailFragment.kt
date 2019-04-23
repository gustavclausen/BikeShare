package com.gustavclausen.bikeshare.view.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.adapters.RidePaymentRecyclerAdapter
import com.gustavclausen.bikeshare.view.adapters.RidesRecyclerAdapter
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import kotlinx.android.synthetic.main.fragment_bike_detail.*

// TODO: Fix rotation bug in terms of dialogs for rides and payments
class BikeDetailFragment : Fragment() {

    private lateinit var mBikeLockId: String
    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mRideVM: RideViewModel

    companion object {
        private const val ARG_BIKE_LOCK_ID = "com.gustavclausen.bikeshare.arg_detail_bike_lock_id"

        fun newInstance(bikeLockId: String): BikeDetailFragment {
            val args = Bundle()
            args.putString(ARG_BIKE_LOCK_ID, bikeLockId)

            val fragment = BikeDetailFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBikeLockId = arguments!!.getString(ARG_BIKE_LOCK_ID)
        mBikeVM = ViewModelProviders.of(this).get(BikeViewModel::class.java)
        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bike_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bike = mBikeVM.getById(mBikeLockId)!!

        // Load picture of bike (asynchronous operation)
        Glide.with(context!!)
             .load(bike.picture)
             .centerCrop()
             .placeholder(R.drawable.ic_missing_image)
             .into(bike_photo)

        bike_type.text = bike.type
        bike_price.text = bike.priceHour.toString()
        bike_availability.text = if (bike.inUse) getString(R.string.in_use) else getString(R.string.not_in_use)
        ride_end_address.text = bike.positionAddress
        bike_owner_name.text = bike.owner?.fullName
        bike_lock_id.text = bike.lockId

        view_rides_button.setOnClickListener {
            // TODO: Separate to function
            val rideDialog = AlertDialog.Builder(context!!)
                                        .setPositiveButton(R.string.button_close, null)
                                        .create()

            val rideList = layoutInflater.inflate(R.layout.fragment_rides_overview, null)

            val rideAdapter = RidesRecyclerAdapter(context!!)
            rideAdapter.setList(mRideVM.getAllRidesForBike(mBikeLockId))

            val ridesList = rideList.findViewById(R.id.ride_list) as RecyclerView
            ridesList.layoutManager = LinearLayoutManager(activity)
            ridesList.adapter = rideAdapter

            rideDialog.setView(rideList)
            rideDialog.show()
        }

        view_payments_button.setOnClickListener {
            // TODO: Separate to function
            val paymentDialog = AlertDialog.Builder(context!!)
                                           .setPositiveButton(R.string.button_close, null)
                                           .create()

            val paymentList = layoutInflater.inflate(R.layout.fragment_payment_overview, null)

            val rides = mRideVM.getAllRidesForBike(mBikeLockId)

            val paymentAdapter = RidePaymentRecyclerAdapter(context!!)
            paymentAdapter.setList(rides)

            rides.addChangeListener { rides ->
                val total = paymentList.findViewById(R.id.payment_total_amount) as TextView
                total.text = "Total: ${rides.toList().fold(0.0) { acc, ride -> acc + ride.finalPrice}}"
            }

            val paymentRecyclerView = paymentList.findViewById(R.id.payment_list) as RecyclerView
            paymentRecyclerView.layoutManager = LinearLayoutManager(activity)
            paymentRecyclerView.adapter = paymentAdapter

            paymentDialog.setView(paymentList)
            paymentDialog.show()
        }
    }
}