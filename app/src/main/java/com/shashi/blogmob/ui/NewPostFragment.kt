package com.shashi.blogmob.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.shashi.blogmob.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.InputStream

class NewPostFragment : Fragment() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var userId: String
    private val COLLECTION_NAME = "blogs"

    private lateinit var progressBar: ProgressBar
    private lateinit var buttonSave: Button
    private lateinit var editTextDescription: EditText

    private lateinit var imageView: ImageView
    private var profileImageUri: Uri = Uri.EMPTY
    private lateinit var bitmap: Bitmap

    lateinit var mActivity: Activity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_new_post, container, false)

        firebaseFirestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid!!
        mActivity = activity!!

        initViews(view)

        return view

    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progress_bar_new_post)

        buttonSave = view.findViewById(R.id.button_post_new_post)
        editTextDescription = view.findViewById(R.id.edit_text_new_post)

        imageView = view.findViewById(R.id.image_view_new_post)

        imageView.setOnClickListener { circleImageViewClicked() }
        buttonSave.setOnClickListener { saveClicked() }
    }

    private fun saveClicked() {

        progressBar.visibility = View.VISIBLE

        val blogDescription = editTextDescription.text.toString()
        if (!isBlogValid(blogDescription)) {
            progressBar.visibility = View.GONE
            return
        }

        //To do -> Post the blog
        postBlog(blogDescription)
    }

    private fun postBlog(blogDescription: String) {

        //Upload image in FirebaseStorage
        val randomImageName: String = FieldValue.serverTimestamp().toString()

        val firebaseStorage = FirebaseStorage.getInstance()
        val uploader =
            firebaseStorage.reference.child("blog_pictures").child("$randomImageName.jpg")

        uploader.putFile(profileImageUri)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    uploader
                        .downloadUrl
                        .addOnSuccessListener {
                            saveDataInFirestore(blogDescription, it.toString(), randomImageName)
                        }

                } else {

                    progressBar.visibility = View.GONE
                    Toast.makeText(mActivity, "Could not upload image", Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun saveDataInFirestore(
        blogDescription: String,
        uploadedImageUri: String,
        randomImageName: String
    ) {

        //Update name in Firestore

        val blogData: MutableMap<String, Any> = HashMap()
        blogData["image_url"] = uploadedImageUri
        blogData["desc"] = blogDescription
        blogData["user_id"] = userId
        blogData["timestamp"] = randomImageName

        firebaseFirestore
            .collection(COLLECTION_NAME)
            .add(blogData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(mActivity, "Blog uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(mActivity, "Could not upload blog", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.GONE
            }

        progressBar.visibility = View.GONE

    }

    private fun circleImageViewClicked() {
        permissionCheck()
    }

    private fun permissionCheck() {

        Dexter.withContext(mActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        createIntent()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()

    }

    private fun createIntent() {

        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(2, 1)
            .start(mActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == AppCompatActivity.RESULT_OK) {

                profileImageUri = result.uri
                val inputStream: InputStream =
                    mActivity.contentResolver.openInputStream(profileImageUri)!!
                bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(
                    mActivity,
                    "Something went wrong while loading image",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    private fun isBlogValid(blogDescription: String): Boolean {
        if (blogDescription.isEmpty()) {
            editTextDescription.error = "Cannot be empty"
            return false
        }

        if (profileImageUri == Uri.EMPTY) {
            Toast.makeText(mActivity, "Image not selected", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}