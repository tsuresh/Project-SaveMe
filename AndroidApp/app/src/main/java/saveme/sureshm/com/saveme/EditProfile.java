package saveme.sureshm.com.saveme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class EditProfile extends AppCompatActivity {

    @InjectView(R.id.editpass) EditText editpass;
    @InjectView(R.id.editnic) EditText editnic;
    @InjectView(R.id.editname) EditText editfname;
    @InjectView(R.id.editemer) EditText editeme;
    @InjectView(R.id.btneditprofile1) Button editprofile1;

    String newpass = "";
    String newname = "";
    String neweme = "";
    String newnic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.inject(this);

        SharedPreferences sp = getSharedPreferences("Login", 0);

        editeme.setText(sp.getString("emergencyno", ""));
        editfname.setText(sp.getString("fullname", ""));
        editnic.setText(sp.getString("nic", ""));

        editprofile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editprofile();
            }
        });
    }

    public void editprofile()  {
        if(validate() == false){
            Toast.makeText(getBaseContext(), "Editing failed", Toast.LENGTH_LONG).show();
            editprofile1.setEnabled(true);
            return;
        }
        editprofile1.setEnabled(true);
       UpdateProf(newname,newpass,neweme,newnic);
    }


    public boolean validate() {
        boolean valid = true;

        String fname = editfname.getText().toString();
        String password = editpass.getText().toString();
        String nic = editnic.getText().toString();
        String eme = editeme.getText().toString();

        if (!password.isEmpty()) {
            if (password.length() < 4 || password.length() > 10) {
                editpass.setError("Between 4 and 10 alphanumeric characters");
                valid = false;
            } else {
                newpass = password;
                editpass.setError(null);
            }
        }
        if(!fname.isEmpty()){
            newname = fname;
        }
        if(!eme.isEmpty()){
            neweme = eme;
        }
        if(!nic.isEmpty()){
            newnic = nic;
        }

        return valid;
    }

    public String UpdateProf(String name, String password, String emergency, String nic){
        SharedPreferences sp = getSharedPreferences("Login", 0);
        String reporter = sp.getString("username", "email");
        String reporterhash = sp.getString("hash", "hash");
        ProceedEdit proceedEdit = new ProceedEdit();
        proceedEdit.execute(name,password,emergency,nic,reporter,reporterhash);
        return null;
    }

    class ProceedEdit extends AsyncTask<String, String, String> {

        private ProgressDialog regprogress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            regprogress = new ProgressDialog(EditProfile.this);
            regprogress.setIndeterminate(true);
            regprogress.setMessage("Processing...");
            regprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String name = params[0];
            String password = params[1];
            String emergency =  params[2];
            String nic =  params[3];

            String username = params[4];
            String hash = params[5];

            try{
                String link = "http://icts.stcmount.edu.lk/saveme/client.php?task=editprofile&email="+Uri.encode(username)+"&hash="+Uri.encode(hash)+"&name="+Uri.encode(name)+"&password="+Uri.encode(password)+"&emergency="+Uri.encode(emergency)+"&nic="+Uri.encode(nic);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String json = in.readLine();
                JSONObject jsonObject = new JSONObject(json);

                String message = jsonObject.getString("message");

                in.close();
                return message;
            }

            catch(Exception e){
                return e.toString();
            }
           // return name + " " + password + " " + emergency + " " + nic + " " + username + " "+ hash;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            regprogress.dismiss();

            if(s.equalsIgnoreCase("success")){

                Toast.makeText(getBaseContext(), "Your profile has been successfully updated", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
            }
        }
    }
}
