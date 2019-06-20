package com.appstairs.movies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements DataController.DataListener {

    public static final String MOVIES_KEY = "movies key";
    private final String TAG = SplashActivity.class.getSimpleName();
    public static final String MOVIES_DB = "moviesDB", TABLE_NAME = "movies_table", TITLE = "title";
    public static final String IMAGE = "image", RATING = "rating", RELEASE_YEAR = "release_year", GENRE = "genre";
    private static final long MIN_SPLASH_DELAY = 2000;

    private SQLiteDatabase moviesDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.splashTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        moviesDB = openOrCreateDatabase(MOVIES_DB, MODE_PRIVATE, null);

        if (isDbExists(moviesDB, TABLE_NAME)) {
            List<Movie> movies = parseSqlLite(new ArrayList<Movie>());
            continueFlow(movies);

        } else {
            DataController dataController = DataController.getInstance();
            dataController.setUrl("https://api.androidhive.info/json/movies.json");
            dataController.retrieveData(this);
        }
    }

    private boolean isDbExists(SQLiteDatabase db, String table) {
        try {
            db.rawQuery("SELECT * FROM " + table, null);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private List<Movie> parseSqlLite(List<Movie> movies) {
        Cursor c = moviesDB.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        c.moveToFirst();

        do {
            String title = c.getString(c.getColumnIndex(TITLE));
            String image = c.getString(c.getColumnIndex(IMAGE));
            int rating = c.getInt(c.getColumnIndex(RATING));
            int releaseYear = c.getInt(c.getColumnIndex(RELEASE_YEAR));
            String[] genre = c.getString(c.getColumnIndex(GENRE)).split(", ");

            movies.add(new Movie(title, image, rating, releaseYear, genre));
        } while (c.moveToNext());

        return movies;
    }

    private void continueFlow(List<Movie> movies) {
        Log.d(TAG, "continueFlow");
        Intent start = new Intent(SplashActivity.this, MainActivity.class);
        start.putExtra(MOVIES_KEY, (ArrayList<Movie>)movies);

        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(start);
        finish();
    }

    @Override
    public void onFinish(final List<Movie> movies) {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                continueFlow(movies);
            }
        }, MIN_SPLASH_DELAY);

        DataController.getInstance().saveOnSqlLite(moviesDB, movies, true);
    }
}