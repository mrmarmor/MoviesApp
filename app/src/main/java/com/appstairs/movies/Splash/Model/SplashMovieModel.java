package com.appstairs.movies.Splash.Model;

import com.appstairs.movies.Main.Model.MovieModel;

//this class was made to reach the MVC principles even though we can use MovieModel in whole project
public class SplashMovieModel extends MovieModel {//inheritance is used to avoid duplicating class

    public SplashMovieModel(String title, String image, int rating, int releaseYear, String[] genre) {
        super(title, image, rating, releaseYear, genre);
    }
}