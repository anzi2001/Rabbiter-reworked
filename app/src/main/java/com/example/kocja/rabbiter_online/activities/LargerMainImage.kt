package com.example.kocja.rabbiter_online.activities

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import coil.api.load

import com.example.kocja.rabbiter_online.R
import kotlinx.android.synthetic.main.activity_larger_main_image.*

/**
 * Created by kocja on 26/03/2018.
 */

class LargerMainImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_larger_main_image)
        largeImageView.transitionName = "closerLook"
        val imageURL = intent.getStringExtra("imageURL")
        largeImageView.load(imageURL)

        constraint.setOnClickListener {supportFinishAfterTransition() }
    }
}
