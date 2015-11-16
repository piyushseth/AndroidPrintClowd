package com.example.developer.cloudprint.ui;

import android.app.Application;
import android.content.ContentValues;
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
    String DB_CREATE = "CREATE TABLE IF NOT EXISTS USERS(ID TEXT PRIMARY KEY, FIRSTNAME TEXT, LASTNAME TEXT, EMAIL TEXT," +
            "PASSWORD TEXT, TOKEN TEXT)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login_user);
        mysql=this.openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        mysql.execSQL(DB_CREATE);
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

        userService.login(user1, context);

        if(user1.getToken()!= null){
            Log.i("Login Activity", user1.getToken());

            result=mysql.rawQuery(DB_QUERY,new String []{login_username.getText().toString().trim(),login_password.getText().toString().trim()});
            Log.i("Result", "Count is " + result.getCount());
            if(result.getCount()>0){
                Log.i("SQL Result", "Record already exists in the database");
                ContentValues cv = new ContentValues();
                cv.put("TOKEN", user1.getToken());
                mysql.update("USERS", cv, "ID" + "=" + "'" + user1.get_id() + "'", null);

            }
            else{
                Log.i("LoginActivity", "Inserting records into the user table");
                mysql.execSQL("INSERT INTO USERS(ID,FIRSTNAME,LASTNAME,EMAIL,PASSWORD,TOKEN) VALUES('" + user1.get_id() + "','" + user1.getFirstName() + "' ,'" + user1.getLastName() + "','" + user1.getEmail() + "','" + user1.getPassword() + "','" + user1.getToken() +"')");
            }
            Intent intent;
            intent = new Intent(LoginUser.this,MapActivity.class);
            intent.putExtra("User", user1);
            startActivity(intent);
        }
        else{
            Log.i("Token", "No token from server");
        }
    }

}
