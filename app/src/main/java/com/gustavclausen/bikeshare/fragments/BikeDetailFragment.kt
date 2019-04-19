package com.gustavclausen.bikeshare.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.viewmodel.BikeViewModel
import kotlinx.android.synthetic.main.fragment_bike_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class BikeDetailFragment : Fragment() {

    private lateinit var mBikeLockId: String
    private lateinit var mBikeVM: BikeViewModel

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
        mBikeVM = ViewModelProviders.of(this, BikeViewModel.Factory(mBikeLockId)).get(BikeViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bike_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load bike asynchronous from DB and update UI upon completion
        doAsync {
            val bike = mBikeVM.bike

            uiThread {
                // Load picture of bike (asynchronous operation)
                Glide.with(context!!)
                     .load(bike.picture)
                     .centerCrop()
                     .placeholder(R.drawable.ic_missing_image)
                     .into(bike_photo)

                bike_type.text = bike.type
                bike_price.text = bike.priceHour.toString()
                bike_availability.text = if (bike.inUse) getString(R.string.in_use) else getString(R.string.not_in_use)
                bike_address.text = bike.lastLocationAddress
                bike_owner_name.text = bike.owner?.fullName
                bike_lock_id.text = bike.lockId
            }
        }
    }
}