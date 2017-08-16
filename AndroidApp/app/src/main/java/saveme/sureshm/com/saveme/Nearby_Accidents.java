package saveme.sureshm.com.saveme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

public class Nearby_Accidents extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String finalloc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby__accidents);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
            AlertDialog.Builder dialog = new AlertDialog.Builder(Nearby_Accidents.this);
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

            finalloc = latitude+","+longlat;

            ProceedAccidents pa = new ProceedAccidents();
            pa.execute(finalloc);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
        }

    }

    public class ProceedAccidents extends AsyncTask<String, String, String> {

        private ProgressDialog loadaccidents;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadaccidents = new ProgressDialog(Nearby_Accidents.this);
            loadaccidents.setIndeterminate(true);
            loadaccidents.setMessage("Processing...");
            loadaccidents.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String location = params[0];

            //Start the data part
            try{

                String link = "http://projects.stcicts.org/saveme/client.php?task=nearby&location="+Uri.encode(location);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String json = in.readLine();

                JSONArray jsonarray = new JSONArray(json);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    final String arlocation = jsonobject.getString("location");
                    String atype = jsonobject.getString("type");
                    final String[] coordinates = arlocation.split(",");

                    String emergencytitle = "";

                    if(atype.equals("natural")){
                        emergencytitle = jsonobject.getString("disaster")+" in "+jsonobject.getString("city");
                    } else if(atype.equals("fire")){
                        emergencytitle = "Fire in "+jsonobject.getString("city");
                    } else if(atype.equals("thief")){
                        emergencytitle = "Thief alert in "+jsonobject.getString("city");
                    } else if(atype.equals("road")){
                        emergencytitle = "Road accident in "+jsonobject.getString("city");
                    } else {
                        emergencytitle = "Emeygency in "+jsonobject.getString("city");
                    }

                    final String finaltitle = emergencytitle;

                    runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void run() {
                            // Add a marker in Sydney and move the camera
                            LatLng sydney = new LatLng(Float.valueOf(coordinates[0]), Float.valueOf(coordinates[1]));
                            mMap.addMarker(new MarkerOptions().position(sydney).title(finaltitle));
                        }
                    });
                }

                return "success";

            } catch(Exception e){
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!s.equals("success")){
                Toast.makeText(Nearby_Accidents.this, s, Toast.LENGTH_LONG).show();
            }

            loadaccidents.dismiss();
        }
    }
}
