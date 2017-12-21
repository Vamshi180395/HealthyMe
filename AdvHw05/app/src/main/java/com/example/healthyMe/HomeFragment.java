package com.example.healthyMe;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.USAGE_STATS_SERVICE;

public class HomeFragment extends Fragment implements MessagesAdapter.MessagesAdapterInterface {

    private RecyclerView messages_view;
    private ArrayList<Message> list_of_all_messages;
    private SharedPreferences sharedpref;
    private User current_user;
    private NetworkCallResponse api_response;
    private Request request;
    private OkHttpClient client;
    private ProgressBar pb_messsages_loading; FrameLayout root_layout;
    private MessagesAdapter msgadapter;
    public static int USER_JUST_SIGNEDIN = 0;

    private OnHomeFragmentInteractionListener mListener;

    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
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
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIDs();
        getCurrentSessionDetailsFronSharedPreferences();
        checkIfUserHasJustLoggedInAndInformHim();
        if(current_user!=null) setRecycleViewWithMessages();
    }

    private void checkIfUserHasJustLoggedInAndInformHim() {
        if(USER_JUST_SIGNEDIN == 0){
            displaySnack("Signed in as "+current_user.getUsername());
            USER_JUST_SIGNEDIN  = 1;
        }
    }

    private void getCurrentSessionDetailsFronSharedPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String current_user_json = sharedpref.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        if(current_user_json!=null){ current_user = gson.fromJson(current_user_json,User.class);}
    }

    private void setRecycleViewWithMessages() {
        list_of_all_messages = new ArrayList<Message>();
        pb_messsages_loading.setVisibility(View.VISIBLE);
        if (isConnectedtoInternet() && current_user!=null){
            getAllMessagesForCurrentUser();
            }
    else {
            displaySnack("No Network Available..");
        }
    }

    private boolean isConnectedtoInternet() {
        ConnectivityManager cm= (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni= cm.getActiveNetworkInfo();
        if(ni!=null) return true;
        return false;
    }

    private void setAdapterAndLayoutsForRecycleView() {
        Collections.sort(list_of_all_messages);
        msgadapter = new MessagesAdapter(getActivity(), (HomeActivity) getActivity(), list_of_all_messages);
        messages_view.setAdapter(msgadapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);
        messages_view.setLayoutManager(layoutManager);
    }

    private void getAllMessagesForCurrentUser() {
        client = new OkHttpClient();
        request = new Request.Builder()
                .url(getURLForRetrievingUserMessages())
                .get()
                .addHeader("token", current_user.getUsertoken().toString())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    removeExistingUserTokenDetailsFromPreferences();
                    goBackToLoginActivity();
                } else {
                    try {
                        list_of_all_messages = JsonParser.ParseUserMessagesList.doJsonParsing(response.body().string());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (list_of_all_messages != null && list_of_all_messages.size() > 0) {
                                    pb_messsages_loading.setVisibility(View.INVISIBLE);
                                    setAdapterAndLayoutsForRecycleView();
                                } else {
                                    pb_messsages_loading.setVisibility(View.INVISIBLE);
                                    displaySnack("Your inbox is empty.");
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void removeExistingUserTokenDetailsFromPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString("MyPreviousSession", null);
        editor.putString("previoususername", null);
        editor.commit();
    }

    private void goBackToLoginActivity() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private String getURLForRetrievingUserMessages() {
        return getResources().getString(R.string.API_URL)+"getMessages";
    }

    private void findViewByIDs() {
        messages_view = (RecyclerView) getActivity().findViewById(R.id.recycle_messages_view);
        pb_messsages_loading = (ProgressBar) getActivity().findViewById(R.id.pb_messages_loading);
        root_layout = (FrameLayout) getActivity().findViewById(R.id.home_frame_layout);
    }

    @Override
    public void submitResponseForSimpleQuestion(String selected_msg_id, String answer) {
        if(answer!=null && answer.length()>1 && isConnectedtoInternet()) postAnswerToServer(selected_msg_id,answer);
        else if (answer == null) displaySnack("Please select a valid answer");
        else displaySnack("No Network Available.");
    }

    @Override
    public void startSurveyForSurveyQuestion(String selected_msg_id) {
        if(selected_msg_id!=null){
            HomeActivity.selected_survey_id = selected_msg_id;
        };
        moveToSurveyActivity();
    }

    private void moveToSurveyActivity() {
        Intent i = new Intent(getActivity(),SurveyActivity.class);
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        getActivity().finish();
    }

    private void postAnswerToServer(String selected_msg_id, String answer) {
        client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "mid="+selected_msg_id+"&answer="+answer);
        request = new Request.Builder()
                .url(getURLToSubmitUsersAnswer())
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
                    throw new IOException("Unexpected failure. Reason:" + response);
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
                                        displaySnack("Your response has been recorded.");
                                        setRecycleViewWithMessages();
                                    } else {
                                        displaySnack("Sending Response Failed." + api_response.getStatus().toString());
                                    }
                                } else {
                                    displayToast("Your Response could not be saved. Please try after some time.");
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public interface OnHomeFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private String getURLToSubmitUsersAnswer() {
        return getResources().getString(R.string.API_URL)+"submitResponse";
    }

    private void displayToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    private void displaySnack(String s) {
        Snackbar.make(root_layout, s, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query!=null && query.trim().length()>0 && list_of_all_messages!=null && list_of_all_messages.size()>0){
                    final List<Message> filteredModelList = filterMessagesAccordingToUserSearch(query);
                    if(filteredModelList==null || filteredModelList.size()<1){
                        displaySnack("No Matches found for "+query+".");
                        return true;
                    }
                    msgadapter.setFilter(filteredModelList);
                    displaySnack("Showing results for "+query+".");
                }
                else if((query==null || query.trim().length()<1) && list_of_all_messages!=null && list_of_all_messages.size()>0){
                    msgadapter.setFilter(list_of_all_messages);
                    displaySnack("Enter a valid search query.");
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText!=null && newText.trim().length()>0 && list_of_all_messages!=null && list_of_all_messages.size()>0){
                    final List<Message> filteredModelList = filterMessagesAccordingToUserSearch(newText);
                    msgadapter.setFilter(filteredModelList);
                }else if((newText==null || newText.trim().length()<1) && list_of_all_messages!=null && list_of_all_messages.size()>0){
                    msgadapter.setFilter(list_of_all_messages);
                }
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                msgadapter.setFilter(list_of_all_messages);
                return true;
            }
        });
    }

    private List<Message> filterMessagesAccordingToUserSearch(String newText) {
        final List<Message> filteredModelList = new ArrayList<>();
        for (Message msg : list_of_all_messages) {
            if (msg.getMsg_txt().toLowerCase().contains(newText.toLowerCase())) {
                filteredModelList.add(msg);
            }
        }
        return filteredModelList;
    }



}
