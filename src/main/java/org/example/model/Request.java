package org.example.model;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String action;
    private String method;
    private Object data;
    private String sessionId;
    private String token;

    public Request() {}

    public Request(String action, String method, Object data) {
        this.action = action;
        this.method = method;
        this.data = data;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public String toString() {
        return "Request{" +
                "action='" + action + '\'' +
                ", method='" + method + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}

