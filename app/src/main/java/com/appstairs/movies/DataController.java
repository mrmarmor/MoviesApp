package com.appstairs.movies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.appstairs.movies.SplashActivity.GENRE;
import static com.appstairs.movies.SplashActivity.IMAGE;
import static com.appstairs.movies.SplashActivity.RATING;
import static com.appstairs.movies.SplashActivity.RELEASE_YEAR;
import static com.appstairs.movies.SplashActivity.TABLE_NAME;
import static com.appstairs.movies.SplashActivity.TITLE;

public class DataController /*TODO This is a singleton class*/{
    private static final String TAG = DataController.class.getCanonicalName();
    private static final DataController dataController = new DataController();

    private String url;
    private List<Movie> movies = new ArrayList<>();
    private DataListener dataListener;

    private DataController() {}

    public static DataController getInstance() {
        if (dataController == null)
            return new DataController();
        return dataController;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void retrieveData(Context context) {
        dataListener = (DataListener)context;

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        parseResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(TAG, "onErrorResponse: " + error.getMessage());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void parseResponse(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String title = jsonArray.getJSONObject(i).getString("title");
                String image = jsonArray.getJSONObject(i).getString("image");
                int rating = jsonArray.getJSONObject(i).getInt("rating");
                int releaseYear = jsonArray.getJSONObject(i).getInt("releaseYear");

                String[] genre = new String[3];
                JSONArray genreArr = jsonArray.getJSONObject(i).getJSONArray("genre");
                for (int j = 0; j < genreArr.length(); j++)
                    genre[j] = genreArr.getString(j);

                Movie movie = new Movie(title, image, rating, releaseYear, genre);
                movies.add(movie);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(movies, new ReleaseComparator());
        dataListener.onFinish(movies);
    }

    public void saveOnSqlLite(SQLiteDatabase moviesDB, List<Movie> movies, boolean createNewColumns) {
        if (createNewColumns) {
            moviesDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (tmp VARCHAR);");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TITLE + " VARCHAR");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + IMAGE + " VARCHAR");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + RATING + " INTEGER");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + RELEASE_YEAR + " INTEGER");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GENRE + " VARCHAR");
        }
        /*ContentValues cv = new ContentValues();//TODO we can use contentValues
        for (Movie movie : movies) {
            cv.put(TITLE, movie.getTitle());
            cv.put(IMAGE, movie.getImage());
            cv.put(RATING, movie.getRating());
            cv.put(RELEASE_YEAR, movie.getReleaseYear());

            for (int i = 0; i < movie.getGenre().length; i++) {
                cv.put(movie.getTitle() + " " + GENRE + i, movie.getGenre()[i]);
            }
        }
        moviesDB.insert(TABLE_NAME, null, cv);*/

        for (Movie movie : movies) {
            moviesDB.execSQL(
                    "INSERT INTO " + TABLE_NAME + " (" + TITLE + ", " + IMAGE + ", " + RATING + ", " + RELEASE_YEAR + ", " + GENRE + ")" +
                            " VALUES (\"" + movie.getTitle() + "\", \"" + movie.getImage() + "\", " + movie.getRating() + ", "
                            + movie.getReleaseYear() + ", \"" + movie.getGenre()[0]+", "+movie.getGenre()[1]+", "+movie.getGenre()[2] + "\");");
        }

        moviesDB.close();
    }

    public class ReleaseComparator implements Comparator<Movie> {//sort movies by release date
        @Override
        public int compare(Movie o1, Movie o2) {
            return o2.getReleaseYear() - o1.getReleaseYear();
        }
    }

    public interface DataListener {
        void onFinish(List<Movie> movies);
    }
}