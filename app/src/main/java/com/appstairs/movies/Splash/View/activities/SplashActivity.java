package com.appstairs.movies;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.appstairs.movieModels.R;
import com.appstairs.movies.Main.Controller.MainController;
import com.appstairs.movies.Main.Model.MovieModel;
import com.appstairs.movies.Main.View.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

import static com.appstairs.movies.Main.Model.iMovie.GENRE;
import static com.appstairs.movies.Main.Model.iMovie.IMAGE;
import static com.appstairs.movies.Main.Model.iMovie.MOVIES_DB;
import static com.appstairs.movies.Main.Model.iMovie.RATING;
import static com.appstairs.movies.Main.Model.iMovie.RELEASE_YEAR;
import static com.appstairs.movies.Main.Model.iMovie.TABLE_NAME;
import static com.appstairs.movies.Main.Model.iMovie.TITLE;

public class SplashActivity extends Activity implements MainController.DataListener {

    public static final String MOVIES_KEY = "movies key";
    private final String TAG = SplashActivity.class.getSimpleName();
    private final long MIN_SPLASH_DELAY = 2000;

    private MainController mainController = MainController.getInstance();
    private SQLiteDatabase moviesDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.splashTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        moviesDB = openOrCreateDatabase(MOVIES_DB, MODE_PRIVATE, null);

        if (isDbExists(moviesDB, TABLE_NAME)) {
            List<MovieModel> movies = parseSqlLite(new ArrayList<MovieModel>());
            continueFlow(movies);

        } else {
            mainController.setUrl("https://api.androidhive.info/json/movies.json");
            mainController.retrieveData(this);
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

    private List<MovieModel> parseSqlLite(List<MovieModel> movies) {
        Cursor c = moviesDB.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        c.moveToFirst();

        do {
            String title = c.getString(c.getColumnIndex(TITLE));
            String image = c.getString(c.getColumnIndex(IMAGE));
            int rating = c.getInt(c.getColumnIndex(RATING));
            int releaseYear = c.getInt(c.getColumnIndex(RELEASE_YEAR));
            String[] genre = c.getString(c.getColumnIndex(GENRE)).split(", ");

            movies.add(new MovieModel(title, image, rating, releaseYear, genre));
        } while (c.moveToNext());

        return movies;
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