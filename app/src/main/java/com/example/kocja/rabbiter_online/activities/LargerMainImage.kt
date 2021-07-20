package com.example.kocja.rabbiter_online.activities

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import coil.load

import com.example.kocja.rabbiter_online.databinding.ActivityLargerMainImageBinding

/**
 * Created by kocja on 26/03/2018.
 */

class LargerMainImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityLargerMainImageBinding = ActivityLargerMainImageBinding.inflate(layoutInflater)
        setContentView(activityLargerMainImageBinding.root)
        activityLargerMainImageBinding.largeImageView.transitionName = "closerLook"
        val imageURL = intent.getStringExtra("imageURL")
        activityLargerMainImageBinding.largeImageView.load(imageURL)

        activityLargerMainImageBinding.constraint.setOnClickListener {supportFinishAfterTransition() }
    }
}
