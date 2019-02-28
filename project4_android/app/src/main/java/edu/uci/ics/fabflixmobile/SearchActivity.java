package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class SearchActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
    }


    public void searchMovie(View view) {
        String title = ((EditText) findViewById(R.id.title_input)).getText().toString();
        Intent goToIntent = new Intent(this, ListViewActivity.class);
        goToIntent.putExtra("title", title);
        startActivity(goToIntent);
    }
}


//
//    final RequestQueue queue = NetworkManager.sharedManager(this).queue;
//    // init url
//    String title = ((EditText) findViewById(R.id.title_input)).getText().toString();
//    String url = IpAddress.ip+"project4/api/movies?p=0&numRecord=15&genre=&Title=" + title + "&Year=&Director=&Star_name=&sort=ASC";
//
//    final StringRequest loginRequest = new StringRequest(Request.Method.GET, url,
//            new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.d("response", response);
//
//                }
//            },
//            new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    // error
//                    Log.d("login.error", error.toString());
//                }
//            }
//    );
//
//// !important: queue.add is where the login request is actually sent
//        queue.add(loginRequest);