package com.amwallace.jobscheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationJobService extends JobService {
    private NotificationManager notificationManager;
    //Constant for notification channel ID
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    @Override
    public boolean onStartJob(JobParameters params) {
        //create notification channel
        createNotificationChannel();
        //set up notification  pending intent w/ context, reqCode, intent to launch MainActivity
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //build Job Service notification w/ Title,text, intent, priority, default settings
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                PRIMARY_CHANNEL_ID).setContentTitle("Job Service")
                .setContentText("Job ran to completion")
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_job_running)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        //build and notify
        notificationManager.notify(0, builder.build());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //return true, want job to be rescheduled if it fails
        return true;
    }

    //method to create Notification Channel for SDK version >= 26
    public void createNotificationChannel(){
        //init notification manager w/ system service
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //check SDK version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //create notification channel w/ primary channel id, high importance
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Job Service Notification", NotificationManager.IMPORTANCE_HIGH);
            //configure channel settings for notifications (red lights, vibration, description)
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifications from Job Service");
            //create channel w/ manager
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }
}
