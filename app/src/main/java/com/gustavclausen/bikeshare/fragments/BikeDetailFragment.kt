package com.gustavclausen.bikeshare.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.models.Bike
import com.gustavclausen.bikeshare.models.BikeDB
import kotlinx.android.synthetic.main.fragment_bike_detail.*

class BikeDetailFragment : Fragment() {

    lateinit var mBike: Bike

    companion object {
        private const val ARG_BIKE_LOCK_ID = "com.gustavclausen.bikeshare.arg_bike_lock_id"

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

        val bikeLockId = arguments!!.getString(ARG_BIKE_LOCK_ID)
        mBike = BikeDB.get().getBike(bikeLockId)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bike_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load picture
        Glide.with(this).load(mBike.picture).centerCrop().placeholder(R.drawable.ic_missing_image).into(bike_photo)

        bike_type_text.text = mBike.type
        bike_price_text.text = mBike.priceHour.toString()
        bike_availability_text.text = if (mBike.inUse) getString(R.string.in_use) else getString(R.string.not_in_use)
        bike_address_text.text = mBike.lastLocationAddress
        bike_owner_text.text = mBike.owner.toString()
        bike_lock_id_text.text = mBike.lockId
    }
}