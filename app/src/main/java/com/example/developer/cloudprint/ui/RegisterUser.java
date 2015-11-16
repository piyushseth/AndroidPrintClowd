package com.example.developer.cloudprint.ui;

import com.example.developer.cloudprint.model.User;
import com.example.developer.cloudprint.services.UserServiceImpl;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.developer.cloudprint.R;


import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class RegisterUser extends AppCompatActivity {

    private EditText register_email;
    private EditText register_passwd;
    private EditText reregister_passwd;
    private EditText firstname;
    private EditText lastname;
    private Button register_submit;
    private Button login;
    LinearLayout layout;
    private  UserServiceImpl userService;
    SQLiteDatabase mysql;
    String DB_CREATE = "CREATE TABLE IF NOT EXISTS USERS(ID TEXT PRIMARY KEY, FIRSTNAME TEXT, LASTNAME TEXT, EMAIL TEXT," +
            "PASSWORD TEXT, TOKEN TEXT)";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_register_user);

        final Context context = this.getApplicationContext();

        layout = (LinearLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                hideKeyboard(view);
                return false;
            }
        });
        mysql=this.openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        mysql.execSQL(DB_CREATE);
        firstname=(EditText)findViewById(R.id.firstname);
        lastname=(EditText)findViewById(R.id.lastname);
        register_email=(EditText)findViewById(R.id.register_email);
        register_passwd=(EditText)findViewById(R.id.register_password);
        reregister_passwd=(EditText)findViewById(R.id.register_cpassword);
        register_submit=(Button)findViewById(R.id.register_submit);
        login=(Button)findViewById(R.id.login);

        login.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterUser.this,LoginUser.class);
                startActivity(intent);
            }

        });
        register_submit.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!checkEdit()){
                    return;
                }
                User user1 = new User();
                user1.setEmail(register_email.getText().toString().trim());
                user1.setFirstName(firstname.getText().toString().trim());
                user1.setLastName(lastname.getText().toString().trim());
                user1.setPassword(register_passwd.getText().toString().trim());

                userService= new UserServiceImpl();

                userService.register(user1, context);

                if(user1.get_id()!=null) {
                    mysql.execSQL("INSERT INTO USERS(ID,FIRSTNAME,LASTNAME,EMAIL,PASSWORD) VALUES('" + user1.get_id() + "','" + user1.getFirstName() + "' ,'" + user1.getLastName() + "','" + user1.getEmail() + "','" + user1.getPassword() + "')");

                    Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                    intent.putExtra("User", user1);
                    startActivity(intent);
                }
            }

        });
    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }


    private boolean checkEdit(){
        if(firstname.getText().toString().trim().equals("")){
            Toast.makeText(RegisterUser.this, "First Name cannot be empty.", Toast.LENGTH_SHORT).show();
        }else if(lastname.getText().toString().trim().equals("")){
            Toast.makeText(RegisterUser.this, "Last Name cannot be empty.", Toast.LENGTH_SHORT).show();
        }else if(register_email.getText().toString().trim().equals("")){
            Toast.makeText(RegisterUser.this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
        }else if(register_passwd.getText().toString().trim().equals("")){
            Toast.makeText(RegisterUser.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
        }else if(!isValidPassword(register_passwd.getText().toString().trim())){
            Toast.makeText(RegisterUser.this, "Password should contain at least an uppercase, a lowercase letter and a digit (Total 6 chars or more)", Toast.LENGTH_SHORT).show();
        }else if(!register_passwd.getText().toString().trim().equals(reregister_passwd.getText().toString().trim())){
            Toast.makeText(RegisterUser.this, "The passwords do not match.", Toast.LENGTH_SHORT).show();
        }else{
            return true;
        }
        return false;
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
