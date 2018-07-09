package com.example.kocja.rabbiter_online.activities;

import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
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
        HttpManager.postRequest("searchForImage", imageUri, (response, bytes) -> Glide.with(this).load(BitmapFactory.decodeByteArray(bytes,0,bytes.length)).into(largerMainView));

        ConstraintLayout layout = findViewById(R.id.constraint);
        layout.setOnClickListener(view -> supportFinishAfterTransition());
    }
}
