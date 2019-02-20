package com.example.kocja.rabbiter_reworked.activities

import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import com.github.chrisbanes.photoview.PhotoView

/**
 * Created by kocja on 26/03/2018.
 */

class largerMainImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_larger_main_image)
        val largerMainView = findViewById<PhotoView>(R.id.largeImageView)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            largerMainView.transitionName = "closerLook"
        }
        val imageUri = intent.getStringExtra("imageURI")
        Glide.with(this).load(imageUri).into(largerMainView)
        val layout = findViewById<ConstraintLayout>(R.id.constraint)
        layout.setOnClickListener { supportFinishAfterTransition() }
    }
}
