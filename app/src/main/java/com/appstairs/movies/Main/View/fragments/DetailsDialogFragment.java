package com.appstairs.movies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class DetailsDialogFragment extends DialogFragment {
    private Movie movie;

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

        title.setText("Movie name: " + movie.getTitle());/*TODO it is preferred using string format*/
        Glide.with(v).asBitmap().load(movie.getImage()).into(imageView);
        rating.setText("Movie rating: " + movie.getRating());
        releaseYear.setText("Movie release year: " + movie.getReleaseYear());
        genre.setText(String.format("Movie genre: %s\\%s\\%s", movie.getGenre()[0], movie.getGenre()[1], movie.getGenre()[2]));

        Button okButton = (Button)v.findViewById(R.id.details_ok_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setData(Movie movie) {
        this.movie = movie;
    }
}