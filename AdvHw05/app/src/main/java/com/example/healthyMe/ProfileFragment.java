package com.example.healthyMe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

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

import static android.content.Context.CONNECTIVITY_SERVICE;


public class ProfileFragment extends Fragment {
    private User current_user; private SharedPreferences sharedpref;
    private TextView txtname,txtusername;
    private Switch toggle; private FrameLayout root_layout;
    private NetworkCallResponse api_response;
    private ProgressDialog progressDialog;
    private OnProfileFragmentInteractionListener mListener;
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileFragmentInteractionListener) {
            mListener = (OnProfileFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean isConnectedtoInternet() {
        ConnectivityManager cm= (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni= cm.getActiveNetworkInfo();
        if(ni!=null) return true;
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        getCurrentUserSessionDetailsFromSharedPreferences();
        if(current_user!=null) {
            setSwitchStatusAndOtherSettings();
            fillTextViewsWithUserDetails();
        }
        else{
            displaySnack("Failed to Retrieve your profile.");
        }
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isConnectedtoInternet() && current_user!=null) {
                    showProgress();
                    updateNotificationsChoiceToServer(isChecked);
                }
                else {
                    toggle.setChecked(!isChecked);
                    displaySnack("No Network Connection.");
                }
            }
        });
    }

    private void fillTextViewsWithUserDetails() {
        txtname.setText(current_user.getFirstname().toString());
        txtusername.setText(current_user.getUsername().toString());
    }

    private void getCurrentUserSessionDetailsFromSharedPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String current_user_json = sharedpref.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        if(current_user_json!=null){ current_user = gson.fromJson(current_user_json,User.class);}
    }

    private void updateNotificationsChoiceToServer(final boolean isChecked) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "notification="+isChecked);
        Request request = new Request.Builder()
                .url(getURLForUpdatingNotificationsSettings())
                .post(body)
                .addHeader("token", current_user.getUsertoken())
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyUpdateFailure(isChecked);
                        }
                    });
                }

            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyUpdateFailure(isChecked);
                            }
                        });
                    }

                } else {
                    try {
                        api_response = JsonParser.ParseGenericResponseObject.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (api_response != null) {
                                    if (api_response.getSuccess()) {
                                        updateCurrentUserSessionAboutNotificationSetting(isChecked);
                                    } else {
                                        notifyUpdateFailure(isChecked);
                                    }
                                } else {
                                    notifyUpdateFailure(isChecked);
                                }
                            }
                        });
                    }

                }
            }
        });
    }

    private void notifyUpdateFailure(Boolean isChecked) {
        progressDialog.dismiss();
        toggle.setChecked(!isChecked);
        displaySnack("Sorry. Please try again..");
    }

    private void updateCurrentUserSessionAboutNotificationSetting(boolean isChecked) {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        SharedPreferences.Editor editor = sharedpref.edit();
        current_user.setReceive_notifications(isChecked);
        Gson gson = new Gson();
        String json = gson.toJson(current_user);
        editor.putString("CurrentUserSession", json);
        editor.commit();
        progressDialog.dismiss();
        if(isChecked) displaySnack("Notifications Enabled."); else displaySnack("Notifications Disabled.");
    }

    private String getURLForUpdatingNotificationsSettings() {
        return getResources().getString(R.string.API_URL)+"notification";
    }

    private void setSwitchStatusAndOtherSettings() {
        toggle.setChecked(current_user.getReceive_notifications());
    }

    private void findViewByIds() {
        toggle = (Switch) getActivity().findViewById(R.id.switch1);
        txtname = (TextView) getActivity().findViewById(R.id.txtname);
        txtusername = (TextView) getActivity().findViewById(R.id.txtusername);
        root_layout = (FrameLayout) getActivity().findViewById(R.id.profile_frame_layout);

    }

    public interface OnProfileFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void displaySnack(String s) {
        Snackbar.make(root_layout, s, Snackbar.LENGTH_LONG).show();
    }

    private  void showProgress(){
        progressDialog = new ProgressDialog(getActivity(),R.style.MyProgressDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }


}
