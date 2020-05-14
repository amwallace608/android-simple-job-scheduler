package com.amwallace.jobscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private JobScheduler jobScheduler;
    private Switch deviceIdleSwitch;
    private Switch deviceChargingSwitch;
    private SeekBar overrideSeekbar;

    //constant for job id
    private static final int JOB_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init switches
        deviceChargingSwitch = (Switch) findViewById(R.id.chargingSwitch);
        deviceIdleSwitch = (Switch) findViewById(R.id.idleSwitch);
        //init seekbar
        overrideSeekbar = (SeekBar) findViewById(R.id.overrideSeekBar);
        //init seekbar progress text view
        final TextView seekBarProgress = (TextView) findViewById(R.id.seekBarProgress);
        //seekbar change listener
        overrideSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //if progress value > 0, set progress label to integer value (seconds)
                if(progress > 0){
                    seekBarProgress.setText(progress + "s");
                } else {
                    seekBarProgress.setText(getString(R.string.not_set));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //schedule job method
    public void scheduleJob(View view){
        //get network options radiogroup
        RadioGroup networkOptions = findViewById(R.id.networkOptionsGrp);
        //get selected network option ID
        int selectedNetId = networkOptions.getCheckedRadioButtonId();
        //create int for selected network option constant, init default as none
        int selectedNetOption = JobInfo.NETWORK_TYPE_NONE;
        //assign selected network option to correct network constant for input
        switch (selectedNetId){
            case R.id.noNetRadioBtn:
                //no network required
                selectedNetOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetRadioBtn:
                //any network type
                selectedNetOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiRadioBtn:
                //wifi network required
                selectedNetOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
            default:
                //none for default
                selectedNetOption = JobInfo.NETWORK_TYPE_NONE;
                break;
        }

        //get seekbar progress
        int seekBarProg = overrideSeekbar.getProgress();
        //record if seekbar has been set by user
        boolean seekBarSet = seekBarProg > 0;

        //init scheduler w/ system service
        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        //create componentName for jobService from package and Jobservice class
        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        //jobInfo builder for job w/id and service name
        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, serviceName);
        //set required network type
        jobBuilder.setRequiredNetworkType(selectedNetOption);
        //set device idle constraint from switch state
        jobBuilder.setRequiresDeviceIdle(deviceIdleSwitch.isChecked());
        //set device charging constraint from switch state
        jobBuilder.setRequiresCharging(deviceChargingSwitch.isChecked());

        //override deadline by seekbar progress in seconds if seekbar is set
        if(seekBarSet){
            //progress int * 1000ms = number of seconds to override deadline
            jobBuilder.setOverrideDeadline(seekBarProg * 1000);
        }

        //check that at least one requirement is set
        if(selectedNetOption != JobInfo.NETWORK_TYPE_NONE
                || deviceIdleSwitch.isChecked() || deviceChargingSwitch.isChecked() || seekBarSet){
            //build JobInfo
            JobInfo jobInfo = jobBuilder.build();
            //schedule job w/ scheduler
            jobScheduler.schedule(jobInfo);
            //indicate job was scheduled
            Toast.makeText(this,
                    "Job scheduled, running when constraints are met",
                    Toast.LENGTH_SHORT).show();
        } else {
            //network requirement constraint not set
            Toast.makeText(this,
                    "Set at least one requirement",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //cancel all jobs method
    public void cancelJobs(View view){
        //check if scheduler is null
        if(jobScheduler != null){
            //cancel jobs
            jobScheduler.cancelAll();
            //set scheduler to null
            jobScheduler = null;
            //indicate jobs cancelled
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
