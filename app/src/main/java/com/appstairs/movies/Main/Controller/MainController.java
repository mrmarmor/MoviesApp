package com.appstairs.movies.Main.Controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appstairs.movies.Main.Model.MovieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.appstairs.movies.Main.Model.iMovie.IMAGE;
import static com.appstairs.movies.Main.Model.iMovie.RATING;
import static com.appstairs.movies.Main.Model.iMovie.RELEASE_YEAR;
import static com.appstairs.movies.Main.Model.iMovie.TABLE_NAME;
import static com.appstairs.movies.Main.Model.iMovie.GENRE;
import static com.appstairs.movies.Main.Model.iMovie.TITLE;

public class MainController /*TODO This is a singleton class*/{
    private static final String TAG = MainController.class.getCanonicalName();
    private static final MainController dataController = new MainController();

    private String url;
    private List<MovieModel> movieModels = new ArrayList<>();
    private DataListener dataListener;

    protected MainController() { }

    public static MainController getInstance() {
        if (dataController == null)
            return new MainController();
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

                MovieModel movie = new MovieModel(title, image, rating, releaseYear, genre);
                movieModels.add(movie);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(movieModels, new ReleaseComparator());
        dataListener.onFinish(movieModels);
    }

    public void saveOnSqlLite(SQLiteDatabase moviesDB, List<MovieModel> movies, boolean createNewColumns) {
        if (createNewColumns) {
            moviesDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (tmp VARCHAR);");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TITLE + " VARCHAR");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + IMAGE + " VARCHAR");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + RATING + " INTEGER");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + RELEASE_YEAR + " INTEGER");
            moviesDB.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GENRE + " VARCHAR");
        }
        /*ContentValues cv = new ContentValues();//TODO we can use contentValues
        for (MovieModel movie : movieModels) {
            cv.put(TITLE, movie.getTitle());
            cv.put(IMAGE, movie.getImage());
            cv.put(RATING, movie.getRating());
            cv.put(RELEASE_YEAR, movie.getReleaseYear());

            for (int i = 0; i < movie.getGenre().length; i++) {
                cv.put(movie.getTitle() + " " + GENRE + i, movie.getGenre()[i]);
            }
        }
        moviesDB.insert(TABLE_NAME, null, cv);*/

        for (MovieModel movie : movies) {
            moviesDB.execSQL(
                    "INSERT INTO " + TABLE_NAME + " (" + TITLE + ", " + IMAGE + ", " + RATING + ", " + RELEASE_YEAR + ", " + GENRE + ")" +
                            " VALUES (\"" + movie.getTitle() + "\", \"" + movie.getImage() + "\", " + movie.getRating() + ", "
                            + movie.getReleaseYear() + ", \"" + movie.getGenre()[0]+", "+movie.getGenre()[1]+", "+movie.getGenre()[2] + "\");");
        }

        moviesDB.close();
    }

    public MovieModel parseValue(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            String title = jsonObject.getString("title");
            String image = jsonObject.getString("image");
            int rating = jsonObject.getInt("rating");
            int releaseYear = jsonObject.getInt("releaseYear");

            String[] genre = new String[3];
            JSONArray genreArr = jsonObject.getJSONArray("genre");
            for (int j = 0; j < genreArr.length(); j++)
                genre[j] = genreArr.getString(j);

            return new MovieModel(title, image, rating, releaseYear, genre);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public class ReleaseComparator implements Comparator<MovieModel> {//sort movieModels by release date
        @Override
        public int compare(MovieModel o1, MovieModel o2) {
            return o2.getReleaseYear() - o1.getReleaseYear();
        }
    }

    public interface DataListener {
        void onFinish(List<MovieModel> movies);
    }
}