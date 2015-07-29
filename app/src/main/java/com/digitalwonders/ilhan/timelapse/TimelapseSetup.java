package com.digitalwonders.ilhan.timelapse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TimelapseSetup extends Activity implements View.OnClickListener {

    private List mResolutionSizes;
    private int mSelectedResolution = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timelapse_setup);

        findViewById(R.id.b_start_timelapse).setOnClickListener(this);
        findViewById(R.id.set_resolution_layout ).setOnClickListener(this);

        loadResolutionSizes();
        selectResolution(mSelectedResolution);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timelapse_setup, menu);
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
    public void onClick(View v) {

        if(v == findViewById(R.id.set_resolution_layout))
            showResolutionSelectorDialog();

        else if(v == findViewById(R.id.b_start_timelapse))
            startTimelapseService();
    }

    private void startTimelapseService() {
        Intent recordActivity= new Intent(this, RecordActivity.class);

        recordActivity.putExtra(Constants.RECORD_WIDTH, ((Camera.Size) mResolutionSizes.get(mSelectedResolution)).width);
        recordActivity.putExtra(Constants.RECORD_HEIGHT, ((Camera.Size) mResolutionSizes.get(mSelectedResolution)).height);
        recordActivity.putExtra(Constants.RECORD_INTERVAL, Integer.parseInt(((EditText)
                                findViewById(R.id.tw_interval_seconds)).getText().toString()));
        recordActivity.putExtra(Constants.RECORD_LENGHT, Integer.parseInt(((EditText)
                findViewById(R.id.tw_duration_hour)).getText().toString())*60000+1000*
                        Integer.parseInt(((EditText)
                                findViewById(R.id.tw_duration_min)).getText().toString()));
        startActivity(recordActivity);
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


    private void loadResolutionSizes() {
        Camera camera = getCameraInstance();
        mResolutionSizes = camera.getParameters() .getSupportedVideoSizes();

        Camera.Size s;
        for(int i=0; i< mResolutionSizes.size(); i++) {
            s = (Camera.Size) mResolutionSizes.get(i);
            if(s.width<640) {
                mResolutionSizes.remove(i);
                i--;
            }
        }
        camera.release();
    }

    private void showResolutionSelectorDialog() {


        List sizeStr = new ArrayList();
        Camera.Size s;
        for(int i=0; i< mResolutionSizes.size(); i++) {
            s = (Camera.Size) mResolutionSizes.get(i);
            sizeStr.add((i + 1) + "- " + s.width + " x " + s.height);
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                TimelapseSetup.this);
        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select One Name:-");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                TimelapseSetup.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(sizeStr);
        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int pos) {
                        selectResolution(pos);
                    }
                });
        builderSingle.show();
    }


    private void selectResolution(int pos) {
        mSelectedResolution = pos;
        ((TextView)findViewById(R.id.tw_image_size)).setText(
                ((Camera.Size) mResolutionSizes.get(pos)).width + " x " +
                        ((Camera.Size) mResolutionSizes.get(pos)).height);
    }
}
