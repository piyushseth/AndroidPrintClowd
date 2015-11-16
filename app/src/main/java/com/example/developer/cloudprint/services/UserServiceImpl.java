package com.example.developer.cloudprint.services;

/**
 * Created by Ankush on 10/17/15.
 */
import android.content.Context;
import android.util.Log;

import com.example.developer.cloudprint.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import org.json.*;

import java.io.UnsupportedEncodingException;


public class UserServiceImpl implements UserService {

    public UserServiceImpl(){

    }

//    @Override
//    public void login(User user) {
//
//        UserClient.get("", null, new JsonHttpResponseHandler() {
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
//                // Pull out the first event on the public timeline
//                JSONObject firstEvent = null;
//                User user = null;
//
//                try {
//                    firstEvent = (JSONObject) timeline.get(0);
//                    user = (User)timeline.get(0);
//                    System.out.println("user found with User Name--->  "+user.getEmail());
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String tweetText;
//                tweetText = null;
//                try {
//                    tweetText = firstEvent.getString("email");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                // Do something with the response
//                System.out.println(tweetText);
//            }
//
//
//        });
//    }


    @Override
    public void login(final User user, Context context) throws JSONException{
        JSONObject jsonParams = new JSONObject();

        jsonParams.put("email", user.getEmail());
        jsonParams.put("password", user.getPassword());

        String jsonString = jsonParams.toString();
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonString);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        UserClient.post(context, "auth/app", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseString) {
                //super.onSuccess(statusCode, headers, responseString);
                String str;
                String email="";
                str = new String(responseString);

                try {
                    JSONObject jsonObj = new JSONObject(str);
                    email = jsonObj.getString("email");

                } catch (Exception e) {
                    Log.e("Json", "Error");
                }

                if(email!="") {
                    Log.i("Login", "Successfully logged in");
                    for (Header header : headers) {
                        if (header.getName().equals("X-Auth-Token")) {
                            user.setToken(header.getValue());
                        }

                    }
                }
                else{
                    Log.i("Login", "Email not found");
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {
                Log.e("Post", "failure: " + new String(responseBody));
                Log.e("Post", "failurecode: " + statusCode);

                //super.onFailure(statusCode, headers, responseBody, throwable);
            }


        });

    }




    @Override
    public void register(final User user, Context context) {
        try {
//            JSONObject jsonParams = new JSONObject();
//            jsonParams.put("notes", "Test api support");
//            StringEntity entity = new StringEntity(jsonParams.toString());

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(user);
            Log.i("JSON",json);
            StringEntity entity = new StringEntity(json);

            UserClient.post(context, "api/users", entity, "application/json", new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseString) {
                    //super.onSuccess(statusCode, headers, responseString);
                    String str = new String(responseString);

                    for (Header header : headers) {
                        if(header.getName().equals("X-Auth-Token")){
                            Log.i("header","Key : " + header.getName()
                                    + " ,Value : " + header.getValue());
                        }

                    }
                    String id = "";
                    try {
                        JSONObject jsonObj = new JSONObject(str);
                        id = jsonObj.getString("_id");

                    } catch (Exception e) {
                        Log.e("Json", "Error");
                    }
                    Log.i("ID",id);
                    user.set_id(id);
                    Log.i("Post", "Successfully posted registration records");

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {
                    Log.e("Post", "failure: " + new String(responseBody));
                    Log.e("Post", "failurecode: " + statusCode);

                    //super.onFailure(statusCode, headers, responseBody, throwable);
                }


            });
        }
        catch(Exception e){
            Log.e("Post", "Error");
        }
    }
}
