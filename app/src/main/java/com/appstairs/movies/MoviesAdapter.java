package com.appstairs.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
    private Context ctx;
    private List<Movie> movies;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView textView;
        private MyViewHolder(View v) {
            super(v);
            view = v;
            textView = v.findViewById(R.id.rv_item_tv);
        }
    }

    public MoviesAdapter(Context context, List<Movie> myMovies) {
        ctx = context;
        movies = myMovies;
    }

    @Override
    public MoviesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.textView.setText(movies.get(position).getTitle());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailsDialogFragment detailsDialogFragment = new DetailsDialogFragment();
                detailsDialogFragment.setData(movies.get(position));
                detailsDialogFragment.show(((FragmentActivity)ctx).getSupportFragmentManager(), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }
}