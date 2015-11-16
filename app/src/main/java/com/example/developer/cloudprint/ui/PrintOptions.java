package com.example.developer.cloudprint.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.developer.cloudprint.R;

public class PrintOptions extends AppCompatActivity {
    EditText NoOfPage;
    Button printButton;
    Spinner colorDropdown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_options);


        NoOfPage = (EditText) findViewById(R.id.printsText);
        printButton = (Button)findViewById(R.id.printButton);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NoOfPage.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Field Cannot be Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog dialog = new AlertDialog.Builder(PrintOptions.this)
                            .setTitle("Print")
                            .setMessage("Print Success")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    NoOfPage.setText(" ");
                                }
                            })
                            .show();
                }
            }
        });

    }
}


