package com.gustavclausen.bikeshare.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.gustavclausen.bikeshare.BikeShareApplication
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.dialogs.InfoDialog
import com.gustavclausen.bikeshare.models.BikeDB
import com.gustavclausen.bikeshare.models.Coordinate
import com.gustavclausen.bikeshare.models.UserDB
import com.gustavclausen.bikeshare.services.FetchAddressIntentService
import com.gustavclausen.bikeshare.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_register_bike.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterBikeFragment : Fragment() {

    private val bikesDB = BikeDB.get()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback

    private var mCurrentBikePhotoPath: String? = null
    private var mLockId: UUID? = null
    private var mSelectedBikeType: Int = 0
    private var mLocationPermissionDenied: Boolean = false
    private var mLocation: Coordinate? = null
    private var mLocationAddress: String? = null

    companion object {
        private const val TAG = "RegisterBikeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_LOCATION_PERMISSION_CODE = 1

        private const val SAVED_THUMBNAIL_BIKE_PHOTO_PATH = "thumbnailBikePhotoPath"
        private const val SAVED_LOCK_ID = "lockId"
        private const val SAVED_SELECTED_BIKE_TYPE = "selectedBikeType"
        private const val SAVED_LOCATION = "location"
        private const val SAVED_LOCATION_ADDRESS = "locationAddress"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            mCurrentBikePhotoPath = savedInstanceState.getString(SAVED_THUMBNAIL_BIKE_PHOTO_PATH)
            mLockId = savedInstanceState.getSerializable(SAVED_LOCK_ID) as UUID?
            mSelectedBikeType = savedInstanceState.getInt(SAVED_SELECTED_BIKE_TYPE)
            mLocation = savedInstanceState.getSerializable(SAVED_LOCATION) as Coordinate?
            mLocationAddress = savedInstanceState.getString(SAVED_LOCATION_ADDRESS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        enableLocation()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                /*
                 * Display dialog with error message if location service does not return location.
                 * Quits Activity afterwards.
                 */
                if (locationResult == null) {
                    InfoDialog.newInstance(
                        dialogText = getString(R.string.location_service_error),
                        finishActivity = true,
                        finishActivityToastText = getString(R.string.location_service_required_toast)
                    )
                }

                val location = locationResult!!.lastLocation
                mLocation = Coordinate(location.latitude, location.longitude)
                fetchLocationAddress(location)
            }
        }

        return inflater.inflate(R.layout.fragment_register_bike, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bike_type_spinner.emptyView = bike_type_empty_view
        setBikeTypesSpinner()

        bike_photo_button.setOnClickListener {
            dispatchTakePictureIntent()
        }
        setBikeThumbnail()

        register_lock_id_button.setOnClickListener {
            registerLockId()
        }
        setLockId()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.fragment_register_bike, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SAVED_THUMBNAIL_BIKE_PHOTO_PATH, mCurrentBikePhotoPath)
        outState.putSerializable(SAVED_LOCK_ID, mLockId)
        outState.putInt(SAVED_SELECTED_BIKE_TYPE, bike_type_spinner.selectedItemPosition)
        outState.putSerializable(SAVED_LOCATION, mLocation)
        outState.putString(SAVED_LOCATION_ADDRESS, mLocationAddress)
    }

    override fun onResume() {
        super.onResume()

        if (mLocationPermissionDenied) {
            // Location permission was not granted, display error dialog
            InfoDialog.newInstance(
                dialogText = getString(R.string.location_permission_denied),
                finishActivity = true,
                finishActivityToastText = getString(R.string.permission_required_toast)
            ).show(childFragmentManager, "dialog")
        } else {
            getLocation()
        }
    }

    override fun onPause() {
        super.onPause()

        // Stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.register_bike_button -> {
                submitForm()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            setBikeThumbnail()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_LOCATION_PERMISSION_CODE) return

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, ACCESS_FINE_LOCATION))
            enableLocation()
        else
            // Set variable to display the missing permission error dialog when this fragment resumes (see onResume)
            mLocationPermissionDenied = true
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // Permission to access the location is missing
            PermissionUtils.requestPermission(
                permission = ACCESS_FINE_LOCATION,
                requestId = REQUEST_LOCATION_PERMISSION_CODE,
                rationaleText = getString(R.string.permission_rationale_location),
                finishActivity = true,
                dismissText = getString(R.string.permission_required_toast),
                fragment = this
            )
        else
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1

        // Check whether location settings on device are satisfied
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(context!!)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener {
            // Location service is enabled and can be used
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null)
        }.addOnFailureListener {
            // Location service is not available, show error dialog and quit Activity afterwards
            InfoDialog.newInstance(
                dialogText = getString(R.string.location_service_error),
                finishActivity = true,
                finishActivityToastText = getString(R.string.location_service_required_toast)
            ).show(childFragmentManager, "dialog")
        }
    }

    private fun registerLockId() {
        mLockId = UUID.randomUUID() // Mocks Bluetooth pairing
        setLockId()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there is an camera app in the OS that can handle the intent
            val cameraComponent = takePictureIntent.resolveActivity(context!!.packageManager)

            if (cameraComponent == null) {
                makeToast(R.string.camera_intent_error_message)
                return@dispatchTakePictureIntent
            }

            cameraComponent.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e(TAG, "Error occurred while creating image file", ex)
                    null
                }

                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        context!!,
                        "com.gustavclausen.bikeshare.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun setBikeTypesSpinner() {
        ReadBikeTypes(
            { bikesDB.getBikeTypes(context!!).toList() },
            { bikeTypes ->
                bike_type_spinner.adapter = ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_spinner_item,
                    bikeTypes
                )
                bike_type_spinner.setSelection(mSelectedBikeType)
            }
        ).execute()
    }

    private fun setBikeThumbnail() {
        if (mCurrentBikePhotoPath != null)
            Glide.with(this).load(mCurrentBikePhotoPath).centerCrop().into(bike_photo_button)
    }

    private fun setLockId() {
        if (mLockId != null) {
            lock_id_text.text = mLockId.toString()
            lock_id_text.visibility = VISIBLE

            register_lock_id_button.isEnabled = false
        }
    }

    private fun submitForm() {
        when {
            mLockId == null -> {
                makeToast(R.string.no_lock_id_error_message)
                return
            }
            price_input.text.isNullOrBlank() -> {
                makeToast(R.string.no_price_specified_error_message)
                return
            }
            mLocation == null -> {
                makeToast(R.string.location_not_read_message)
                return
            }
        }

        val userPreferences = context!!.getSharedPreferences(BikeShareApplication.PREF_USER_FILE, Context.MODE_PRIVATE)
        val registeredUserId = userPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

        // Save bike to DB
        BikeDB.get().addBike(
            lockId = mLockId.toString(),
            type = bike_type_spinner.selectedItem.toString(),
            priceHour = price_input.text.toString().toInt(),
            picture = getCompressedBikePhoto(),
            owner = UserDB.get().getUser(registeredUserId)!!,
            lastKnownPosition = mLocation!!,
            locationAddress = mLocationAddress!!
        )

        activity?.finish() // Close activity after submission
    }

    private fun getCompressedBikePhoto(): ByteArray? {
        if (mCurrentBikePhotoPath == null) return null

        val bitmap = BitmapFactory.decodeFile(mCurrentBikePhotoPath)
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
            return stream.toByteArray()
        }
    }

    private fun makeToast(stringResourceId: Int) {
        Toast.makeText(context!!, getString(stringResourceId), Toast.LENGTH_SHORT).show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timestamp}_", // Prefix of file name
            ".jpg", // Suffix of file name
            storageDir
        ).apply {
            mCurrentBikePhotoPath = absolutePath
        }
    }

    private fun fetchLocationAddress(location: Location) {
        val intent = Intent(context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.RECEIVER, AddressResultReceiver())
            putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
        }

        context?.startService(intent)
    }

    private class ReadBikeTypes internal constructor(
        val handler: () -> List<String>,
        val postExecution: (List<String>) -> Unit
    ) : AsyncTask<Void, Void, List<String>>() {

        override fun doInBackground(vararg p0: Void?): List<String> {
            return handler()
        }

        override fun onPostExecute(result: List<String>) {
            postExecution(result)
        }
    }


    internal inner class AddressResultReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            val resultMessage = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY) ?: ""

            if (resultCode == FetchAddressIntentService.Constants.FAILURE_RESULT) {
                Toast.makeText(context!!, resultMessage, Toast.LENGTH_SHORT).show()
            } else if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mLocationAddress = resultMessage
            }
        }
    }
}