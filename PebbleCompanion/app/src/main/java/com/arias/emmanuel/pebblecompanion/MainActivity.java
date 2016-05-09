package com.arias.emmanuel.pebblecompanion;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

// La clase implementa el manejador de Android para la localizaci[on
public class MainActivity extends AppCompatActivity implements LocationListener {

    // esta variable viene del IDE cloudPebble
    private UUID uid = UUID.fromString("15bfbb0b-33d4-4d88-b749-a431cd78a6bf");

    protected LocationManager locationManager;
    TextView txtLat;

    private long compassData;
    private double latitudeData, longitudeData;

    // Maneja los mensajes recibidos desde el app Pebble
    PebbleKit.PebbleDataReceiver dataReceiver = new PebbleKit.PebbleDataReceiver(uid) {

        @Override
        public void receiveData(Context context, int transaction_id,
                                PebbleDictionary dict) {
            // A new AppMessage was received, tell Pebble
            StringBuilder builder = new StringBuilder();
            builder.append("Compass: ");
            compassData = dict.getInteger(0);
            builder.append(compassData);
            builder.append("°");

            TextView textViewDirection = (TextView) findViewById(R.id.text_view_direction);
            textViewDirection.setText(builder.toString());
            //System.out.println(builder.toString());
            PebbleKit.sendAckToPebble(context, transaction_id);
        }

    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.arias.emmanuel.pebblecompanion/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onPause() {
        super.onPause();

        Context context = getApplicationContext();
        PebbleKit.closeAppOnPebble(context, uid);

        locationManager.removeUpdates(this);

    }
    @Override
    public void onStop() {
        super.onStop();

        Context context = getApplicationContext();
        PebbleKit.closeAppOnPebble(context, uid);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.arias.emmanuel.pebblecompanion/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnPut = (Button) findViewById(R.id.btnPut);
        btnPut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postPosition(view);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postPosition(view);
            }
        });

        getPosition(null);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        TextView tv=(TextView)findViewById(R.id.txtGet);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }

    private void getPosition(View view){
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = null;

                    URL url = new URL("http://eariassoto.ddns.net/api/positions/1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();

                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String text = null;
                    try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                        text = scanner.useDelimiter("\\A").next();
                    }

                    TextView textView = (TextView) findViewById(R.id.txtGet);
                    textView.setText("Response: "+response + "\nData: " + text);

                    if (is != null) {
                        is.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void postPosition(View view){
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = null;
                    String str =  "{\"compass\": "+compassData+",\"latitude\":\""+latitudeData+"\",\"longitude\":\""+longitudeData+"\"}";
                    URL url = new URL("http://eariassoto.ddns.net/api/positions/1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("PUT");
                    conn.setDoInput(true);

                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(str.getBytes().length));
                    byte[] outputInBytes = str.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();

                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String text = null;
                    try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                        text = scanner.useDelimiter("\\A").next();
                    }

                    TextView textView = (TextView) findViewById(R.id.txtGet);
                    textView.setText("Response: "+response + "\nData: " + text);

                    // TODO no sirve
                    Snackbar.make(view, "PUT succesful", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    if (is != null) {
                        is.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Construct output String
        StringBuilder builder = new StringBuilder();
        builder.append("Pebble Info\n\n");

        // Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);
        builder.append("Watch connected: " + (isConnected ? "true" : "false")).append("\n");

        // What is the firmware version?
        PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
        builder.append("Firmware version: ");
        builder.append(info.getMajor()).append(".");
        builder.append(info.getMinor()).append("\n");

        // Is AppMesage supported?
        boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
        builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));
        builder.append("\n");

        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText(builder.toString());


        Context context = getApplicationContext();
        PebbleKit.startAppOnPebble(context, uid);

        PebbleKit.registerReceivedDataHandler(context, dataReceiver);

        txtLat = (TextView) findViewById(R.id.text_view_loc);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            latitudeData = loc.getLatitude();
            longitudeData = loc.getLongitude();

            String latitude = "Latitude: " + latitudeData;
            String longitude = "Longitude: " + longitudeData;

            String s = latitude + "\n" + longitude;

            txtLat.setText(s);
        }
        catch(SecurityException s){
            txtLat = (TextView) findViewById(R.id.text_view_loc);
            txtLat.setText(s.getMessage());
        }


        getPosition(null);

    }

    @Override
    public void onLocationChanged(Location location) {
        txtLat = (TextView) findViewById(R.id.text_view_loc);
        latitudeData = location.getLatitude();
        longitudeData = location.getLongitude();
        txtLat.setText("Latitude:" + latitudeData + ", Longitude:" + longitudeData);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }
}
