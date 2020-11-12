package com.shashi.blogmob.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shashi.blogmob.MainActivity
import com.shashi.blogmob.R


class SplashScreenActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //Hide status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val hander = Handler()
        hander.postDelayed({
            startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            finish()
        }, 2500)

        initViews()
        setAnimation()

    }

    private fun initViews() {
        imageView = findViewById(R.id.imageViewSplash)
        textView = findViewById(R.id.textViewSplash)
    }

    private fun setAnimation() {
        val animationImageView: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.image_view_fade_splash)
        val animationTextView: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.text_view_fade_splash)

        imageView.animation = animationImageView
        textView.animation = animationTextView
    }

}