package com.talk2machines.phoneunlockchecker;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.talk2machines.phoneunlockchecker.api.User;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity{

    ProgressDialog progress;
    SharedPreferences prefs;
    String userid, response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        final EditText ln = (EditText) findViewById(R.id.loginName);
        final EditText lun = (EditText) findViewById(R.id.loginUsername);
        final Button lb = (Button) findViewById(R.id.loginbutton);


        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);

        prefs = getSharedPreferences("PUC", 0);
        final String reg_id=prefs.getString("REG_ID", "");


        lb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                    // test, ob die name und username schon eingetippen sind.
                    if(ln.getText().toString().equals("") || lun.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), R.string.loginerror, Toast.LENGTH_LONG).show();
                    }else{
                        progress.show();
                        User newUser = new User(ln.getText().toString().trim(),lun.getText().toString().trim(), reg_id);
                        newUser.login(getApplicationContext(), new User.VolleyCallback() {
                            @Override
                            public void onSuccess(JSONObject result) {
                                Log.i("Login", result.toString());
                                try {
                                    response = result.getString("response");
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    userid = result.getString("id");
                                    //test ob ein userid zurückbekommen, wenn ja, speichern userid in sharePreferences, und leitet zu ListActivity
                                    if(userid !=  null ){
                                        Log.i("Login", userid);
                                        prefs = getSharedPreferences("PUC", 0);
                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.putString("LOG_ID", userid);
                                        edit.commit();

                                        Intent intent = new Intent();
                                        intent.setClass(LoginActivity.this, ListActivity.class);
                                        startActivity(intent);

                                        //TODO move next block to other activity or service
                                        PackageManager pm  = LoginActivity.this.getPackageManager();
                                        ComponentName componentName = new ComponentName(LoginActivity.this, UnlockReceiver.class);
                                        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                PackageManager.DONT_KILL_APP);
                                        Toast.makeText(getApplicationContext(), "activated", Toast.LENGTH_LONG).show();

                                        finish();

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progress.hide();
                            }

                            @Override
                            public void onError(JSONObject result) {
                                Log.i("Login", result.toString());
                                try {
                                    response = result.getString("response");
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progress.hide();
                            }
                        });

                    }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    protected void onRestart() {
        super.onRestart();


    }


    @Override
    protected void onResume() {
        super.onResume();


    }


    @Override
    protected void onPause() {

        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

}
