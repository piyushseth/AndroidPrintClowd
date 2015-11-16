package com.example.developer.cloudprint.services;

import android.content.Context;
import com.example.developer.cloudprint.model.User;
import com.example.developer.cloudprint.model.Document;


/**
 * Created by Chavi on 11/8/15.
 */
public interface DocService {
    public void postDocument(User user, Document doc, Context context);
    public void delete(User user, Document doc, Context context);
}
