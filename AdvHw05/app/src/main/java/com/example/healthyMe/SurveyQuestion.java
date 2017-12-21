package com.example.healthyMe;

import org.researchstack.backbone.model.Choice;

/**
 * Created by Rama Vamshi Krishna on 12/04/2017.
 */

public class SurveyQuestion {
    private String question_id, question_txt;
    private int question_type;
    private Choice[] options_list;
    private Boolean isOptional;

    public SurveyQuestion(){
        this.isOptional = false;
    }

    public SurveyQuestion(String question_id, String question_txt, int question_type, Choice[] options_list, Boolean isOptional) {
        this.question_id = question_id;
        this.question_txt = question_txt;
        this.question_type = question_type;
        this.options_list = options_list;
        this.isOptional = isOptional;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_txt() {
        return question_txt;
    }

    public void setQuestion_txt(String question_txt) {
        this.question_txt = question_txt;
    }

    public int getQuestion_type() {
        return question_type;
    }

    public void setQuestion_type(int question_type) {
        this.question_type = question_type;
    }

    public Choice[] getOptions_list() {
        return options_list;
    }

    public void setOptions_list(Choice[] options_list) {
        this.options_list = options_list;
    }

    public Boolean getOptional() {
        return isOptional;
    }

    public void setOptional(Boolean optional) {
        isOptional = optional;
    }
}
