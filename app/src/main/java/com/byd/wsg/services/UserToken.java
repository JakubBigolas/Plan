package com.byd.wsg.services;

import com.byd.wsg.com.wsg.byd.plan.raw.RawAuthenticate;

/**
 * Created by Jakub on 2016-06-05.
 */
public class UserToken {

    private RawAuthenticate token;

    private String login, password;

    public UserToken(String login,String password){
        this.login = login;
        this.password = password;
    }

    public void onTokenChange(RawAuthenticate lastToken, RawAuthenticate newToen){

    }

    public synchronized final void setToken(RawAuthenticate token){
        onTokenChange(this.token, token);
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public RawAuthenticate getToken() {
        return token;
    }
}
