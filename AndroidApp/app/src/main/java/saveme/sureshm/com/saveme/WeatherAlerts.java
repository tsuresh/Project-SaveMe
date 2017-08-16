package saveme.sureshm.com.saveme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WeatherAlerts extends AppCompatActivity implements LocationListener {

    @InjectView(R.id.temprature) TextView tvTemp;
    @InjectView(R.id.weathertype) TextView tvWtype;
    @InjectView(R.id.toolbar) Toolbar toolbar;
    EditText wcondition, whelp;
    Button wreport;

    public double latitude, longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_alerts);
        setSupportActionBar(toolbar);
        ButterKnife.inject(this);

        boolean gps_enabled = false;
        boolean network_enabled = false;

        double latitude, longlat;
        Location location;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(WeatherAlerts.this);
            dialog.setMessage(getBaseContext().getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getBaseContext().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(getBaseContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                }
            });
            dialog.show();
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            latitude = location.getLatitude();
            longlat = location.getLongitude();

            LoadWeather weatherReq = new LoadWeather();
            weatherReq.execute(String.valueOf(latitude),String.valueOf(longlat));
        }


        wcondition = (EditText) findViewById(R.id.input_wcondition);
        whelp = (EditText) findViewById(R.id.input_whelp);
        wreport = (Button) findViewById(R.id.reportweather);

        wreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wcondition.getText().toString().matches("") || whelp.getText().toString().matches("")){
                    Toast.makeText(WeatherAlerts.this, "The above text fields cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                   report("","","","natural",wcondition.getText().toString(),whelp.getText().toString());
                }
            }
        });

    }

    public String report(String scard, String nic, String name, String type, String disaster, String howhelp) {

        //Get info
        SharedPreferences sp = WeatherAlerts.this.getSharedPreferences("Login", 0);
        String reporter = sp.getString("username", "email");
        String reporterhash = sp.getString("hash", "hash");
        getLocation();
        String finalloc = latitude+","+longitude;

        MainActivity ma = new MainActivity();
        MainActivity.ProceedReport pc = ma.new ProceedReport(WeatherAlerts.this);

        pc.execute(reporter, reporterhash, scard, nic, name, finalloc, type, disaster, howhelp);

        return null;

    }

    public void getLocation() {

        boolean gps_enabled = false;
        boolean network_enabled = false;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if (gps_enabled && network_enabled) {

            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
            Location location = locationManager.getLastKnownLocation(bestProvider);

            Log.e("TAG", String.valueOf(location.getLatitude()));

            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            else{
                locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
            }


        } else {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
            dialog.setMessage(getBaseContext().getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getBaseContext().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(getBaseContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class LoadWeather extends AsyncTask<String, String, String> {

        private ProgressDialog weatherprogress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weatherprogress = new ProgressDialog(WeatherAlerts.this);
            weatherprogress.setIndeterminate(true);
            weatherprogress.setMessage("Loading Weather News...");
            weatherprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String latitude = params[0];
            String longlatitude = params[1];

            try{

                String link = "http://projects.stcicts.org/saveme/client.php?task=weather&lat="+Uri.encode(latitude)+"&longlat="+Uri.encode(longlatitude);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String json = in.readLine();
                JSONObject jsonObject = new JSONObject(json);

                String status = jsonObject.getString("status");

                if(status.equals("ok")){
                    final String temp = jsonObject.getString("temp")+" C";
                    final String wmode = jsonObject.getString("wmode");
                    final String area = jsonObject.getString("city");

                    runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void run() {
                            tvTemp.setText(temp);
                            tvWtype.setText(wmode);
                            toolbar.setTitle(area);
                        }
                    });
                }

                return status;
            }

            catch(Exception e){
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(WeatherAlerts.this, s, Toast.LENGTH_LONG).show();
            weatherprogress.dismiss();
        }

    }

}
