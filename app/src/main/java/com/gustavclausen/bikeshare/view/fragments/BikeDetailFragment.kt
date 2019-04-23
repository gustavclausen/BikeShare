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

class BikeDetailFragment : Fragment() {

    private lateinit var mBikeLockId: String
    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mRideVM: RideViewModel

    private var mRideListDialog: AlertDialog? = null
    private var mPaymentListDialog: AlertDialog? = null

    private var mRideListIsShowing: Boolean = false
    private var mPaymentListIsShowing: Boolean = false

    companion object {
        private const val ARG_BIKE_LOCK_ID = "com.gustavclausen.bikeshare.arg_detail_bike_lock_id"
        private const val SAVED_RIDE_LIST_IS_SHOWING = "savedRideListIsShowing"
        private const val SAVED_PAYMENT_LIST_IS_SHOWING = "savedPaymentListIsShowing"

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

        if (savedInstanceState != null) {
            mRideListIsShowing = savedInstanceState.getBoolean(SAVED_RIDE_LIST_IS_SHOWING)
            mPaymentListIsShowing = savedInstanceState.getBoolean(SAVED_PAYMENT_LIST_IS_SHOWING)
        }

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
            showRideListDialog()
        }

        view_payments_button.setOnClickListener {
            showPaymentListDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        if (mRideListIsShowing) {
            showRideListDialog()
        } else if (mPaymentListIsShowing) {
            showPaymentListDialog()
        }
    }

    override fun onPause() {
        super.onPause()

        mRideListDialog?.dismiss()
        mPaymentListDialog?.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SAVED_RIDE_LIST_IS_SHOWING, mRideListIsShowing)
        outState.putBoolean(SAVED_PAYMENT_LIST_IS_SHOWING, mPaymentListIsShowing)
    }

    private fun showRideListDialog() {
        mRideListDialog = createListDialog()
        mRideListDialog!!.setOnShowListener {
            mRideListIsShowing = true
        }
        mRideListDialog!!.setOnDismissListener {
            mRideListIsShowing = false
        }

        val dialogView = layoutInflater.inflate(R.layout.fragment_rides_overview, mRideListDialog!!.listView)

        val adapter = RidesRecyclerAdapter(context!!)
        adapter.setList(mRideVM.getAllRidesForBike(mBikeLockId))

        val listView = dialogView.findViewById<RecyclerView>(R.id.ride_list)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = adapter

        mRideListDialog!!.setView(dialogView)
        mRideListDialog!!.show()
    }

    private fun showPaymentListDialog() {
        mPaymentListDialog = createListDialog()
        mPaymentListDialog!!.setOnShowListener {
            mPaymentListIsShowing = true
        }
        mPaymentListDialog!!.setOnDismissListener {
            mPaymentListIsShowing = false
        }

        val dialogView = layoutInflater.inflate(R.layout.fragment_payment_overview, mPaymentListDialog!!.listView)

        val allRides = mRideVM.getAllRidesForBike(mBikeLockId)
        allRides.addChangeListener { rides ->
            val totalAmountField = dialogView.findViewById<TextView>(R.id.payment_total_amount)
            val totalCalculatedAmount = rides.toList().fold(0.0) { acc, ride -> acc + ride.finalPrice }

            totalAmountField.text = getString(R.string.total_amount_text, totalCalculatedAmount)
        }
        
        val adapter = RidePaymentRecyclerAdapter(context!!)
        adapter.setList(allRides)

        val listView = dialogView.findViewById<RecyclerView>(R.id.payment_list)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = adapter

        mPaymentListDialog!!.setView(dialogView)
        mPaymentListDialog!!.show()
    }

    private fun createListDialog(): AlertDialog {
        return AlertDialog.Builder(context!!)
            .setPositiveButton(R.string.button_close, null)
            .create()
    }
}