package com.example.healthyMe;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import org.json.JSONException;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private EditText ed_username, ed_password;
    private CheckBox cb_rememberMe;
    private LinearLayout coordinatorLayout;
    private SharedPreferences sharedPreferences;
    private static User current_user;
    private NetworkCallResponse api_response;
    private OkHttpClient client;
    private Request request;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewByIds();
        showDetailsIfUserIsRemembered();
        registerReceiver();
        if (getCurrentUserSession() != null) {
            goToHomeActivity();
        }
    }

    private void callGCMTokenRegistrationService() {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private User getCurrentUserSession() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_user_json = sharedPreferences.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        current_user = gson.fromJson(current_user_json,User.class);
        if (current_user != null && current_user.getUsertoken()!=null && current_user.getUsertoken().length() >= 1) {
            return current_user;
        }
        return null;
    }

    private void showDetailsIfUserIsRemembered() {
        sharedPreferences = getSharedPreferences("LOGIN_PREFERENCES", MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.getBoolean("LOGIN_SAVED", false)) {
            cb_rememberMe.setChecked(true);
            ed_username.setText(sharedPreferences.getString("USERNAME", "").toString());
            ed_password.setText(sharedPreferences.getString("PASSWORD", ""));
        }
    }

    private void findViewByIds() {
        ed_username = (EditText) findViewById(R.id.ed_username);
        ed_password = (EditText) findViewById(R.id.ed_password);
        cb_rememberMe = (CheckBox) findViewById(R.id.cb_rememberMe);
        coordinatorLayout = (LinearLayout) findViewById(R.id.main_layout_id);
    }

    public void loginUser(View view) {
        if (TextUtils.isEmpty(ed_username.getText().toString().trim())) {
            displaySnack("Invalid Username..");
        } else if (TextUtils.isEmpty(ed_password.getText().toString().trim())) {
            displaySnack("Invalid Password..");
        } else if (ed_password.getText().toString().trim().length() < 0) {
            displaySnack("Invalid Password..");
        } else {
            if(isConnectedtoInternet()) {
                showProgress();
                updateLoginPreferences();
                loginUserOnToServer(new User(ed_username.getText().toString(), ed_password.getText().toString()));
            }
            else{
                displaySnack("No Network Available..");
            }
        }
    }

    private void loginUserOnToServer(User user_trying_to_login) {
        client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "username=" + user_trying_to_login.getUsername().toString()
                + "&password=" + user_trying_to_login.getPassword().toString());
        request = new Request.Builder()
                .url(getURLForUserLogin())
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displaySnack("Unexpected Failure. Please try again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            displaySnack("Unexpected Failure. Please try again.");
                        }
                    });

                } else {
                    try {
                        api_response = JsonParser.ParseGenericResponseObject.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (api_response != null) {
                                if (api_response.getSuccess() && api_response.getUser()!=null) {
                                    updateSharedPreferencesAboutUserLogin();
                                    callGCMTokenRegistrationService();
                                    progressDialog.dismiss();
                                    goToHomeActivity();

                                } else {
                                    progressDialog.dismiss();
                                    displaySnack("Login Failed." + api_response.getStatus().toString());
                                }
                            } else {
                                progressDialog.dismiss();
                                displaySnack("Login Failed.");
                            }
                        }
                    });

                }
            }
        });
    }

    private String getURLForUserLogin() {
        return getResources().getString(R.string.API_URL)+"login";
    }

    private void updateSharedPreferencesAboutUserLogin() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        User current_loggedin_user = new User(api_response.getUser().getFirstname().toString(),api_response.getUser().getUsername().toString(),api_response.getUser().getUsertoken().toString(),api_response.getUser().getReceive_notifications());
        Gson gson = new Gson();
        String json = gson.toJson(current_loggedin_user);
        editor.putString("CurrentUserSession", json);
        editor.commit();
        current_user = current_loggedin_user;
    }

    private void updateLoginPreferences() {
        if (cb_rememberMe.isChecked()) {
            rememberUser(ed_username.getText().toString(), ed_password.getText().toString());
        } else {
            forgetUser();
        }
    }

    private void goToHomeActivity() {
        Intent intnt = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intnt);
        overridePendingTransition(R.anim.from_fade, R.anim.to_fade);
        finish();
    }

    private void displaySnack(String s) {
        Snackbar.make(coordinatorLayout, s, Snackbar.LENGTH_LONG).show();
    }

    public void rememberUser(String username, String password) {
        getSharedPreferences("LOGIN_PREFERENCES", MODE_PRIVATE)
                .edit()
                .putBoolean("LOGIN_SAVED", true)
                .putString("USERNAME", username)
                .putString("PASSWORD", password)
                .commit();
    }

    public void forgetUser() {
        getSharedPreferences("LOGIN_PREFERENCES", MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    public void userForgotPassword(View view) {

    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(GCMPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private  void showProgress(){
        progressDialog = new ProgressDialog(LoginActivity.this,R.style.MyProgressDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("TAG", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private boolean isConnectedtoInternet() {
        ConnectivityManager cm= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni= cm.getActiveNetworkInfo();
        if(ni!=null) return true;
        return false;
    }
}
