package com.example.developer.cloudprint.ui;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
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
import com.amazonaws.services.s3.AmazonS3Client;
import com.dropbox.chooser.android.DbxChooser;
import com.example.developer.cloudprint.R;
import com.example.developer.cloudprint.model.Document;
import com.example.developer.cloudprint.model.User;
import com.example.developer.cloudprint.services.DocServiceImpl;
import com.example.developer.cloudprint.services.UserServiceImpl;
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

    //DocumentAPI
    private User user1;
    private Document doc;
    private DocServiceImpl docService;
    private UserServiceImpl userService;
    SQLiteDatabase mysql;
    String DOC_CREATE = "CREATE TABLE IF NOT EXISTS DOCUMENTS(ID TEXT PRIMARY KEY, DOCNAME TEXT, DOCURL TEXT, USER_ID TEXT)";
    Context context;
    public static final String BASE_FILE_URL = "https://s3.amazonaws.com/cloudprint/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DocumentAPI
        context = this.getApplicationContext();

        mysql=this.openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        mysql.execSQL(DOC_CREATE);

        Intent i = this.getIntent();
        user1 = (User)i.getSerializableExtra("User");
        Log.i("LoggedActivity User", user1.getFirstName() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

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
        File sourceFile = new File(sourceFileUri);
        //Looper.prepare();
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"
                    +fileName);
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

            Upload upload = transferManager.upload(BUCKET_NAME, sourceFile.getName(), sourceFile);
            AmazonS3Client client = new AmazonS3Client(credentialsProvider);

            URL fileURL = client.getUrl(BUCKET_NAME, sourceFile.getName());

            Log.i("uploadFile", sourceFile.getName());
            Log.i("uploadFile", BASE_FILE_URL+sourceFile.getName());
            Log.i("uploadFile URL", fileURL.toString());
            Log.i("uploadFile", user1.getFirstName());


            while (!upload.isDone()){
                //Show a progress bar...
                TransferProgress transferred = upload.getProgress();
                Toast.makeText(MapActivity.this, "Uploading... ", Toast.LENGTH_LONG).show();
                Log.i("Percentage", "" +transferred.getPercentTransferred());
            }

            if(upload.isDone()){
                Toast.makeText(MapActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
                doc = new Document();
                doc.setName(sourceFile.getName());
                doc.setUrl(fileURL.toString());

                docService = new DocServiceImpl();
                docService.postDocument(user1, doc, context);

                Log.i("Document", "ID is " + user1.get_id());
                Log.i("Document", "DocID is " + doc.get_id());
                Log.i("Document", "Name is " + doc.getName());
                Log.i("Document", "URL is " + doc.getUrl());

                if(doc.get_id()!= null) {
                    mysql.execSQL("INSERT INTO DOCUMENTS(ID,DOCNAME,DOCURL,USER_ID) VALUES('" + doc.get_id() + "','" + doc.getName() + "' ,'" + doc.getUrl() + "','" + user1.get_id() + "')");
                }
            }



        } // End else block
    }
}
