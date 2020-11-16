package com.shashi.blogmob.others

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shashi.blogmob.R

class AboutActivity : AppCompatActivity() {

    private lateinit var linearLayoutAbout: LinearLayout
    private lateinit var constraintLayoutAbout: ConstraintLayout
    private lateinit var cardViewAbout: CardView
    private lateinit var imageViewArrowAbout: ImageView

    private lateinit var linearLayoutTerms: LinearLayout
    private lateinit var constraintLayoutTerms: ConstraintLayout
    private lateinit var cardViewTerms: CardView
    private lateinit var imageViewArrowTerms: ImageView

    private lateinit var linearLayoutPrivacy: LinearLayout
    private lateinit var constraintLayoutPrivacy: ConstraintLayout
    private lateinit var cardViewPrivacy: CardView
    private lateinit var imageViewArrowPrivacy: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setStatusBarColor()
        initViews()

    }

    private fun setStatusBarColor() {
        val window: Window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.faded_blue)
    }

    private fun initViews() {

        linearLayoutAbout = findViewById(R.id.linear_layout_about)
        constraintLayoutAbout = findViewById(R.id.constraint_layout_about)
        cardViewAbout = findViewById(R.id.card_view_about)
        imageViewArrowAbout = findViewById(R.id.image_view_arrow_about)

        linearLayoutTerms = findViewById(R.id.linear_layout_terms)
        constraintLayoutTerms = findViewById(R.id.constraint_layout_terms)
        cardViewTerms = findViewById(R.id.card_view_terms)
        imageViewArrowTerms = findViewById(R.id.image_view_arrow_terms)

        linearLayoutPrivacy = findViewById(R.id.linear_layout_privacy)
        constraintLayoutPrivacy = findViewById(R.id.constraint_layout_privacy)
        cardViewPrivacy = findViewById(R.id.card_view_privacy)
        imageViewArrowPrivacy = findViewById(R.id.image_view_arrow_privacy)

        constraintLayoutAbout.setOnClickListener { aboutClicked() }
        constraintLayoutTerms.setOnClickListener { termsClicked() }
        constraintLayoutPrivacy.setOnClickListener { privacyClicked() }

    }

    private fun aboutClicked() {

        if (linearLayoutAbout.visibility == View.GONE) {
            imageViewArrowAbout.setImageResource(R.drawable.icon_arrow_up)
            TransitionManager.beginDelayedTransition(cardViewAbout, AutoTransition())
            linearLayoutAbout.visibility = View.VISIBLE
        } else {
            imageViewArrowAbout.setImageResource(R.drawable.icon_arrow_down)
            TransitionManager.beginDelayedTransition(cardViewAbout, AutoTransition())
            linearLayoutAbout.visibility = View.GONE
        }

    }

    private fun termsClicked() {

        if (linearLayoutTerms.visibility == View.GONE) {
            imageViewArrowTerms.setImageResource(R.drawable.icon_arrow_up)
            TransitionManager.beginDelayedTransition(cardViewTerms, AutoTransition())
            linearLayoutTerms.visibility = View.VISIBLE
        } else {
            imageViewArrowTerms.setImageResource(R.drawable.icon_arrow_down)
            TransitionManager.beginDelayedTransition(cardViewTerms, AutoTransition())
            linearLayoutTerms.visibility = View.GONE
        }

    }

    private fun privacyClicked() {

        if (linearLayoutPrivacy.visibility == View.GONE) {
            imageViewArrowPrivacy.setImageResource(R.drawable.icon_arrow_up)
            TransitionManager.beginDelayedTransition(cardViewPrivacy, AutoTransition())
            linearLayoutPrivacy.visibility = View.VISIBLE
        } else {
            imageViewArrowPrivacy.setImageResource(R.drawable.icon_arrow_down)
            TransitionManager.beginDelayedTransition(cardViewPrivacy, AutoTransition())
            linearLayoutPrivacy.visibility = View.GONE
        }

    }

}