package com.shashi.blogmob.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.shashi.blogmob.R

class SignupFragment : Fragment() {

    lateinit var buttonSignin: Button
    lateinit var textInputLayoutEmail: TextInputLayout
    lateinit var textInputLayoutPassword: TextInputLayout
    lateinit var textInputLayoutConfirmPassword: TextInputLayout

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_signup, container, false)
        initViews(view)
        firebaseAuth = FirebaseAuth.getInstance()

        return view
    }

    private fun initViews(view: View) {
        buttonSignin = view.findViewById(R.id.button_signup_fragment)
        textInputLayoutEmail = view.findViewById(R.id.text_input_layout_email_sign_up)
        textInputLayoutPassword = view.findViewById(R.id.text_input_layout_password_sign_up)
        textInputLayoutConfirmPassword =
            view.findViewById(R.id.text_input_layout_confirm_password_sign_up)

        buttonSignin.setOnClickListener { signupClicked() }
    }

    private fun signupClicked() {

        val userEmail: String = textInputLayoutEmail.editText?.text.toString()
        val userPassword: String = textInputLayoutPassword.editText?.text.toString()
        val userPasswordConfirm = textInputLayoutConfirmPassword.editText?.text.toString()

        if (!isValid(userEmail, userPassword, userPasswordConfirm)) {
            return
        }

        registerUser(userEmail, userPassword)

    }

    private fun registerUser(userEmail: String, userPass: String) {

        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPass)
            .addOnCompleteListener(
                activity!!
            ) { task ->
                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser

                    user!!.sendEmailVerification()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    activity,
                                    "An email verification link is sent to you",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    activity,
                                    "Could not send email verification",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthUserCollisionException) {
                    textInputLayoutEmail.error = "Email already in use"
                } else {
                    textInputLayoutEmail.error = null
                }
            }

        firebaseAuth.signOut()
    }

    private fun isValid(userEmail: String, userPass: String, userPasswordConfirm: String): Boolean {

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
        } else if (userPass.length < 6) {
            textInputLayoutPassword.error = "Password too short"
            return false
        } else {
            textInputLayoutPassword.error = null
        }

        if (userPasswordConfirm.isEmpty()) {
            textInputLayoutConfirmPassword.error = "Cannot be empty"
            return false
        } else if (userPass.length <= 6) {
            textInputLayoutPassword.error = "Password too short"
            return false
        } else {
            textInputLayoutConfirmPassword.error = null
        }

        if (!userPass.equals(userPasswordConfirm)) {
            textInputLayoutConfirmPassword.error = "Passwords does not match"
            return false
        } else {
            textInputLayoutConfirmPassword.error = null
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}