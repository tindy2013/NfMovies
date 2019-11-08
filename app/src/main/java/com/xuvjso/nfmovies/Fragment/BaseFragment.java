package com.xuvjso.nfmovies.Fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.xuvjso.nfmovies.Activity.MovieDetailActivity;
import com.xuvjso.nfmovies.Listener.MovieItemClickListener;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.R;

import me.yokeyword.fragmentation.SupportFragment;

public class BaseFragment extends SupportFragment implements MovieItemClickListener {

    @Override
    public void onMovieClick(Movie movie, ImageView img) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                getActivity(), img, "sharedImg"
        );
        startActivity(intent, options.toBundle());
    }
}
