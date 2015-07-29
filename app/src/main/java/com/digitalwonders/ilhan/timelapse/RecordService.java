package com.digitalwonders.ilhan.timelapse;


import android.app.IntentService;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ilhan on 24.07.2015.
 */
public class RecordService extends IntentService {

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private static final String TAG = "Timelapse";

    private int mWidth = 1280;
    private int mHeight = 720;
    private double mFps = 1;
    private int mLength = 60;

    public RecordService() {
        super("RecordService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        try {

            Log.i("Timelapse", "service started!");
            mWidth = workIntent.getIntExtra(Constants.RECORD_WIDTH, mWidth);
            mHeight = workIntent.getIntExtra(Constants.RECORD_HEIGHT, mHeight);
            mFps = workIntent.getDoubleExtra(Constants.RECORD_FPS, 1);
            mLength = workIntent.getIntExtra(Constants.RECORD_LENGHT, mLength);
            Log.i("Timelapse", "mLength: " + mLength);
            mCamera = getCameraInstance();
            if(mCamera == null) {
                Log.i("Timelapse", "camera failed!");
                return;
            }

            if (prepareVideoRecorder())
                mMediaRecorder.start();

            else
                Log.i("Timelapse", "media recorder failed!");

            Log.i("Timelapse", "recording!");

        }
        catch (Exception e) {
            Log.e("Timelapse", e.toString());
        }


        // Gets data from the incoming Intent
        //String dataString = workIntent.getDataString();

        // Do work here, based on the contents of dataString

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

    private boolean prepareVideoRecorder(){


        //mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mMediaRecorder.stop();
                    Log.i("Timelapse", "record finished!");
                    releaseMediaRecorder();
                    releaseCamera();
                }
            }
        });
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);


        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        if(mWidth != 0 && mHeight != 0)
            mMediaRecorder.setVideoSize(mWidth, mHeight);

        // Step 5: Set the preview output
        //mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setPreviewDisplay(RecordActivity.mSurface);

// Step 5.5: Set the video capture rate to a low number
        mMediaRecorder.setCaptureRate(mFps); // capture a frame every 10 seconds


        mMediaRecorder.setMaxDuration(mLength);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** Create a file Uri for saving an image or video */
    private static android.net.Uri getOutputMediaFileUri(){
        return android.net.Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Timelapse");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Timelapse", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

        return mediaFile;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
