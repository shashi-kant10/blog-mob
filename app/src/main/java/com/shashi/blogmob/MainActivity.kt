package com.shashi.blogmob

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.shashi.blogmob.login.GoogleSigninActivity
import com.shashi.blogmob.login.LoginActivity
import com.shashi.blogmob.login.ProfileActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    lateinit var firebaseFirestore: FirebaseFirestore
    private val COLLECTION_NAME = "users"

    lateinit var circleImageView: CircleImageView

    lateinit var textInputLayoutName: TextInputLayout
    lateinit var previousImageUri: Uri
    lateinit var updatedImageUri: Uri
    private lateinit var bitmap: Bitmap

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var authStateListener: AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        authStateListener = AuthStateListener {
            checkIfDataAvaiable()
        }

        checkIfEmailVerified()
    }

    private fun checkIfEmailVerified() {
        if (!FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            logout()
        }
    }

    private fun initViews() {

        textInputLayoutName = findViewById(R.id.text_input_layout_display_name_main)

        circleImageView = findViewById(R.id.display_image_main)

        val buttonUpdate = findViewById<Button>(R.id.button_update_main)
        val buttonLogout = findViewById<Button>(R.id.button_logout_main)

        buttonUpdate.setOnClickListener { updateData() }
        buttonLogout.setOnClickListener { logout() }
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

                } else {

                    for (user in FirebaseAuth.getInstance().currentUser!!.providerData) {
                        if (user.providerId == "password") {
                            startActivity(Intent(this, ProfileActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, GoogleSigninActivity::class.java))
                            finish()
                        }
                    }

                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
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
                    Toast.makeText(this, "Could not upload image", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Could not update name", Toast.LENGTH_SHORT).show()
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

    private fun isDataValid(userName: String): Boolean {
        if (userName.isEmpty()) {
            textInputLayoutName.error = "Cannot be empty"
            return false
        } else {
            textInputLayoutName.error = null
        }

        if (previousImageUri == Uri.EMPTY) {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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

                updatedImageUri = result.uri
                val inputStream: InputStream =
                    contentResolver.openInputStream(updatedImageUri)!!
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

    private fun logout() {

        FirebaseAuth.getInstance().signOut()

        // Google sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        startActivity(Intent(this, LoginActivity::class.java))
        finish()

    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

}