package com.appstairs.movies.Main.View.activities;

import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.FragmentActivity;

import com.appstairs.movies.R;
import com.appstairs.movies.Main.Model.MovieModel;
import com.appstairs.movies.Main.View.fragments.MainFragment;
import com.appstairs.movies.Splash.View.activities.SplashActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements MainFragment.MainFragmentListener{
    private MainFragment mainFragment;
    private boolean isQrScanMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<MovieModel> movieModels = (ArrayList<MovieModel>)getIntent().getSerializableExtra(SplashActivity.MOVIES_KEY);

        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.movie_fragment);
        mainFragment.setViews(movieModels);
    }

    @Override
    public void onBackPressed() {
        if (isQrScanMode) {
            findViewById(R.id.fabAddMovie).setVisibility(View.VISIBLE);
            findViewById(R.id.rv_movies).setVisibility(View.VISIBLE);
            findViewById(R.id.scanQr_surfaceView).setVisibility(View.GONE);
            isQrScanMode = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentComplete(MovieModel movieModel) {
        mainFragment.updateViews(movieModel);
    }

    @Override
    public void onSurfaceViewModeChange(boolean isQrScanMode) {
        this.isQrScanMode = isQrScanMode;
    }
}