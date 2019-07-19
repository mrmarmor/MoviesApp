package com.appstairs.movies.Splash.View.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.appstairs.movies.R;
import com.appstairs.movies.Main.Controller.MainController;
import com.appstairs.movies.Main.Model.MovieModel;
import com.appstairs.movies.Main.View.activities.MainActivity;
import com.appstairs.movies.Splash.Controller.SplashSqlController;

import java.util.ArrayList;
import java.util.List;

import static com.appstairs.movies.Main.Model.iMovie.MOVIES_DB;
import static com.appstairs.movies.Main.Model.iMovie.TABLE_NAME;

public class SplashActivity extends Activity implements MainController.DataListener {

    public static final String MOVIES_KEY = "movies key";
    private final String TAG = SplashActivity.class.getSimpleName();
    private final long MIN_SPLASH_DELAY = 2000;

    private SplashSqlController splashSqlController = SplashSqlController.getInstance();
    private SQLiteDatabase moviesDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.splashTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        moviesDB = openOrCreateDatabase(MOVIES_DB, MODE_PRIVATE, null);

        if (splashSqlController.isDbExists(moviesDB, TABLE_NAME)) {
            List<MovieModel> movies = splashSqlController.parseSqlLite(new ArrayList<MovieModel>());
            continueFlow(movies);

        } else {
            splashSqlController.setUrl(getString(R.string.movies_url));
            splashSqlController.retrieveData(this);
        }
    }

    private void continueFlow(List<MovieModel> movies) {
        Log.d(TAG, "continueFlow");
        Intent start = new Intent(SplashActivity.this, MainActivity.class);
        start.putExtra(MOVIES_KEY, (ArrayList<MovieModel>)movies);

        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(start);
        finish();
    }

    @Override
    public void onFinish(final List<MovieModel> movies) {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                continueFlow(movies);
            }
        }, MIN_SPLASH_DELAY);

        MainController.getInstance().saveOnSqlLite(moviesDB, movies, true);
    }
}