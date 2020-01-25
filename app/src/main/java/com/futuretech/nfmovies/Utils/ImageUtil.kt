package com.futuretech.nfmovies.Utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.futuretech.nfmovies.R

object ImageUtil {
    private const val PLACEHOLDER = R.drawable.placeholder

    private const val BITMAP_SCALE = 0.4f
    private const val BLUR_RADIUS = 7.5f

    fun display(context: Context, url: String?, view: ImageView, transformation: Transformation<Bitmap>?) {
        if (url == null || url.isEmpty()) {
            Glide.with(context).load(PLACEHOLDER).into(view)
            return
        }

        val gu = GlideUrl(url, LazyHeaders.Builder().addHeader("Referer", url).build())
        if (transformation == null) {
            Glide.with(context).load(gu)
                    .placeholder(PLACEHOLDER).into(view)
        } else {
            Glide.with(context).load(gu)
                    .placeholder(PLACEHOLDER)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .into(view)
        }
    }
}
