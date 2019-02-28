package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> Movie;

    public MovieAdapter(ArrayList<Movie> movie, Context context) {
        super(context, R.layout.layout_listview_row, movie);
        this.Movie = movie;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_listview_row, parent, false);

        Movie m = Movie.get(position);

        TextView titleView = (TextView)view.findViewById(R.id.title);
        TextView castyearView = (TextView)view.findViewById(R.id.castyear);
        TextView directorView = (TextView)view.findViewById(R.id.director);
        TextView genresView = (TextView)view.findViewById(R.id.genres);
        TextView starsView = (TextView)view.findViewById(R.id.stars);

        titleView.setText(m.getName());
        castyearView.setText(m.getCastYear().toString());
        directorView.setText(m.getdirector());
        genresView.setText(m.getgenres());
        starsView.setText(m.getstars());

        return view;
    }
}
