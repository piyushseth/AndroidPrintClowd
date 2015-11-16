package com.example.developer.cloudprint.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.developer.cloudprint.R;
import com.example.developer.cloudprint.model.User;
import com.example.developer.cloudprint.services.UserServiceImpl;

import org.json.JSONException;


public class LoginUser extends AppCompatActivity implements View.OnClickListener{

    private EditText login_username;
    private EditText login_password;
    private Button user_login_button;
    private Button user_register_button;
    SQLiteDatabase mysql;
    String DB_QUERY;
    String QUERY_result;
    Cursor result;
    LinearLayout layout;
    private  UserServiceImpl userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login_user);
        initWidget();
    }
    private void initWidget()
    {
        layout = (LinearLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent ev)
            {
                hideKeyboard(view);
                return false;
            }
        });

//        Intent i = this.getIntent();
//        User user1 = (User)i.getSerializableExtra("User");
//        Log.i("LoginActivity User", user1.get_id()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        login_username=(EditText)findViewById(R.id.login_username);
        login_password=(EditText)findViewById(R.id.login_password);
        user_login_button=(Button)findViewById(R.id.user_login_button);
        user_register_button=(Button)findViewById(R.id.user_register_button);
        mysql=this.openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        DB_QUERY = "SELECT * FROM USERS WHERE EMAIL=? AND PASSWORD=?";
        user_login_button.setOnClickListener(this);
        user_register_button.setOnClickListener(this);
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.user_login_button:
                if (checkEdit()) {
                    login();
                }
                break;
            case R.id.user_register_button:
                Intent intent2 = new Intent(LoginUser.this, RegisterUser.class);
                startActivity(intent2);
                break;
        }

    }

    private boolean checkEdit(){
        if(login_username.getText().toString().trim().equals("")){
            Toast.makeText(LoginUser.this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
        }else if(login_password.getText().toString().trim().equals("")){
            Toast.makeText(LoginUser.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
        }else{
            return true;
        }
        return false;
    }

    private void login(){
        Context context = this.getApplicationContext();
        User user1 = new User();
        user1.setEmail(login_username.getText().toString().trim());
        user1.setPassword(login_password.getText().toString().trim());

        userService= new UserServiceImpl();

        try {
            userService.login(user1, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Login Activity", user1.getToken());




       result=mysql.rawQuery(DB_QUERY,new String []{login_username.getText().toString().trim(),login_password.getText().toString().trim()});
        Log.i("Result", "Count is " +result.getCount());
        if(result.getCount()>0){
            if (result.moveToFirst()) {
                do {
                    Log.i("Firstname", result.getString(result.getColumnIndexOrThrow("FIRSTNAME")));
                    user1.setFirstName(result.getString(result.getColumnIndexOrThrow("FIRSTNAME")));
                    user1.setLastName(result.getString(result.getColumnIndexOrThrow("LASTNAME")));
                    user1.set_id(result.getString(result.getColumnIndexOrThrow("ID")));
//                    user1.setToken(result.getString(result.getColumnIndexOrThrow("TOKEN")));
                    Log.i("User", user1.toString());
                } while (result.moveToNext());
            }
            Intent intent;
            intent = new Intent(LoginUser.this,MapActivity.class);
            intent.putExtra("User", user1);
            startActivity(intent);
//            Bundle bundle = new Bundle();
//            bundle.putString("UserName", user1.getFirstName()+" " +user1.getLastName());
//            intent.putExtras(bundle);
//            startActivity(intent);

        }
        else{
            Toast.makeText(LoginUser.this,"Your password or username is wrong", Toast.LENGTH_SHORT).show();
        }
    }

}
