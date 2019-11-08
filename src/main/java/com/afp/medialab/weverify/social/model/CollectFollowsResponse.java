package com.afp.medialab.weverify.social.model;

public class CollectFollowsResponse {

    String session;
    String users;
    private Status status;

    public CollectFollowsResponse(String session, String users, Status status)
    {
        this.session = session;
        this.users = users;
        this.status = status;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
