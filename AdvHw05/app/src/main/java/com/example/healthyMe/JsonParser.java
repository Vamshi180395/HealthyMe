package com.example.healthyMe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.researchstack.backbone.model.Choice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Rama Vamshi Krishna on 08/30/2017.
 */
public class JsonParser {


    static class ParseGenericResponseObject {
        static NetworkCallResponse doJsonParsing(String inputstring) throws JSONException {
            JSONObject rootobj = new JSONObject(inputstring);
            NetworkCallResponse resobj = new NetworkCallResponse();
            resobj.setStatus(rootobj.optString("status"));
            resobj.setSuccess(rootobj.optBoolean("success"));
            if (rootobj.optString("token") != null && rootobj.optString("token").length() > 1) {
                User userobj = new User();
                userobj.setUsertoken(rootobj.optString("token"));
                userobj.setUsername(rootobj.optString("username"));
                userobj.setFirstname(rootobj.optString("name"));
                userobj.setReceive_notifications(rootobj.optBoolean("notification"));
                resobj.setUser(userobj);
            }
            return resobj;
        }
    }

    static class ParseUserMessagesList {
        static ArrayList<Message> doJsonParsing(String inputstring) throws JSONException {
            ArrayList<Message> messages_list = new ArrayList<Message>();
            HashMap<String, String> answers_present = new HashMap<>();
            Set<String> survey_answers_present = new HashSet<>();
            JSONObject rootobj = new JSONObject(inputstring);
            JSONArray messages_array = rootobj.optJSONArray("messages");
            JSONArray responses_array = rootobj.optJSONArray("responses");
            JSONArray surveys_array = rootobj.optJSONArray("surveys");
            JSONArray surveys_responses_array= rootobj.optJSONArray("surveyresponses");
            if((messages_array != null && messages_array.length() > 0) || (surveys_array != null && surveys_array.length() > 0)) {
                if (messages_array != null && messages_array.length() > 0) {
                    // Parsing Existing Responses
                    if (responses_array != null && responses_array.length() > 0) {
                        for (int i = 0; i < responses_array.length(); i++) {
                            JSONObject response_obj = responses_array.optJSONObject(i);
                            if (!answers_present.containsKey(response_obj.optString("qid"))) {
                                answers_present.put(response_obj.optString("qid"), response_obj.optJSONArray("responses").optString(0));
                            }
                        }
                    }

                    // Parsing Informational & Simple Messages
                    for (int i = 0; i < messages_array.length(); i++) {
                        Message msg_obj = new Message();
                        JSONObject message_obj = messages_array.optJSONObject(i);
                        msg_obj.setUserHasReponded(false);
                        msg_obj.setMsg_id(message_obj.optString("mid"));
                        msg_obj.setMsg_txt(message_obj.optString("message"));
                        msg_obj.setPublishedDate(message_obj.optString("publishedtime"));
                        if (message_obj.optInt("questiontype") == 1) {
                            msg_obj.setType(1);
                        } else if (message_obj.optInt("questiontype") == 2) {
                            msg_obj.setType(2);
                            if (answers_present != null && answers_present.size() > 0) {
                                if (answers_present.containsKey(msg_obj.getMsg_id())) {
                                    msg_obj.setUserHasReponded(true);
                                    msg_obj.setMsg_answer(answers_present.get(msg_obj.getMsg_id().toString()));
                                }
                            }
                        }
                        ;
                        if (msg_obj.getPublishedDate() != null && (msg_obj.getType() == 1 || msg_obj.getType() == 2)) {
                            messages_list.add(msg_obj);
                        }
                    }
                }
                // Parsing Survey Messages
                if (surveys_array != null && surveys_array.length() > 0) {
                    if (surveys_responses_array != null && surveys_responses_array.length() > 0) {
                        for (int i = 0; i < surveys_responses_array.length(); i++) {
                            JSONObject response_obj = surveys_responses_array.optJSONObject(i);
                            if(!survey_answers_present.contains(response_obj.optString("sid"))) {
                                survey_answers_present.add(response_obj.optString("sid"));
                            }
                        }
                    }
                    for (int i = 0; i < surveys_array.length(); i++) {
                        Message sur_msg_obj = new Message();
                        JSONObject sur_message_obj = surveys_array.optJSONObject(i);
                        sur_msg_obj.setType(3);
                        sur_msg_obj.setMsg_id(sur_message_obj.optString("sid"));
                        sur_msg_obj.setMsg_txt(sur_message_obj.optString("survey"));
                        sur_msg_obj.setUserHasReponded(survey_answers_present.contains(sur_msg_obj.getMsg_id()));
                        sur_msg_obj.setPublishedDate(sur_message_obj.optString("publishedtime"));
                        if(sur_msg_obj.getPublishedDate()!=null) {
                            messages_list.add(sur_msg_obj);
                        }
                    }
                }

                return messages_list;
            }
            return null;
        }
    }

    static class ParseSurveyQuestionsList {
        static ArrayList<SurveyQuestion> doJsonParsing(String inputstring) throws JSONException {
            ArrayList<SurveyQuestion> survey_questions_list = new ArrayList<SurveyQuestion>();
            JSONObject rootobj = new JSONObject(inputstring);
            JSONArray survey_questions_array = rootobj.optJSONArray("questions");
            if (survey_questions_array != null && survey_questions_array.length() > 0) {
                for (int i = 0; i < survey_questions_array.length(); i++) {
                    SurveyQuestion survey_question = new SurveyQuestion();
                    JSONObject survey_question_obj = survey_questions_array.getJSONObject(i);
                    survey_question.setQuestion_id(survey_question_obj.optString("mid"));
                    survey_question.setQuestion_txt(survey_question_obj.optString("message"));
                    survey_question.setQuestion_type(survey_question_obj.optInt("questionsubtype"));
                    JSONArray options_array_json = survey_question_obj.optJSONArray("surveymulti");
                    JSONArray survey_range = survey_question_obj.optJSONArray("surveyrange");
                    if (survey_question.getQuestion_type() == 1 && options_array_json != null && options_array_json.length() > 0) {
                        Choice[] options_array = new Choice[options_array_json.length()];
                        for (int j = 0; j < options_array_json.length(); j++) {
                            options_array[j] = new Choice(options_array_json.optString(j), options_array_json.optString(j));
                        }
                        survey_question.setOptions_list(options_array);
                    } else if (survey_question.getQuestion_type() == 2 && survey_range!=null && survey_range.length()>1) {
                        int min = survey_range.optInt(0);
                        Choice[] options_array = new Choice[(survey_range.optInt(1) - survey_range.optInt(0))+1];
                        for(int k=0; k < options_array.length; k++){
                            options_array[k] = new Choice(min+"", min);
                            min++;
                        }
                        survey_question.setOptions_list(options_array);
                    }
                    survey_questions_list.add(survey_question);
                }
            }
            return  survey_questions_list;
        }
    }

}

