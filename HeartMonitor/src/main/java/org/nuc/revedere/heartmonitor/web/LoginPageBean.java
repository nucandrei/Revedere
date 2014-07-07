package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.service.core.JMSRequestor;
import org.nuc.revedere.service.core.Topics;

@ManagedBean(name = "login")
@SessionScoped
public class LoginPageBean implements Serializable {
    private static final String CURRENT_PAGE = "login";
    private static final long serialVersionUID = 4068393892351343091L;
    private String errorMessage = "";

    private final HeartMonitor heartMonitor;
    private String username;
    private String password;
    private boolean isLoggedIn = false;

    public LoginPageBean() {
        heartMonitor = HeartMonitor.getInstance();
    }

    public String getError() {
        return this.errorMessage;
    }

    public String getUsername() {
        return "";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        if (username == null || username.isEmpty()) {
            errorMessage = "Empty username";
            return CURRENT_PAGE;
        }

        if (password == null || password.isEmpty()) {
            errorMessage = "Empty password";
            return CURRENT_PAGE;
        }

        errorMessage = "";
        isLoggedIn = true;

        final JMSRequestor<LoginRequest> requestor = new JMSRequestor<>(heartMonitor);
        final Response<LoginRequest> response = requestor.request(Topics.USERS_TOPIC, new LoginRequest(username, password));
        if (response.isSuccessfull()) {
            return "servers?faces-redirect=true";
        } else {
            errorMessage = response.getMessage();
            return CURRENT_PAGE;
        }

    }

    public String logout() {
        isLoggedIn = false;
        return CURRENT_PAGE;
    }

    public boolean isConnected() {
        return isLoggedIn;
    }
}
