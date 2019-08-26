package com.example.kocja.rabbiter_online.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

fun Uri.getDownscaledBitmap(context : Context) : Bitmap? {
    var stream = context.contentResolver.openInputStream(this)
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(stream,null,options)

    stream = context.contentResolver.openInputStream(this)
    options.inJustDecodeBounds = false
    options.inSampleSize = calculateInSampleSize(options)
    return BitmapFactory.decodeStream(stream,null,options)
}

private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > 512 || width > 512) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= 512 && halfWidth / inSampleSize >= 512) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}