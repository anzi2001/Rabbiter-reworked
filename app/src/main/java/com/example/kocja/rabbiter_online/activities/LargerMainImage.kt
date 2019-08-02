package com.example.kocja.rabbiter_online.activities

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.github.chrisbanes.photoview.PhotoView

/**
 * Created by kocja on 26/03/2018.
 */

class LargerMainImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_larger_main_image)
        val largerMainView = findViewById<PhotoView>(R.id.largeImageView)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            largerMainView.transitionName = "closerLook"
        }
        val imageUri = intent.getStringExtra("imageURI")
        HttpManager.postRequest("searchForImage", imageUri) { _, bytes -> Glide.with(this).load(BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)).into(largerMainView) }

        val layout = findViewById<ConstraintLayout>(R.id.constraint)
        layout.setOnClickListener {supportFinishAfterTransition() }
    }
}
