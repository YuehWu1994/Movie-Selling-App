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
        if(title.isEmpty()){
            ((EditText) findViewById(R.id.title_input)).setError( "Title is required!" );
        }
        else{
            Intent goToIntent = new Intent(this, ListViewActivity.class);
            goToIntent.putExtra("title", title);
            startActivity(goToIntent);
        }
    }
}
