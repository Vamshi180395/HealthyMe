package com.example.healthyMe;

/**
 * Created by Rama Vamshi Krishna on 08/30/2017.
 */
public class NetworkCallResponse {
    private String status;
    private User user;
    private Boolean success;

    public NetworkCallResponse() {
    }

    public NetworkCallResponse(String status, Boolean success) {
        this.status = status;
        this.success = success;
    }

    public NetworkCallResponse(String status, User user, Boolean success) {
        this.status = status;
        this.user = user;
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
