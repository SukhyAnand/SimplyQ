package com.example.queue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class BusBooking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_booking);

        TextView busName = (TextView)findViewById(R.id.busName);
        ImageView busImageName = findViewById(R.id.busImageName);
        TextView busFromDestination = (TextView)findViewById(R.id.busFromDestination);
        TextView busToDestination = (TextView)findViewById(R.id.busToDestination);

        //int busID;

        if(getIntent() != null)
        {
            //busID = getIntent().getIntExtra("Bus ID", 0);
            //int ID = getIntent().getIntExtra("Bus ID", 0);
            String bus = getIntent().getStringExtra("Bus Name");
            int busImage = getIntent().getIntExtra("Bus Image", R.drawable.gold);
            String fromDestination = getIntent().getStringExtra("Bus From Destination");
            String toDestination = getIntent().getStringExtra("Bus To Destination");
            busName.setText(bus);
            busImageName.setImageResource(busImage);
            busFromDestination.setText(fromDestination+" /");
            busToDestination.setText(toDestination);
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (BusBooking.this, QRcode.class);
                intent.putExtra("Bus ID",getIntent().getIntExtra("Bus ID", 0));
                startActivity(intent);
            }
        });

        new FetchAvailableSeats().execute();
    }

    class FetchAvailableSeats extends AsyncTask<Void, Void, String> {

        String availability;

        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            URL url;
            try {
                String ID = String.valueOf(getIntent().getIntExtra("Bus ID", 0));
                Log.i("AVAILABLE", ID);
                url = new URL("http://13.52.76.220:8080/busavailability?busID="+ID);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    JSONObject obj = convertAsJSON(convertToReadableStream(urlConnection.getContentEncoding(), in));
                    String decodedBusesJsonString = URLDecoder.decode(obj.getString("availability"));
                    Log.i("LAWDA", decodedBusesJsonString);
                    availability = decodedBusesJsonString;

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
            TextView availableSeats = findViewById(R.id.availableSeats);
            availableSeats.setText("Available Seats: "+availability);
            //progressBar.setVisibility(View.GONE);
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
}
