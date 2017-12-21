package com.example.healthyMe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;
    private SharedPreferences sharedpref;
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getPreviousSessionTokenFronSharedPreferences();
        showSplashScreenAndMoveOn();
    }


    private void showSplashScreenAndMoveOn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (current_user!=null && current_user.getUsertoken()!=null) {
                    goToHomeActivity();
                } else {
                    goToLoginActivity();
                }
            }
        }, SPLASH_TIME_OUT);
    }


    public User getPreviousSessionTokenFronSharedPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_user_json = sharedpref.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        current_user = gson.fromJson(current_user_json, User.class);
        if (current_user != null && current_user.getUsertoken()!=null && current_user.getUsertoken().length()>0) {
            return current_user;
        }
        return null;
    }

    private void goToHomeActivity() {
        Intent intnt = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intnt);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void goToLoginActivity() {
        Intent intnt = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intnt);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

}
