package edu.uci.ics.fabflixmobile;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class SingleMovieActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_movie);

        Bundle bundle = getIntent().getExtras();
        String movie_title = bundle.getString("movie_title");
        int movie_year = bundle.getInt("movie_year");
        String movie_director = bundle.getString("movie_director");
        String genreList = bundle.getString("genreList");
        String stars_name = bundle.getString("stars_name");

        // set textview
        if (movie_title != null && !"".equals(movie_title)) {
            ((TextView) findViewById(R.id.title)).setText(movie_title);
        }
        ((TextView) findViewById(R.id.year)).setText(Integer.toString(movie_year));
        if (movie_director != null && !"".equals(movie_director)) {
            ((TextView) findViewById(R.id.director)).setText(movie_director);
        }
        if (genreList != null && !"".equals(genreList)) {
            ((TextView) findViewById(R.id.genres)).setText(genreList);
        }
        if (stars_name != null && !"".equals(stars_name)) {
            ((TextView) findViewById(R.id.stars)).setText(stars_name);
        }
    }


}
