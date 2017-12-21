package com.example.healthyMe;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, HomeFragment.OnHomeFragmentInteractionListener, ProfileFragment.OnProfileFragmentInteractionListener {
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView nav_header_name;
    private User current_user;
    private SharedPreferences sharedpref;
    private NetworkCallResponse api_response;
    private String device_token;
    static String  selected_survey_id = "";
    private ViewFlipper viewFlipper;  private static int FLIP_TIME_OUT = 3000;
    private Bundle savedInstanceState; private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUpMyToolBar();
        findViewByIdsAndHandleNavigation();
        getCurrentUserSessionDetailsFronSharedPreferences();
        setNavigationHeaderValues();
        startHomeFragment();
    }

    private void getCurrentUserSessionDetailsFronSharedPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_user_json = sharedpref.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        if(current_user_json!=null){ current_user = gson.fromJson(current_user_json,User.class);}
        device_token = sharedpref.getString("currentgcmtoken", "");
    }

    private void setNavigationHeaderValues() {
        if(current_user!=null) {
            nav_header_name.setText("Welcome Back "+current_user.getFirstname()+"...");
        }
    }

    public String getEmijoByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    private void startHomeFragment() {
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new HomeFragment(), "Home_Fragment").commit();
    }

    private void setUpMyToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void findViewByIdsAndHandleNavigation() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        nav_header_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_user_firstname);
        viewFlipper = (ViewFlipper) navigationView.getHeaderView(0).findViewById(R.id.viewFlipper);
        viewFlipper.startFlipping();
        viewFlipper.setFlipInterval(FLIP_TIME_OUT);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int count_of_fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        };
        if (count_of_fragments > 0) {
            getSupportFragmentManager().popBackStack();
        }else {
                if (doubleBackToExitPressedOnce) {
                    finish();
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit app..", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            reloadCurrentFragment();
            displaySnack("Screen Refreshed.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadCurrentFragment() {
        getSupportFragmentManager().beginTransaction().detach(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).attach(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_coach) {
            moveToHomeFragment();
        } else if (id == R.id.nav_profile) {
            moveToProfileFragment();
        } else if (id == R.id.nav_logout) {
            removeUserGCMDetailsFromServer();
            logOutUser();
        }
        else{
            helpUsersInSendingTheirFeedback();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void helpUsersInSendingTheirFeedback() {
        Intent Email = new Intent(Intent.ACTION_SENDTO);
        Email.setData(Uri.parse("mailto:"));
        Email.putExtra(Intent.EXTRA_EMAIL, new String[] {getString(R.string.mail_feedback_email)});
        Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        Email.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_feedback_text));
        if (Email.resolveActivity(getPackageManager()) != null) {
            startActivity(Email);
        }

    }


    private void moveToHomeFragment() {
        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container).getTag().equalsIgnoreCase("Home_Fragment")) {
            reloadCurrentFragment();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment(), "Home_Fragment").addToBackStack(null).commit();
        }

    }

    private void moveToProfileFragment() {
        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container).getTag().equalsIgnoreCase("Profile_Fragment")) {
          reloadCurrentFragment();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(), "Profile_Fragment").addToBackStack(null).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public void logOutUser() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString("CurrentUserSession", null);
        editor.commit();
        goBackToLoginActivity();
    }

    private void goBackToLoginActivity() {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void removeUserGCMDetailsFromServer() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "devicetoken="+device_token);
        Request request = new Request.Builder()
                .url(getURLToRemoveGCMDetails())
                .post(body)
                .addHeader("token", current_user.getUsertoken().toString())
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {

                } else {
                    try {
                        api_response = JsonParser.ParseGenericResponseObject.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    private String getURLToRemoveGCMDetails() {
        return getResources().getString(R.string.API_URL)+"logout";
    }
    private void displaySnack(String s) {
        Snackbar.make(drawer, s, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restartThisActivity();
    }

    private void restartThisActivity() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }

}
