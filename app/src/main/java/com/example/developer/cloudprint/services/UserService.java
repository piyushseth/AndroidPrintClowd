package com.example.developer.cloudprint.services;


import android.content.Context;

import com.example.developer.cloudprint.model.User;

import org.json.JSONException;

/**
 * Created by Ankush on 10/17/15.
 */
public interface UserService {
    public void login(User user, Context context) throws JSONException;
    public void register(User user, Context context);
}

