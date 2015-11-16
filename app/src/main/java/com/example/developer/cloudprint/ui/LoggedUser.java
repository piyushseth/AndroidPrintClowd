package com.example.developer.cloudprint.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferProgress;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.regions.Regions;
import com.dropbox.chooser.android.DbxChooser;
import com.example.developer.cloudprint.R;
import com.example.developer.cloudprint.model.User;
import com.ipaulpro.afilechooser.utils.FileUtils;


import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;

public class LoggedUser extends AppCompatActivity {

    private Button user_logout_button;
    private TextView username;
    private Bundle bundle;
    public static final String BUCKET_NAME = "cloudprint";


    static final int DBX_CHOOSER_REQUEST = 0;  // You can change this if needed
    private Button mChooserButton;
    private Button mFileButton;
    private DbxChooser mChooser;

    private static final int REQUEST_CODE = 6384; // onActivityResult request
    // code
    private static final String TAG = "FileChooserActivity";
    String upLoadServerUri = null;
    int serverResponseCode = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = this.getIntent();
        User user1 = (User)i.getSerializableExtra("User");
        Log.i("LoggedActivity User", user1.getFirstName()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        String name = user1.getFirstName() + " " + user1.getLastName();

//        bundle = this.getIntent().getExtras();
//        String name = bundle.getString("UserName");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_logged_user);
        username=(TextView)findViewById(R.id.title);
        username.setText("Name: " + name);
        user_logout_button=(Button)findViewById(R.id.logout);
        user_logout_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggedUser.this, LoginUser.class);
                startActivity(intent);
            }

        });

        mFileButton = (Button) findViewById(R.id.choose_file);
        mFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the file chooser dialog
                showChooser();
            }
        });


        mChooser = new DbxChooser("k0crqroktjryfkp");

        mChooserButton = (Button) findViewById(R.id.chooser_button);
        mChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooser.forResultType(DbxChooser.ResultType.PREVIEW_LINK)
                        .launch(LoggedUser.this, DBX_CHOOSER_REQUEST);
            }
        });

    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                Log.d("main", "Link to selected file: " + result.getLink());

                // Handle the result
            } else {
                // Failed or was cancelled by the user.
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();
                    Log.i(TAG, "Uri = " + uri.toString());
                    try {
                        // Get the file path from the URI
                        final String path = FileUtils.getPath(this, uri);
                        Toast.makeText(LoggedUser.this,
                                "File Selected: " + path, Toast.LENGTH_LONG).show();
                        upLoadServerUri = "http://www.chavisjsu.com/UploadToServer.php";

                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(LoggedUser.this, "uploading started.....", Toast.LENGTH_LONG).show();
                                    }
                                });

                                uploadFile(path);

                            }
                        }).start();

                    } catch (Exception e) {
                        Log.e("FileSelectorTestActivity", "File select error", e);
                    }
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




    public void uploadFile(String sourceFileUri) {


        final String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            //dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +fileName);


            //return 0;

        }
        else
        {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:0b413e9f-7a1b-44b7-adac-9ee2d229da56", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            //Toast.makeText(LoggedUser.this, "CRED DONE", Toast.LENGTH_LONG).show();
            Log.i("uploadFile", "CRED DONE");

            TransferManager transferManager = new TransferManager(credentialsProvider);
            //String bucket = "uni-cloud";


            Upload upload = transferManager.upload(BUCKET_NAME, sourceFile.getName(), sourceFile);
            while (!upload.isDone()){
                //Show a progress bar...
                TransferProgress transferred = upload.getProgress();
                //Toast.makeText(this, "Uploading... ", Toast.LENGTH_LONG).show();
                Log.i("Percentage", "" +transferred.getPercentTransferred());
            }

            //Toast.makeText(this, "Uploaded", Toast.LENGTH_LONG).show();








            /*try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    +" http://www.chavisjsu.com/uploads/"
                                    +fileName;

                            Toast.makeText(LoggedUser.this, msg,
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoggedUser.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoggedUser.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(LoggedUser.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);
            }
            //dialog.dismiss();
            return serverResponseCode;*/

        } // End else block
    }
}
