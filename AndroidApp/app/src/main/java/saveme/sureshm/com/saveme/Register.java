package saveme.sureshm.com.saveme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class Register extends AppCompatActivity {

    @InjectView(R.id.input_regname) EditText fname;
    @InjectView(R.id.input_regemail) EditText email;
    @InjectView(R.id.input_regpassword) EditText password;
    @InjectView(R.id.input_regrepassword) EditText repassword;
    @InjectView(R.id.input_regnic) EditText nic;

    @InjectView(R.id.btn_register) Button register;
    @InjectView(R.id.link_signin) TextView signinlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.inject(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        signinlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);

            }
        });
    }

    public void register()  {
        if(validate() == false){
            onRegFailed();
            return;
        }

        register.setEnabled(true);

        ProceedReg proceedReg = new ProceedReg();
        proceedReg.execute(fname.getText().toString(),email.getText().toString(),password.getText().toString(),nic.getText().toString());
    }

    class ProceedReg extends AsyncTask<String, String, String> {

        private ProgressDialog regprogress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            regprogress = new ProgressDialog(Register.this);
            regprogress.setIndeterminate(true);
            regprogress.setMessage("Processing...");
            regprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String regfname = params[0];
            String regemail = params[1];
            String regpass =  params[2];
            String regnic =  params[3];

            try{
                String link = "http://icts.stcmount.edu.lk/saveme/client.php?task=register&fname="+Uri.encode(regfname)+"&username="+Uri.encode(regemail)+"&password="+Uri.encode(regpass)+"&nic="+Uri.encode(regnic);
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
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            regprogress.dismiss();

            if(s.equalsIgnoreCase("success")){

                Toast.makeText(getBaseContext(), "You have been successfull registered! Please collect your SaveMe card from the nearest SaveMe agency!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);

            } else {
                Toast.makeText(Register.this, s, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onRegFailed() {
        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
        register.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email_in = email.getText().toString();
        String password_in = password.getText().toString();
        String repassword_in = repassword.getText().toString();
        String fname_in = fname.getText().toString();
        String nic_in = nic.getText().toString();

        if(repassword_in.isEmpty()){
            repassword.setError("Please repeat your password");
            valid = false;
        } else {
            if(!repassword_in.equalsIgnoreCase(password_in)){
                repassword.setError("Both passwords has to be the same");
                valid = false;
            } else {
                repassword.setError(null);
            }
        }

        if(nic_in.isEmpty()){
            nic.setError("Please enter your NIC number!");
            valid = false;
        }

        if(fname_in.isEmpty()){
            fname.setError("Please enter your name");
            valid = false;
        }

        if (email_in.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email_in).matches()) {
            email.setError("Invalid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        if (password_in.isEmpty() || password_in.length() < 4 || password_in.length() > 10) {
            password.setError("Between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

}