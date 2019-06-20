package com.appstairs.movies;

import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements MainFragment.MainFragmentListener{
    private MainFragment mainFragment;
    private boolean isQrScanMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Movie> movies = (ArrayList<Movie>)getIntent().getSerializableExtra(SplashActivity.MOVIES_KEY);

        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.movie_fragment);
        mainFragment.setViews(movies);
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
    public void onFragmentComplete(Movie movie) {
        mainFragment.updateViews(movie);
    }

    @Override
    public void onSurfaceViewModeChange(boolean isQrScanMode) {
        this.isQrScanMode = isQrScanMode;
    }
}