package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;


public class ListViewActivity extends ActionBarActivity {
    private Button btnL;
    private Button btnR;
    private ListView listView;
    private TextView pageText;
    private int totalPage;
    private int numRecord = 10;
    private int cur_p = 0;
    private String title;
    private String url;
    private ArrayList<Movie> movies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        btnL = (Button)findViewById(R.id.previous);
        btnR = (Button)findViewById(R.id.next);
        pageText = (TextView)findViewById(R.id.pageText);
        listView = (ListView)findViewById(R.id.list);
        // set up url from bundle
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");

        // Use the same network queue across our application
        RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // request server
        StringRequest  movieRequest = new StringRequest(Request.Method.GET, url = IpAddress.ip+"project4_web/api/movies?p=-1&numRecord="+ Integer.toString(numRecord) +"&genre=&Title=" + title + "&Year=&Director=&Star_name=&sort=ASC",
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                movies = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    totalPage = (Integer.parseInt(jsonArray.getJSONObject(0).getString("movieSize"))-1) / numRecord +1;

                    // init btn
                    enableCheck();

                    // store result into movies
                    for (int i = 1; i < jsonArray.length(); i++) {
                        // build movie object
                        JSONObject movieObj = jsonArray.getJSONObject(i);
                        String movieTitle = movieObj.getString("movie_title");
                        int movieYear = Integer.parseInt(movieObj.getString("movie_year"));
                        String movieDir = movieObj.getString("movie_director");
                        String movieGenres = movieObj.getString("genreList");
                        String movieStars = movieObj.getString("stars_name");

                        movies.add(new Movie(movieTitle, movieYear, movieDir, movieGenres, movieStars));
                    }

                    // load list view
                    loadView();
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON");
                }

            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("login.error", error.toString());
            }
            }
        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
//                        String message = String.format("Clicked on position: %d, name: %s, %d", position, person.getName(), person.getCastYear());
//                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                Intent goToIntent = new Intent(getApplicationContext(), SingleMovieActivity.class);

                goToIntent.putExtra("movie_title", movie.getName());
                goToIntent.putExtra("movie_year", movie.getCastYear());
                goToIntent.putExtra("movie_director", movie.getdirector());
                goToIntent.putExtra("genreList", movie.getgenres());
                goToIntent.putExtra("stars_name", movie.getstars());

                startActivity(goToIntent);
            }
        });

        btnL.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                cur_p = cur_p-1;
                Log.d("page", Integer.toString(cur_p));
                enableCheck();
                loadView();
            }
        });

        btnR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                cur_p = cur_p+1;
                Log.d("page", Integer.toString(cur_p));
                enableCheck();
                loadView();
            }
        });

        queue.add(movieRequest);
    }

    private void enableCheck(){
        if(cur_p+1 == totalPage) btnR.setEnabled(false);
        else btnR.setEnabled(true);
        if(cur_p == 0) btnL.setEnabled(false);
        else btnL.setEnabled(true);
    }

    private void loadView(){
        // adapt view
        pageText.setText(Integer.toString(cur_p+1) + " Out Of " + Integer.toString(totalPage));
        MovieAdapter adapter = new MovieAdapter(new ArrayList<Movie>(movies.subList(cur_p*numRecord, Math.min((cur_p+1)*numRecord, movies.size()))), getApplicationContext());
        listView.setAdapter(adapter);
        if (movies.size() == 0) {
            Toast.makeText(this, "No Result", Toast.LENGTH_LONG).show();
        }
    }
}
