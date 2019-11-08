package com.xuvjso.nfmovies.Utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.xuvjso.nfmovies.R;

public class ImageUtil {
    private final static int PLACEHOLDER = R.drawable.placeholder;

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;

    public static void display(Context context, String url, ImageView view, Transformation transformation) {
        if (url == null || url.isEmpty()) {
            Glide.with(context).load(R.drawable.placeholder).into(view);
            return;
        }

        GlideUrl gu = new GlideUrl(url, new LazyHeaders.Builder().addHeader("Referer", url).build());
        if (transformation == null) {
            Glide.with(context).load(gu)
                    .placeholder(R.drawable.placeholder).into(view);
        } else {
            Glide.with(context).load(gu)
                    .placeholder(R.drawable.placeholder)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .into(view);
        }
    }
}
