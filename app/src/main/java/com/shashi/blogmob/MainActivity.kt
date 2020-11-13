package com.shashi.blogmob

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.shashi.blogmob.login.GoogleSigninActivity
import com.shashi.blogmob.login.LoginActivity
import com.shashi.blogmob.login.ProfileActivity
import com.shashi.blogmob.ui.HomeFragment
import com.shashi.blogmob.ui.NewPostFragment
import com.shashi.blogmob.ui.UserProfileFragment
import com.theartofdev.edmodo.cropper.CropImage


class MainActivity : AppCompatActivity() {

    lateinit var firebaseFirestore: FirebaseFirestore
    private val COLLECTION_NAME = "users"

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var authStateListener: AuthStateListener
    lateinit var bottomNavigation: MeowBottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        authStateListener = AuthStateListener {
            checkIfDataAvaiable()
        }

        setNavBar()

        checkIfEmailVerified()
    }

    private fun setNavBar() {
        bottomNavigation = findViewById(R.id.nav_view_main)
        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.ic_home))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.ic_new_post))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.ic_profile))

        bottomNavigation.show(1)
        addFragment()

        bottomNavigation.setOnClickMenuListener {
            replaceFragment(it)
        }
    }

    private fun replaceFragment(model: MeowBottomNavigation.Model) {

        var fragment: Fragment? = null

        when (model.id) {
            1 -> fragment = HomeFragment()
            2 -> fragment = NewPostFragment()
            3 -> fragment = UserProfileFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_main, fragment!!)
            .commit()

    }

    private fun addFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_main, HomeFragment())
            .commit()
    }

    private fun checkIfEmailVerified() {
        if (!FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            logout()
        }
    }

    private fun checkIfDataAvaiable() {
        val userId = getUserID()

        firebaseFirestore.collection(COLLECTION_NAME)
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot -> //Check if the document exists
                if (!documentSnapshot.exists()) {

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            onActivityRequestResult(
                requestCode,
                resultCode,
                data!!,
                "UserProfileFragment"
            )
        }
    }

    private fun onActivityRequestResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent,
        fragmentName: String
    ) {
        try {
            val fm: FragmentManager = supportFragmentManager
            if (fm.fragments.size > 0) {
                for (i in 0 until fm.fragments.size) {
                    val fragment: Fragment = fm.fragments[i]
                    if (fragment.javaClass.simpleName.equals(fragmentName, ignoreCase = true)) {
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}