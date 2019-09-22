package com.example.queue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MainActivity extends AppCompatActivity {

    GridView gridview;
    ProgressBar progressBar;
    boolean doubleBackToExitPressedOnce = false;

    ArrayList<Integer> busesID = new ArrayList<>();// = {1,2,3,4};
    ArrayList<String> busesName = new ArrayList<>();// = {"Mars", "Mercury", "Gold", "Maroon"};
    ArrayList<String> busesFromDestination = new ArrayList<>();
    ArrayList<String> busesToDestination = new ArrayList<>();
    HashMap<String, Integer> maps;

    ArrayList<Integer> busesImage = new ArrayList<>();// = {R.drawable.mars, R.drawable.mercury, R.drawable.gold, R.drawable.maroon};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        new RetrieveFeedTask().execute();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gridview = findViewById(R.id.grid_view);
        MainAdapter adapter = new MainAdapter(MainActivity.this, this.busesName, this.busesImage, this.busesFromDestination, this.busesToDestination, this.busesID);
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Checking in for " + busesName.get(+position),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this,BusBooking.class);
                intent.putExtra("Bus ID",busesID.get(+position));
                intent.putExtra("Bus Name",busesName.get(+position));
                intent.putExtra("Bus From Destination",busesFromDestination.get(+position));
                intent.putExtra("Bus To Destination",busesToDestination.get(+position));
                intent.putExtra("Bus Image",busesImage.get(+position));
                startActivity(intent);
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            URL url;
            try {
                url = new URL("http://13.52.76.220:8080/fetchbuses");

                maps = new HashMap<>();
                maps.put("Mars", R.drawable.mars);
                maps.put("Mercury", R.drawable.mercury);
                maps.put("Gold", R.drawable.gold);
                maps.put("Maroon", R.drawable.maroon);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    JSONObject obj = convertAsJSON(convertToReadableStream(urlConnection.getContentEncoding(), in));
                    String decodedBusesJsonString = URLDecoder.decode(obj.getString("buses"));
                    Log.i("LAWDA", decodedBusesJsonString);
                    JSONObject jsonObject = new JSONObject(decodedBusesJsonString);
                    JSONArray arr = jsonObject.getJSONArray("buses");
                    for (int i = 0; i < arr.length(); i++) {
                        busesID.add(Integer.parseInt(arr.getJSONObject(i).getString("id")));
                        busesName.add(arr.getJSONObject(i).getString("name"));
                        busesFromDestination.add(arr.getJSONObject(i).getString("from_destination"));
                        busesToDestination.add(arr.getJSONObject(i).getString("toDestination"));

                        busesImage.add(maps.get(arr.getJSONObject(i).getString("name")));
                        Log.i("LAWDA", arr.getJSONObject(i).getString("id"));
                        Log.i("LAWDA", arr.getJSONObject(i).getString("name"));
                        Log.i("LAWDA", arr.getJSONObject(i).getString("from_destination"));
                        Log.i("LAWDA", arr.getJSONObject(i).getString("toDestination"));
                        //System.out.println(post_id);
                    }

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String response) {
            //if(response == null) {
                //response = "THERE WAS AN ERROR";
            //}
            progressBar.setVisibility(View.GONE);
            /*gridview = findViewById(R.id.grid_view);
            MainAdapter adapter = new MainAdapter(MainActivity.this, busesName, busesImage, busesFromDestination, busesToDestination, busesID);
            gridview.setAdapter(adapter);*/

            //Log.i("LAWDA", response);
            //responseView.setText(response);
        }

        private InputStream convertToReadableStream(String contentEncoding, InputStream inputStream) throws Exception {
            return contentEncoding != null && contentEncoding.contains("gzip") ? new GZIPInputStream(inputStream) : inputStream;
        }


        private JSONObject convertAsJSON(InputStream is) throws Exception {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String str = null;
            while ((str = bufReader.readLine()) != null) {
                builder.append(str);
            }
            return new JSONObject(builder.toString());
        }
    }

    @Override

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
