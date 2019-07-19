package com.appstairs.movies.Main.View.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

import com.appstairs.movies.R;
import com.appstairs.movies.Main.Model.MovieModel;
import com.bumptech.glide.Glide;

public class DetailsDialogFragment extends DialogFragment {
    private MovieModel movieModel;

    public DetailsDialogFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_frg, container, false);
        setViews(v);

        return v;
    }

    private void setViews(View v) {
        TextView title = v.findViewById(R.id.details_title);
        ImageView imageView = v.findViewById(R.id.details_image);
        TextView rating = v.findViewById(R.id.details_rating);
        TextView releaseYear = v.findViewById(R.id.details_releaseYear);
        TextView genre = v.findViewById(R.id.details_genre);

        title.setText("MovieModel name: " + movieModel.getTitle());/*TODO it is preferred using string format*/
        rating.setText("MovieModel rating: " + movieModel.getRating());
        releaseYear.setText("MovieModel release year: " + movieModel.getReleaseYear());
        genre.setText(String.format("MovieModel genre: %s\\%s\\%s", movieModel.getGenre()[0], movieModel.getGenre()[1], movieModel.getGenre()[2]));

        Glide.with(v)
                .asBitmap()
                .placeholder(R.mipmap.ic_launcher)
                .load(movieModel.getImage())
                .into(imageView);

        Button okButton = (Button)v.findViewById(R.id.details_ok_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setData(MovieModel movieModel) {
        this.movieModel = movieModel;
    }
}