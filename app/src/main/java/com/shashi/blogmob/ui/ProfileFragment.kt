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
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
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
import de.hdodenhof.circleimageview.CircleImageView
import java.io.InputStream

class ProfileFragment : Fragment() {

    lateinit var firebaseFirestore: FirebaseFirestore
    private val COLLECTION_NAME = "users"

    lateinit var circleImageView: CircleImageView

    lateinit var textInputLayoutName: TextInputLayout
    lateinit var previousImageUri: Uri
    lateinit var updatedImageUri: Uri
    private lateinit var bitmap: Bitmap

    lateinit var mActivity: Activity

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        initViews(view)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        checkIfDataAvaiable()

        return view

    }

    private fun initViews(view: View) {

        mActivity = activity!!

        textInputLayoutName = view.findViewById(R.id.text_input_layout_display_name_userprofile)

        circleImageView = view.findViewById(R.id.display_image_userprofile)

        val buttonUpdate = view.findViewById<Button>(R.id.button_update_userprofile)

        buttonUpdate.setOnClickListener { updateData() }
        circleImageView.setOnClickListener { circleImageViewClicked() }

    }

    private fun checkIfDataAvaiable() {

        val userId = getUserID()

        firebaseFirestore.collection(COLLECTION_NAME)
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot -> //Check if the document exists
                if (documentSnapshot.exists()) {

                    var userName = documentSnapshot.getString("name")
                    var imageUrl = documentSnapshot.getString("image")

                    if (userName!!.isEmpty()) {
                        userName = ""
                    }
                    if (imageUrl!!.isEmpty()) {
                        imageUrl = ""
                    }

                    showData(userName, imageUrl)

                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    mActivity,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private fun showData(userName: String, imageUrl: String) {

        previousImageUri = Uri.parse(imageUrl)
        updatedImageUri = Uri.parse(imageUrl)

        textInputLayoutName.editText!!.setText(userName)

        val placeolderRequest = RequestOptions()
        placeolderRequest.placeholder(R.drawable.icon_profile)

        Glide.with(this)
            .setDefaultRequestOptions(placeolderRequest)
            .load(imageUrl)
            .into(circleImageView)
    }

    private fun updateData() {

        val userName = textInputLayoutName.editText?.text.toString()

        if (!isDataValid(userName)) {
            return
        }

        if (previousImageUri == updatedImageUri) {
            saveNameInFirestore(getUserID(), userName)
        } else {
            uploadImageInStorage(userName, getUserID())
        }
    }

    private fun uploadImageInStorage(userName: String, userId: String) {

        //Upload image in FirebaseStorage
        val firebaseStorage = FirebaseStorage.getInstance()
        val uploader = firebaseStorage.reference.child("profile_pictures").child("$userId.jpg")

        uploader.putFile(updatedImageUri)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    uploader
                        .downloadUrl
                        .addOnSuccessListener {
                            saveDataInFirestore(userId, userName, it.toString())
                        }

                } else {
                    Toast.makeText(mActivity, "Could not upload image", Toast.LENGTH_SHORT).show()
                    isProfileUpdateSuccessfull(false)
                }
            }

    }

    private fun saveNameInFirestore(userId: String, userName: String) {

        //Update name in Firestore
        val documentReference = firebaseFirestore
            .collection(COLLECTION_NAME)
            .document(userId)

        val userData: MutableMap<String, Any> = HashMap()
        userData["name"] = userName

        documentReference.update(userData)
            .addOnSuccessListener {
                isProfileUpdateSuccessfull(true)
            }
            .addOnFailureListener {
                Toast.makeText(mActivity, "Could not update name", Toast.LENGTH_SHORT).show()
                isProfileUpdateSuccessfull(false)
            }

    }

    private fun saveDataInFirestore(userId: String, userName: String, uploadedImageUri: String) {

        //Update name in Firestore
        val documentReference = firebaseFirestore
            .collection(COLLECTION_NAME)
            .document(userId)

        val userData: MutableMap<String, Any> = HashMap()
        userData["name"] = userName
        userData["image"] = uploadedImageUri

        documentReference.set(userData)
            .addOnSuccessListener {
                isProfileUpdateSuccessfull(true)
            }
            .addOnFailureListener {
                Toast.makeText(mActivity, "Could not update name", Toast.LENGTH_SHORT).show()
                isProfileUpdateSuccessfull(false)
            }

    }

    private fun isProfileUpdateSuccessfull(isSuccessful: Boolean) {

        if (isSuccessful) {
            Toast.makeText(mActivity, "Updated", Toast.LENGTH_SHORT).show()
        }

    }

    private fun isDataValid(userName: String): Boolean {
        if (userName.isEmpty()) {
            textInputLayoutName.error = "Cannot be empty"
            return false
        } else {
            textInputLayoutName.error = null
        }

        if (previousImageUri == Uri.EMPTY) {
            Toast.makeText(mActivity, "Image not selected", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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
            .setAspectRatio(1, 1)
            .start(mActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == AppCompatActivity.RESULT_OK) {

                updatedImageUri = result.uri
                val inputStream: InputStream =
                    mActivity.contentResolver.openInputStream(updatedImageUri)!!
                bitmap = BitmapFactory.decodeStream(inputStream)
                circleImageView.setImageBitmap(bitmap)

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


}