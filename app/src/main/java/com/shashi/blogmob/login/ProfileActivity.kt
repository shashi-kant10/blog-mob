package com.shashi.blogmob.login

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.shashi.blogmob.R
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.shashi.blogmob.MainActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.InputStream


class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var userId: String
    private val COLLECTION_NAME = "users"

    private lateinit var buttonSave: Button
    private lateinit var textInputLayoutName: TextInputLayout

    private lateinit var circleImageView: CircleImageView
    private var profileImageUri: Uri = Uri.EMPTY
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()

        firebaseFirestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid!!

    }

    private fun initViews() {
        buttonSave = findViewById(R.id.button_save_profile)
        textInputLayoutName = findViewById(R.id.text_input_layout_display_name_profile)

        circleImageView = findViewById(R.id.display_image_profile)

        circleImageView.setOnClickListener { circleImageViewClicked() }
        buttonSave.setOnClickListener { saveClicked() }
    }

    private fun saveClicked() {

        val userName = textInputLayoutName.editText?.text.toString()
        if (!isNameValid(userName)) {
            return
        }

        uploadImageInStorage(userName)

    }

    private fun uploadImageInStorage(userName: String) {

        //Upload image in FirebaseStorage
        val firebaseStorage = FirebaseStorage.getInstance()
        val uploader = firebaseStorage.reference.child("profile_pictures").child("$userId.jpg")

        uploader.putFile(profileImageUri)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    uploader
                        .downloadUrl
                        .addOnSuccessListener {
                            saveDataInFirestore(userId, userName, it.toString())
                        }

                } else {
                    Toast.makeText(this, "Could not upload image", Toast.LENGTH_SHORT).show()
                    isProfileUpdateSuccessfull(false)
                }
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
                Toast.makeText(this, "Could not update name", Toast.LENGTH_SHORT).show()
                isProfileUpdateSuccessfull(false)
            }

    }

    private fun isProfileUpdateSuccessfull(isSuccessful: Boolean) {

        if (isSuccessful) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun circleImageViewClicked() {
        permissionCheck()
    }

    private fun permissionCheck() {

        Dexter.withContext(this)
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
            .start(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {

                profileImageUri = result.uri
                val inputStream: InputStream =
                    contentResolver.openInputStream(profileImageUri)!!
                bitmap = BitmapFactory.decodeStream(inputStream)
                circleImageView.setImageBitmap(bitmap)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(
                    this,
                    "Something went wrong while loading image",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    private fun isNameValid(userName: String): Boolean {
        if (userName.isEmpty()) {
            textInputLayoutName.error = "Cannot be empty"
            return false
        } else {
            textInputLayoutName.error = null
        }

        if (profileImageUri == Uri.EMPTY) {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}