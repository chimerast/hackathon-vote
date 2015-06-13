package org.e2d3;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

public class UserData extends WebSession {

    public static UserData get() {
        return (UserData) Session.get();
    }

    private String userId;

    public UserData(Request request) {
        super(request);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

}
