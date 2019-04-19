package com.gustavclausen.bikeshare.services

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.gustavclausen.bikeshare.R
import java.io.IOException
import java.util.*

/**
 * Service that fetches address by reverse geocoding.
 *
 * Inspiration source (accessed 2019-04-14): https://developer.android.com/training/location/display-address
 */
class FetchAddressIntentService : IntentService("FetchAddressService") {

    private var mReceiver: ResultReceiver? = null

    object Constants {
        const val TAG = "FetchAddressService"
        private const val PACKAGE_NAME = "com.gustavclausen.bikeshare"

        const val EXTRA_SUCCESS_RESULT = 0
        const val EXTRA_FAILURE_RESULT = 1
        const val EXTRA_RESULT_DATA_KEY = "$PACKAGE_NAME.EXTRA_RESULT_DATA_KEY"
        const val EXTRA_RECEIVER = "$PACKAGE_NAME.EXTRA_RECEIVER"
        const val EXTRA_LOCATION_DATA = "$PACKAGE_NAME.EXTRA_LOCATION_DATA"
    }

    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        // Get the location passed to this service through an extra
        val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION_DATA)

        // Get result receiver passed to this service through an extra
        mReceiver = intent.getParcelableExtra(Constants.EXTRA_RECEIVER) ?: return

        val geocoder = Geocoder(this, Locale.getDefault())

        val addresses: List<Address>

        try {
            // Single address
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioe: IOException) {
            // Catch network or other I/O problems
            val errorMessage = getString(R.string.fetch_address_service_not_available)

            deliverResultToReceiver(Constants.EXTRA_FAILURE_RESULT, errorMessage)
            Log.e(Constants.TAG, errorMessage, ioe)

            return
        } catch (iae: IllegalArgumentException) {
            // Catch invalid latitude or longitude values
            Log.e(
                Constants.TAG,
                "Invalid latitude or longitude values:\n" +
                "Latitude = ${location.latitude}, Longitude =  ${location.longitude}",
                iae
            )

            return
        }

        // Handle case where no addresses were found
        if (addresses.isEmpty()) {
            val errorMessage = getString(R.string.no_addresses_found_error_message)

            deliverResultToReceiver(Constants.EXTRA_FAILURE_RESULT, errorMessage)
            Log.e(Constants.TAG, errorMessage)

            return
        }

        val firstAddress = addresses.first()

        // Fetch the address lines using getAddressLine and join them
        val addressFragments = with(firstAddress) {
            (0..maxAddressLineIndex).map { getAddressLine(it) }
        }
        deliverResultToReceiver(Constants.EXTRA_SUCCESS_RESULT, addressFragments.joinToString(separator = "\n"))
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(Constants.EXTRA_RESULT_DATA_KEY, message) }
        mReceiver?.send(resultCode, bundle)
    }
}