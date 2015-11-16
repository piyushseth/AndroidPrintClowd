package com.example.developer.cloudprint.ui;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferProgress;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.regions.Regions;
import com.dropbox.chooser.android.DbxChooser;
import com.example.developer.cloudprint.R;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.Wearable;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//seth
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    LinearLayout mapBelowLinearLayout;
    Button uploadButton;
    private static final int REQUEST_CODE = 6384;
    static final int DBX_CHOOSER_REQUEST = 0;
    private static final String TAG = "FileChooserActivity";
    String upLoadServerUri = null;
    int serverResponseCode = 0;
    EditText NoOfPage;
    Button printButton;
    boolean flag1 = false;
    boolean flag2 = false;
    public static final String BUCKET_NAME = "cloudprint";
    public FloatingActionButton profile,payment,history, logout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // setUpMapIfNeeded();

        mapBelowLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutButton);
        mapBelowLinearLayout.setVisibility(LinearLayout.GONE);

        //seth
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        printButton = (Button)findViewById(R.id.printButton);
        printButton.setVisibility(Button.GONE);
        uploadButton = (Button) findViewById(R.id.leftButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();
                Toast.makeText(MapActivity.this, "Button Clicked", Toast.LENGTH_LONG).show();

                //printButton.setVisibility(Button.VISIBLE);

            }
        });

        final FloatingActionsMenu mFabMenu = (FloatingActionsMenu) findViewById(R.id.floatingActionsMenu);

        profile = (FloatingActionButton) findViewById(R.id.profile);
        payment = (FloatingActionButton) findViewById(R.id.payment);
        history = (FloatingActionButton) findViewById(R.id.history);
        logout = (FloatingActionButton) findViewById(R.id.logout);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, Profile.class);
                startActivity(intent);
            }
        });


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, PrintOptions.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, LoginUser.class);
                startActivity(intent);
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
    public void onMapReady(GoogleMap map) {

        map.setMyLocationEnabled(true);
//        LatLng sydney = new LatLng(-33.867, 151.206);
        Location location = map.getMyLocation();
        if (location != null) {

            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            String message = String.format("latitude = %f longitude = %f", location.getLatitude(), location.getLongitude());
            Log.i("Location", message);
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
        } else {
            Log.i("location", "Its null!!!!!!!!!!!!!");
        }


        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(37.3404372, -121.8976136))
                .title("Vila Torino")
                .snippet("this shows desciption of place"));

        map.addMarker(new MarkerOptions()
                .position(new LatLng(37.343329, -121.8915832))
                .title("Mi Pueblo")
                .snippet("this shows desciption of place"));
        Log.v("Marker Error", "Set near Marker");
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mapBelowLinearLayout.setVisibility(LinearLayout.VISIBLE);
                return false;
            }
        });

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                //setUpMap();
                // Load coordinate from database;
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // setUpMapIfNeeded();
    }

    // on Activity Result

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
                        Toast.makeText(MapActivity.this,
                                "File Selected: " + path, Toast.LENGTH_LONG).show();

                        upLoadServerUri = "http://www.chavisjsu.com/UploadToServer.php";

                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MapActivity.this, "uploading started.....", Toast.LENGTH_LONG).show();
                                        uploadFile(path);
                                    }
                                });

                                // uploadFile(path);

                            }
                        }).start();

                    } catch (Exception e) {
                        Log.e("FileSelectorTestAcivity", "File select error", e);
                    }
                }
            }
        } else {
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
            Toast.makeText(this, "Uploaded", Toast.LENGTH_LONG).show();
            printButton.setVisibility(Button.VISIBLE);







//            try {
//
//                // open a URL connection to the Servlet
//                FileInputStream fileInputStream = new FileInputStream(sourceFile);
//                URL url = new URL(upLoadServerUri);
//
//                // Open a HTTP  connection to  the URL
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setDoInput(true); // Allow Inputs
//                conn.setDoOutput(true); // Allow Outputs
//                conn.setUseCaches(false); // Don't use a Cached Copy
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                conn.setRequestProperty("uploaded_file", fileName);
//
//                dos = new DataOutputStream(conn.getOutputStream());
//
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
//                        + fileName + "\"" + lineEnd);
//
//                dos.writeBytes(lineEnd);
//
//                // create a buffer of  maximum size
//                bytesAvailable = fileInputStream.available();
//
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//
//                // read file and write it into form...
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                while (bytesRead > 0) {
//
//                    dos.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                }
//
//                // send multipart form data necesssary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//                // Responses from the server (code and message)
//                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//
//                Log.i("uploadFile", "HTTP Response is : "
//                        + serverResponseMessage + ": " + serverResponseCode);
//
//                if(serverResponseCode == 200){
//                    String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                            +" http://www.chavisjsu.com/uploads/"
//                            +fileName;
//                    Toast.makeText(MapActivity.this, msg,
//                            Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, "File Upload Complete.",
//                            Toast.LENGTH_SHORT).show();
//                    printButton.setVisibility(Button.VISIBLE);
//                    NoOfPage.setVisibility(View.VISIBLE);
//                    NoOfPage.setText(" ");
//                }
//
//                //close the streams //
//                fileInputStream.close();
//                dos.flush();
//                dos.close();
//
//            } catch (MalformedURLException ex) {
//
//                //dialog.dismiss();
//                ex.printStackTrace();
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(MapActivity.this, "MalformedURLException",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
//            } catch (Exception e) {
//
//                //dialog.dismiss();
//                e.printStackTrace();
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//
//                        Toast.makeText(MapActivity.this, "Got Exception : see logcat ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Log.e("Upload file to server Exception", "Exception : "
//                        + e.getMessage(), e);
//            }
//            //dialog.dismiss();
//            return serverResponseCode;

        } // End else block
    }
}
