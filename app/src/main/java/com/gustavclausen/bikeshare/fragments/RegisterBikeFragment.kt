package com.gustavclausen.bikeshare.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.gustavclausen.bikeshare.BikeShareApplication
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.models.BikeDB
import com.gustavclausen.bikeshare.models.Coordinate
import com.gustavclausen.bikeshare.models.UserDB
import kotlinx.android.synthetic.main.fragment_register_bike.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterBikeFragment : Fragment() {

    private val bikesDB = BikeDB.get()

    private var mCurrentBikePhotoPath: String? = null
    private var mLockId: UUID? = null
    private var mSelectedBikeType: Int = 0

    companion object {
        private const val TAG = "RegisterBikeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1

        private const val THUMBNAIL_BIKE_PHOTO_PATH = "thumbnailBikePhotoPath"
        private const val LOCK_ID = "lockId"
        private const val SELECTED_BIKE_TYPE = "selectedBikeType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            mCurrentBikePhotoPath = savedInstanceState.getString(THUMBNAIL_BIKE_PHOTO_PATH)
            mLockId = savedInstanceState.getSerializable(LOCK_ID) as UUID?
            mSelectedBikeType = savedInstanceState.getInt(SELECTED_BIKE_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_bike, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bike_type_spinner.emptyView = bike_type_empty_view

        bike_photo_button.setOnClickListener {
            dispatchTakePictureIntent()
        }

        register_lock_id_button.setOnClickListener {
            registerLockId()
        }


        setBikeThumbnail()
        setLockId()
        setBikeTypesSpinner()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.fragment_register_bike, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(THUMBNAIL_BIKE_PHOTO_PATH, mCurrentBikePhotoPath)
        outState.putSerializable(LOCK_ID, mLockId)
        outState.putInt(SELECTED_BIKE_TYPE, bike_type_spinner.selectedItemPosition)
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
        }

        val userPreferences = context!!.getSharedPreferences(BikeShareApplication.PREF_USER_FILE, Context.MODE_PRIVATE)
        val registeredUserId = userPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

        // Save bike to DB
        BikeDB.get().addBike(
            lockId = mLockId.toString(),
            type = bike_type_spinner.selectedItem.toString(),
            priceHour = price_input.text.toString().toInt(),
            picture = File(mCurrentBikePhotoPath).readBytes(), // TODO: Compress image and delete image from local storage
            owner = UserDB.get().getUser(registeredUserId)!!,
            lastKnownPosition = Coordinate(0.0, 0.0)
        )

        activity!!.finish() // Close activity after submission
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
}