package com.example.kocja.rabbiter_reworked.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.R;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by kocja on 26/03/2018.
 */

public class largerMainImage extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_larger_main_image);
        PhotoView largerMainView = findViewById(R.id.largeImageView);
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP){
            largerMainView.setTransitionName("closerLook");
        }
        String imageUri = getIntent().getStringExtra("imageURI");
        Glide.with(this).load(imageUri).into(largerMainView);
        ConstraintLayout layout = findViewById(R.id.constraint);
        layout.setOnClickListener(view -> supportFinishAfterTransition());
    }
}
