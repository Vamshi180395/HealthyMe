package com.example.healthyMe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SurveyActivity extends AppCompatActivity {
    private String selected_survey_id;
    private ArrayList<SurveyQuestion> list_of_survey_questions;
    private User current_user;
    private SharedPreferences sharedpref;
    private NetworkCallResponse api_response;
    private static final int SURVEY_SUBMISSION = 2;
    private static final int SURVEY_SUMMUARY_SUBMISSION = 3;
    private ArrayList<SurveyAnswer> list_of_survey_answers;
    private Button btn_start_survey; private ProgressDialog  progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        findViewByIds();
        selected_survey_id = HomeActivity.selected_survey_id;
        getCurrentUserSessionDetailsFronSharedPreferences();
        btn_start_survey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSelectedSurveyForCurrentUser();
            }
        });
    }
    private void findViewByIds() {
        btn_start_survey = (Button) findViewById(R.id.survey_button);
    }

    public void startSelectedSurveyForCurrentUser(){
        if(current_user!=null && isConnectedtoInternet()) {
            showProgress();
            getSurveyQuestionForGivenSurveyID();
        }
        else{
            displayToast("No Connection Available...");
        }
    }

    private void showSurveyQuestions() {
        List<Step> steps = new ArrayList<>(); AnswerFormat ans_format; QuestionStep questionStep;
        for (SurveyQuestion current_survey_question:list_of_survey_questions){
            switch (current_survey_question.getQuestion_type()) {
                case 0:
                    questionStep = new QuestionStep(current_survey_question.getQuestion_id(), current_survey_question.getQuestion_txt(), new TextAnswerFormat());
                    questionStep .setStepTitle(R.string.back);
                    questionStep.setOptional(current_survey_question.getOptional());
                    steps.add(questionStep);
                    break;
                case 1:
                    ans_format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle
                            .SingleChoice, current_survey_question.getOptions_list());
                    questionStep = new QuestionStep(current_survey_question.getQuestion_id(), current_survey_question.getQuestion_txt(), ans_format);
                    questionStep .setStepTitle(R.string.back);
                    questionStep.setOptional(current_survey_question.getOptional());
                    steps.add(questionStep);
                    break;
                case 2:
                    ans_format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle
                            .SingleChoice, current_survey_question.getOptions_list());
                    questionStep = new QuestionStep(current_survey_question.getQuestion_id(), current_survey_question.getQuestion_txt(), ans_format);
                    questionStep .setStepTitle(R.string.back);
                    questionStep.setOptional(current_survey_question.getOptional());
                    steps.add(questionStep);
                    break;
                case 3:
                    ans_format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle
                            .MultipleChoice,current_survey_question.getOptions_list());
                    questionStep = new QuestionStep(current_survey_question.getQuestion_id(), current_survey_question.getQuestion_txt(), ans_format);
                    questionStep .setStepTitle(R.string.back);
                    questionStep.setOptional(current_survey_question.getOptional());
                    steps.add(questionStep);
                    break;
                default:
                    break;
            }
        }
        OrderedTask task = new OrderedTask(selected_survey_id, steps);
        Intent intent = ViewTaskActivity.newIntent(SurveyActivity.this, task);
        startActivityForResult(intent, SURVEY_SUBMISSION);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            processSurveyResult((TaskResult) data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT), requestCode);
        }

    }

    private void processSurveyResult(TaskResult task_result, int requestcode)
    {
        if(requestcode == SURVEY_SUBMISSION){
            calculateAndSaveResults(task_result);
            if(list_of_survey_answers!=null && list_of_survey_answers.size()>0){
                showProgress();
                postSurveyAnswersToServer(list_of_survey_answers);
            }
            else{
                displayToast("Something Went Wrong. Please try again.");
            }
        }
    }

    private void calculateAndSaveResults(TaskResult task_result) {
        list_of_survey_answers = new ArrayList<>();
        for(String id : task_result.getResults().keySet())
        {
            StepResult stepResult = task_result.getStepResult(id);
            if(stepResult!=null && stepResult.getResult()!=null) {
                list_of_survey_answers.add(new SurveyAnswer(id,stepResult.getResult().toString()));
            }
        }
    }

    private boolean isConnectedtoInternet() {
        ConnectivityManager cm= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni= cm.getActiveNetworkInfo();
        if(ni!=null) return true;
        return false;
    }

    private void getCurrentUserSessionDetailsFronSharedPreferences() {
        sharedpref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_user_json = sharedpref.getString("CurrentUserSession", "");
        Gson gson = new Gson();
        if(current_user_json!=null){ current_user = gson.fromJson(current_user_json,User.class);}
    }

    private void getSurveyQuestionForGivenSurveyID() {
        list_of_survey_questions = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getURLForRetrievingSurveyQuestions())
                .get()
                .addHeader("token", current_user.getUsertoken().toString())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
             SurveyActivity.this.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     progressDialog.dismiss();
                     displayToast("Something Went wrong. Retry later.");
                 }
             });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    SurveyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            displayToast("Something Went wrong. Retry later.");
                        }
                    });
                }
                else{
                    try {
                        list_of_survey_questions = JsonParser.ParseSurveyQuestionsList.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SurveyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(list_of_survey_questions!=null && list_of_survey_questions.size()>0){
                                progressDialog.dismiss();
                                showSurveyQuestions();
                            }
                            else{
                                progressDialog.dismiss();
                                displayToast("Survey Unavailable. Retry Later");
                            }
                        }
                    });
                }
            }
        });
    }

    private String getURLForRetrievingSurveyQuestions() {
        return getResources().getString(R.string.API_URL)+"showQuestions?sid="+HomeActivity.selected_survey_id;
    }

    public interface OnSurveyFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void displayToast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

    private void postSurveyAnswersToServer(ArrayList<SurveyAnswer> list_of_survey_answers) {
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, gson.toJson(list_of_survey_answers));
        Log.d("Hello", gson.toJson(list_of_survey_answers) );
        Request request = new Request.Builder()
                .url(getURLForSubmittingSurveyAnswers())
                .post(body)
                .addHeader("token", current_user.getUsertoken().toString())
                .addHeader("content-type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SurveyActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayToast("Network Error. Please try later");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    SurveyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                progressDialog.dismiss();
                                displayToast("Network Error. Please try later");
                        }
                    });
                } else {
                    try {
                        api_response = JsonParser.ParseGenericResponseObject.doJsonParsing(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SurveyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (api_response != null){
                                if(api_response.getSuccess()) {
                                    progressDialog.dismiss();
                                    displayToast("Responses Saved. Thank you for your participation.");
                                    goToHomeActivity();
                                } else {
                                    progressDialog.dismiss();
                                    displayToast("Responses could not be saved." + api_response.getStatus().toString());
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                displayToast("Could not save responses. Please Retry..");
                            }
                        }
                    });

                }
            }
        });
    }

    private void goToHomeActivity() {
        Intent i = new Intent(this,HomeActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }

    private String getURLForSubmittingSurveyAnswers() {
        return getResources().getString(R.string.API_URL)+"submitAnswers?sid="+HomeActivity.selected_survey_id;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToHomeActivity();
    }

    private  void showProgress(){
        progressDialog = new ProgressDialog(SurveyActivity.this,R.style.MyProgressDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
    }

}
