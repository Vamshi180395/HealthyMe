package com.example.healthyMe;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Rama Vamshi Krishna on 11/14/2017.
 */

public class Message implements Comparable<Message>{
    private String msg_txt, msg_answer, msg_id;
    private int type;
    private Date publishedDate;
    private Boolean userHasReponded;

    public Message() {

    }

    public Message(int type, String msg_txt, String msg_answer, Boolean userHasReponded, String msg_id) {
        this.type = type;
        this.msg_txt = msg_txt;
        this.msg_answer = msg_answer;
        this.userHasReponded = userHasReponded;
        this.msg_id = msg_id;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg_txt() {
        return msg_txt;
    }

    public void setMsg_txt(String msg_txt) {
        this.msg_txt = msg_txt;
    }

    public String getMsg_answer() {
        return msg_answer;
    }

    public void setMsg_answer(String msg_answer) {
        this.msg_answer = msg_answer;
    }

    public Boolean getUserHasReponded() {
        return userHasReponded;
    }

    public void setUserHasReponded(Boolean userHasReponded) {
        this.userHasReponded = userHasReponded;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(publishedDate));
            cal.add(Calendar.HOUR, -5);
            this.publishedDate = cal.getTime();
        } catch (ParseException ex) {
            Log.d("String Parsing Failed :", ex.toString());
        }
    }

    @Override
    public int compareTo(@NonNull Message o) {
         return (o.getPublishedDate().compareTo(this.getPublishedDate()));
    }
}
