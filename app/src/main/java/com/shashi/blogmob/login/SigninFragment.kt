package com.shashi.blogmob.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.shashi.blogmob.MainActivity
import com.shashi.blogmob.R


class SigninFragment : Fragment() {

    private lateinit var progressBar: ProgressBar

    lateinit var buttonSignin: Button
    lateinit var textInputLayoutEmail: TextInputLayout
    lateinit var textInputLayoutPassword: TextInputLayout

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_signin, container, false)
        initViews(view)
        firebaseAuth = FirebaseAuth.getInstance()

        return view
    }

    private fun initViews(view: View) {

        progressBar = view.findViewById(R.id.progressBarSignin)

        buttonSignin = view.findViewById(R.id.button_signin_fragment)
        textInputLayoutEmail = view.findViewById(R.id.text_input_layout_email_sign_in)
        textInputLayoutPassword = view.findViewById(R.id.text_input_layout_password_sign_in)

        buttonSignin.setOnClickListener { signinClicked() }
    }

    private fun signinClicked() {

        progressBar.visibility = View.VISIBLE

        val userEmail: String = textInputLayoutEmail.editText?.text.toString()
        val userPassword: String = textInputLayoutPassword.editText?.text.toString()

        if (!isValid(userEmail, userPassword)) {
            progressBar.visibility = View.GONE
            return
        }

        loginUser(userEmail, userPassword)

    }

    private fun loginUser(userEmail: String, userPass: String) {

        firebaseAuth.signInWithEmailAndPassword(userEmail, userPass)
            .addOnSuccessListener {

                if (firebaseAuth.currentUser!!.isEmailVerified) {
                    checkIfDataAvaiable()
                } else {

                    progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Verify your email to sign-in", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE

                if (e is FirebaseAuthInvalidCredentialsException) {
                    textInputLayoutPassword.error = "Invalid Password"
                } else {
                    textInputLayoutPassword.error = null
                }
                if (e is FirebaseAuthInvalidUserException) {
                    textInputLayoutEmail.error = "Email not in use"
                } else {
                    textInputLayoutEmail.error = null
                }
            }
    }

    private fun checkIfDataAvaiable() {
        val userId = firebaseAuth.currentUser!!.uid
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot -> //Check if the document exists
                if (documentSnapshot.exists()) {

                    progressBar.visibility = View.GONE
                    startActivity(Intent(activity, MainActivity::class.java))
                    activity?.finish()

                } else {

                    progressBar.visibility = View.GONE
                    startActivity(Intent(activity, ProfileActivity::class.java))
                    activity?.finish()

                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE

                Toast.makeText(
                    activity,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun isValid(userEmail: String, userPass: String): Boolean {

        if (userEmail.isEmpty()) {
            textInputLayoutEmail.error = "Cannot be empty"
            return false
        } else if (!isValidEmail(userEmail)) {
            textInputLayoutEmail.error = "Invalid email"
            return false
        } else {
            textInputLayoutEmail.error = null
        }

        if (userPass.isEmpty()) {
            textInputLayoutPassword.error = "Cannot be empty"
            return false
        } else if (userPass.length <= 6) {
            textInputLayoutPassword.error = "Password too short"
            return false
        } else {
            textInputLayoutPassword.error = null
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}