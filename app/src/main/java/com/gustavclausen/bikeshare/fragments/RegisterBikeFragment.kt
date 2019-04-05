package com.gustavclausen.bikeshare.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.gustavclausen.bikeshare.R
import kotlinx.android.synthetic.main.fragment_register_bike.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterBikeFragment : Fragment() {

    private var mCurrentBikePhotoPath: String? = null

    companion object {
        private const val TAG = "RegisterBikeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val THUMBNAIL_BIKE_PHOTO_PATH = "thumbnailBikePhotoPath"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            mCurrentBikePhotoPath = savedInstanceState.getString(THUMBNAIL_BIKE_PHOTO_PATH)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_bike, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bike_photo_button.setOnClickListener {
            dispatchTakePictureIntent()
        }

        setBikeThumbnail()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.fragment_register_bike, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(THUMBNAIL_BIKE_PHOTO_PATH, mCurrentBikePhotoPath)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.register_bike_button -> {
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            setBikeThumbnail()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there is an camera app in the OS that can handle the intent
            val cameraComponent = takePictureIntent.resolveActivity(context!!.packageManager)

            if (cameraComponent == null) {
                Toast.makeText(context!!, getString(R.string.camera_intent_error_message), Toast.LENGTH_SHORT).show()
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

    private fun setBikeThumbnail() {
        if (mCurrentBikePhotoPath != null)
            Glide.with(this).load(mCurrentBikePhotoPath).centerCrop().into(bike_photo_button)
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
}