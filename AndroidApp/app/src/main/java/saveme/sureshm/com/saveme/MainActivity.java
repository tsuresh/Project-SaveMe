package saveme.sureshm.com.saveme;

import android.Manifest;
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
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LocationListener {

    ImageButton weather, road, fire, thief, aroundme, about;
    Button editprofile, connectiot;
    String provider, qrcode;

    public double latitude, longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;

    private static final int QR_RESULT_CODE = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Logout")){

            SharedPreferences sp=getSharedPreferences("Login", 0);
            SharedPreferences.Editor Ed=sp.edit();
            Ed.putString("username","");
            Ed.putString("hash","");
            Ed.commit();

            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();

        } else if(item.getTitle().equals("Website")){
            String url = "http://projects.stcicts.org/crisiscall";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent exitint = new Intent(Intent.ACTION_MAIN);
        exitint.addCategory(Intent.CATEGORY_HOME);
        exitint.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exitint);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage("Please enable GPS and DATA on your device.");
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
                    System.exit(1);
                }
            });
            dialog.show();
        }

        //define the form controls
        editprofile = (Button) findViewById(R.id.updateprofile);
        connectiot = (Button) findViewById(R.id.iotconnect);
        weather = (ImageButton) findViewById(R.id.weather);
        road = (ImageButton) findViewById(R.id.road);
        fire = (ImageButton) findViewById(R.id.fire);
        thief = (ImageButton) findViewById(R.id.thief);
        aroundme = (ImageButton) findViewById(R.id.aroundme);
        about = (ImageButton) findViewById(R.id.about);

        //IOT connection
        connectiot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IOTConnect.class);
                startActivity(intent);
            }
        });

        //about the user
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });

        //accidents around me
        aroundme.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Nearby_Accidents.class);
                startActivity(intent);
            }
        });

        //thief alert reporting
        thief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Report to CrisisCall", "Call a relative", "Start the thief siren"};
                AlertDialog.Builder dbuilder = new AlertDialog.Builder(MainActivity.this);
                dbuilder.setTitle("Report a Thief");
                dbuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dg, int item) {
                        switch (item){
                            case 0:

                                report("","","","thief","","");
                                break;

                            case 1:

                                SharedPreferences sp = getSharedPreferences("Login", 0);
                                String emnum = sp.getString("emergencyno", "");
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + emnum));
                                startActivity(intent);
                                break;

                            case 2:

                                Toast.makeText(getApplicationContext(), "Thief alert can be intergrated here",Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
                AlertDialog thiefdialog = dbuilder.create();
                thiefdialog.show();
            }
        });

        //fire alerts
        fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage("Are you sure want to report fire?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        report("","","","fire","","");
                    }
                });
                dialog.setNegativeButton(getBaseContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(MainActivity.this,"Report dismissed!",Toast.LENGTH_SHORT);
                    }
                });
                dialog.show();
            }
        });

        //road accident reporting
        road.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Report via CC Card", "Enter Persons name", "Enter the NIC Number", "Send me Location"};
                AlertDialog.Builder dbuilder = new AlertDialog.Builder(MainActivity.this);
                dbuilder.setTitle("Report a Vehicle Accident");
                dbuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dg, int item) {
                        switch(item){
                            case 0:

                                Intent myIntent = new Intent(MainActivity.this, DecoderActivity.class);
                                startActivityForResult(myIntent, QR_RESULT_CODE);

                                break;
                            case 1:

                                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                                dialog.setMessage("Please enter the name of the victim");

                                final EditText inputnm = new EditText(MainActivity.this);
                                inputnm.setInputType(InputType.TYPE_CLASS_TEXT);
                                dialog.setView(inputnm);

                                dialog.setPositiveButton("Yes, Report Now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        report("","",inputnm.getText().toString(),"road","","");
                                    }
                                });
                                dialog.setNegativeButton(getBaseContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        // TODO Auto-generated method stub
                                    }
                                });
                                dialog.show();

                                break;
                            case 2:

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Report via NIC number");

                                final EditText input = new EditText(MainActivity.this);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String nicno = input.getText().toString();
                                        report("", nicno, "","road","","");
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                                break;
                            case 3:

                                AlertDialog.Builder odialog = new AlertDialog.Builder(MainActivity.this);
                                odialog.setMessage("Your current location will be sent as the accident location");
                                odialog.setPositiveButton("Yes, Report Now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        report("","","","road","","");
                                    }
                                });
                                odialog.setNegativeButton(getBaseContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        // TODO Auto-generated method stub
                                    }
                                });
                                odialog.show();

                                break;
                        }
                    }
                });
                AlertDialog dalert = dbuilder.create();
                dalert.show();
            }
        });

        //weather condition button
        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WeatherAlerts.class);
                startActivity(intent);
            }
        });

        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Main Activity
                Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == QR_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("qrCode"); // This will contain your scan result
                qrcode = contents;
                report(contents, "", "", "road","","");
            }
        }
    }

    //reporting function
    public void report(String scard, String nic, String name, String type, String disaster, String howhelp) {
        //Get info
        SharedPreferences sp = MainActivity.this.getSharedPreferences("Login", 0);
        String reporter = sp.getString("username", "email");
        String reporterhash = sp.getString("hash", "hash");
        getLocation();
        String finalloc = latitude+","+longitude;

        MainActivity ma = new MainActivity();
        MainActivity.ProceedReport pc = ma.new ProceedReport(MainActivity.this);

        pc.execute(reporter, reporterhash, scard, nic, name, finalloc, type, disaster, howhelp);
    }

    //get the current location
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
    public void onLocationChanged(Location mlocation) {
        latitude = mlocation.getLatitude();
        longitude = mlocation.getLongitude();
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

    public class ProceedReport extends AsyncTask<String, String, String> {

        private ProgressDialog reportprogress;

        private Context mContext;

        public ProceedReport(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            reportprogress = new ProgressDialog(mContext);
            reportprogress.setIndeterminate(true);
            reportprogress.setMessage("Reporting...");
            reportprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String g_reporter = params[0];
            String g_reporterhash = params[1];
            String g_savemecard = params[2];
            String g_nic = params[3];
            String g_nane = params[4];
            String g_location = params[5];
            String g_type = params[6];
            String g_disaster = params[7];
            String g_howhelp = params[8];

            try{

                String link = "http://projects.stcicts.org/saveme/client.php?task=report&reporter="+Uri.encode(g_reporter)+"&hash="+Uri.encode(g_reporterhash)+"&savecard="+Uri.encode(g_savemecard)+"&nic="+Uri.encode(g_nic)+"&name="+Uri.encode(g_nane)+"&location="+Uri.encode(g_location)+"&type="+Uri.encode(g_type)+"&disaster="+Uri.encode(g_disaster)+"&howhelp="+Uri.encode(g_howhelp);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String json = in.readLine();
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.getString("message");
                return message;
            }

            catch(Exception e){
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
            reportprogress.dismiss();
        }
    }



}