package com.appstairs.movies.Splash.Controller;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.appstairs.movies.Main.Controller.MainController;
import com.appstairs.movies.Main.Model.MovieModel;
import com.appstairs.movies.Splash.Model.SplashMovieModel;

import java.util.List;

import static com.appstairs.movies.Main.Model.iMovie.GENRE;
import static com.appstairs.movies.Main.Model.iMovie.IMAGE;
import static com.appstairs.movies.Main.Model.iMovie.RATING;
import static com.appstairs.movies.Main.Model.iMovie.RELEASE_YEAR;
import static com.appstairs.movies.Main.Model.iMovie.TABLE_NAME;
import static com.appstairs.movies.Main.Model.iMovie.TITLE;

public class SplashSqlController extends MainController {
    private static final SplashSqlController controller = new SplashSqlController();
    SQLiteDatabase moviesDB;

    private SplashSqlController() {}

    public static SplashSqlController getInstance() {
        if (controller == null)
            return new SplashSqlController();
        return controller;
    }

    public boolean isDbExists(SQLiteDatabase db, String table) {
        try {
            moviesDB = db;
            moviesDB.rawQuery("SELECT * FROM " + table, null);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<MovieModel> parseSqlLite(List<MovieModel> movies) {
        Cursor c = moviesDB.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        c.moveToFirst();

        do {
            String title = c.getString(c.getColumnIndex(TITLE));
            String image = c.getString(c.getColumnIndex(IMAGE));
            int rating = c.getInt(c.getColumnIndex(RATING));
            int releaseYear = c.getInt(c.getColumnIndex(RELEASE_YEAR));
            String[] genre = c.getString(c.getColumnIndex(GENRE)).split(", ");

            movies.add(new SplashMovieModel(title, image, rating, releaseYear, genre));
        } while (c.moveToNext());

        return movies;
    }
}