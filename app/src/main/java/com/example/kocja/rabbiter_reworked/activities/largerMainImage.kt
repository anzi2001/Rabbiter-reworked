package com.example.kocja.rabbiter_reworked.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import kotlinx.android.synthetic.main.activity_larger_main_image.*

/**
 * Created by kocja on 26/03/2018.
 */

class largerMainImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_larger_main_image)
        largeImageView.transitionName = "closerLook"
        val imageUri = intent.getStringExtra("imageURI")
        Glide.with(this).load(imageUri).into(largeImageView)
        val layout = findViewById<ConstraintLayout>(R.id.constraint)
        layout.setOnClickListener { supportFinishAfterTransition() }
    }
}
