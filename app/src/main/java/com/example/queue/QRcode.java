package com.example.queue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

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

public class QRcode extends AppCompatActivity {

    ProgressBar progressBar;
    int ID;
    String beaconcode;
    String QRDecoded;
    String entry;
    TextView message;
    ImageView image;
    boolean beaconAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        if (getIntent() != null) {
            //busID = getIntent().getIntExtra("Bus ID", 0);
            ID = getIntent().getIntExtra("Bus ID", 0);
            Log.i("LAWDAID", String.valueOf(ID));
        }

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        new QRcode.FetchUniqueCode().execute();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        message = findViewById(R.id.qrmessage);
        image = findViewById(R.id.qrcode);

        if (!beaconAvailable) {
            message.setText("You are out of the Beacon range. Please get into the Beacon connectivity area and try to reserve your ride again!");
        } else {
            new QRcode.FetchQRCode().execute();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (entry.equals("true")) {
                message.setText("Congratulations! You have booked a seat!");
                QRCodeWriter writer = new QRCodeWriter();
                try {
                    BitMatrix bitMatrix = writer.encode(QRDecoded, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    image.setImageBitmap(bmp);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else {
                message.setText("Sorry! All seats for the current bus are full.\n " +
                        "You will receive a QR code when the next bus is available.");
            }
        }
    }

    class FetchUniqueCode extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            URL url;
            if (CheckNetwork.isInternetAvailable(getApplicationContext())) {
                try {
                    url = new URL("http://13.52.76.220:8080/beaconuid");
                    //url = new URL("http://10.10.10.10:8000/getcode");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        JSONObject obj = convertAsJSON(convertToReadableStream(urlConnection.getContentEncoding(), in));
                        String decodedBusesJsonString = URLDecoder.decode(obj.getString("uid"));
                        Log.i("LAWDA", decodedBusesJsonString);
                        beaconcode = decodedBusesJsonString;

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Log.i("NOOOOOO","Haha");
                    beaconAvailable = false;
                    e.printStackTrace();

                }
            } else {
                beaconAvailable = false;
            }
            return null;
        }

        protected void onPostExecute(String response) {
            Log.i("NOOOOOO","Haha");
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

    class FetchQRCode extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            URL url;
            try {
                Log.i("BEACONQD", beaconcode);
                String temp = "http://13.52.76.220:8080/adduser?beaconUID="+beaconcode+"&busID="+String.valueOf(ID);
                Log.i("TESTING", temp);
                url = new URL("http://13.52.76.220:8080/adduser?beaconUID="+beaconcode+"&busID="+String.valueOf(ID));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.setRequestMethod("POST");
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    JSONObject obj = convertAsJSON(convertToReadableStream(urlConnection.getContentEncoding(), in));
                    String responseString = URLDecoder.decode(obj.getString("response"));
                    Log.i("LAWDA", responseString);
                    JSONObject obj2 = new JSONObject(responseString);
                    QRDecoded = obj2.getString("uid");
                    entry = obj2.getString("entry");


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
            progressBar.setVisibility(View.GONE);
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
