package com.example.healthyMe;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
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


/**
 * Created by Rama Vamshi Krishna on 11/19/2017.
 */

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private NetworkCallResponse api_response;
    private SharedPreferences sharedPreferences;
    public RegistrationIntentService() {
        super(TAG);
    }
    private User current_user;

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d("Matcha", "GCM Registration Token: " + token);
            sendGCMTokenToServer(token);
            subscribeTopics(token);
            sharedPreferences.edit().putBoolean(GCMPreferences.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putString("currentgcmtoken", null).apply();
            sharedPreferences.edit().putBoolean(GCMPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void getCurrentUserSessionDetailsFronSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_user_json = sharedPreferences.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        if(current_user_json!=null){ current_user = gson.fromJson(current_user_json,User.class);}
    }


    private void sendGCMTokenToServer(final String token) {
        getCurrentUserSessionDetailsFronSharedPreferences();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "devicetoken="+token);
        Request request = new Request.Builder()
                .url(getURLForSendingGCMToken())
                .post(body)
                .addHeader("token", current_user.getUsertoken())
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
                    sharedPreferences.edit().putString("currentgcmtoken", null).apply();
                } else {
                    try {
                        api_response = JsonParser.ParseGenericResponseObject.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (api_response != null) {
                        if (api_response.getSuccess()) {
                            sharedPreferences.edit().putString("currentgcmtoken", token).apply();
                        } else {
                            sharedPreferences.edit().putString("currentgcmtoken", null).apply();
                        }
                    } else {
                        sharedPreferences.edit().putString("currentgcmtoken", null).apply();
                    }

                }
            }
        });

    }


    private String getURLForSendingGCMToken() {
        return getResources().getString(R.string.API_URL)+"savetoken";
    }


    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }


}
