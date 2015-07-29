package com.digitalwonders.ilhan.timelapse;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private static final String TAG = "Timelapse";

    private boolean isRecording = false;
    private Button captureButton;

    private int mWidth = 1280;
    private int mHeight = 720;
    private double mFps = 1;
    private int mLength = 60;

    public static Surface mSurface;

    private PowerManager.WakeLock wl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        if(!checkCameraHardware(getApplicationContext())) {
            finish();
        }

        mWidth = getIntent().getIntExtra(Constants.RECORD_WIDTH, mWidth);
        mHeight = getIntent().getIntExtra(Constants.RECORD_HEIGHT, mHeight);
        double interval = getIntent().getIntExtra(Constants.RECORD_INTERVAL, 1);
        mFps = 1 / interval;
        mLength = getIntent().getIntExtra(Constants.RECORD_LENGHT, mLength);
        Log.i("Timelapse", "Lenght: " + mLength);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                //preview.removeAllViews();
                //mPreview = null;
                releaseCamera();
                RecordActivity.mSurface = mPreview.getHolder().getSurface();
                startTimelapseService();

            }
        });

        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(
                Context.POWER_SERVICE);
        wl = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE,
                TAG);
        wl.acquire();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void startTimelapseService() {

        try {
            Intent recordService = new Intent(this, RecordService.class);
            recordService.putExtra(Constants.RECORD_WIDTH, mWidth);
            recordService.putExtra(Constants.RECORD_HEIGHT, mHeight);
            recordService.putExtra(Constants.RECORD_FPS, mFps);
            recordService.putExtra(Constants.RECORD_LENGHT, mLength);

            startService(recordService);
        }
        catch (Exception e) {
            Log.e("Timelapse", e.toString());
        }
    }

    private void setCaptureButtonText(String text) {
        captureButton.setText(text);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    public void onStop(){

        wl.release();
        super.onStop();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
