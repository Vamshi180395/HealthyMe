package com.example.healthyMe;

import java.io.Serializable;

/**
 * Created by Rama Vamshi Krishna on 11/14/2017.
 */

public class User  implements Serializable {
    private String firstname, username, password, gender, age, usertoken;
    private Boolean receive_notifications;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String firstname, String username, String usertoken, Boolean receive_notifications) {
        this.firstname = firstname;
        this.username = username;
        this.usertoken = usertoken;
        this.receive_notifications = receive_notifications;
    }

    public User(String firstname, String username, String password, String gender, String age, Boolean receive_notifications, String usertoken) {
        this.firstname = firstname;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.age = age;
        this.receive_notifications = receive_notifications;
        this.usertoken = usertoken;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Boolean getReceive_notifications() {
        return receive_notifications;
    }

    public void setReceive_notifications(Boolean receive_notifications) {
        this.receive_notifications = receive_notifications;
    }

    public String getUsertoken() {
        return usertoken;
    }

    public void setUsertoken(String usertoken) {
        this.usertoken = usertoken;
    }
}
