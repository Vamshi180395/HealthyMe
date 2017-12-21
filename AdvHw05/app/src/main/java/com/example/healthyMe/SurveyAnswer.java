package com.example.healthyMe;

/**
 * Created by Rama Vamshi Krishna on 12/05/2017.
 */

public class SurveyAnswer {
    private String qid,response;

    public SurveyAnswer(String question_id, String answer) {
        this.qid = question_id;
        this.response = answer;
    }

    public String getQuestion_id() {
        return qid;
    }

    public void setQuestion_id(String question_id) {
        this.qid = question_id;
    }

    public String getAnswer() {
        return response;
    }

    public void setAnswer(String answer) {
        this.response = answer;
    }
}
