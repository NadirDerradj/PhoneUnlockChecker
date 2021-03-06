package com.talk2machines.phoneunlockchecker.gcm;

/**
 * Created by Erik on 03.11.2015.
 */
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.talk2machines.phoneunlockchecker.ListActivity;
import com.talk2machines.phoneunlockchecker.R;
import com.talk2machines.phoneunlockchecker.SessionActivity;
import com.talk2machines.phoneunlockchecker.UnlockReceiver;


public class CloudService extends IntentService {

    SharedPreferences prefs;
    NotificationCompat.Builder notification;
    NotificationManager manager;
    String msgType, msgHead, msgBody;


    public CloudService() {
        super("CloudService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("GCMService", "Received");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        prefs = getSharedPreferences("PUC", 0);


        if (!extras.isEmpty()) {

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e("GCM","Error");

            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.e("GCM","Error");

            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    msgType = extras.getString("msgType");
                    msgHead = extras.getString("msgHead");
                    msgBody = extras.getString("msgBody");

                    if(!prefs.getString("LOG_ID", "").isEmpty()){
                        switch (msgType) {
                            case "started":
                                startUnlockReciever();
                                sendNotification(msgHead, msgBody);
                                break;
                            case "stopped":
                                stopUnlockReciever();
                                sendNotification(msgHead, msgBody);
                                break;
                            case "delete":
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("SESSION_ID");
                                edit.apply();
                                edit.putBoolean("ADMIN", false);
                                edit.commit();
                                sendDeleteNotification(msgHead, msgBody);
                                break;
                        }

                    }
                Log.i("GCMService", "Received: " + extras.getString("msgBody"));
            }
        }
        CloudReceiver.completeWakefulIntent(intent);
    }




    private void sendNotification(String msgHead,String msgBody) {
        Intent session = new Intent(this, SessionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("s_id", prefs.getString("SESSION_ID", ""));
        bundle.putString("s_state",prefs.getString("SESSION_STATE","false"));
        session.putExtras(bundle);
        notification = new NotificationCompat.Builder(this);
        notification.setContentText(msgBody);
        notification.setContentTitle(msgHead);
        notification.setTicker("New Message !");
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setDefaults(Notification.DEFAULT_VIBRATE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000,
                session, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }

    private void sendDeleteNotification(String msgHead,String msgBody) {
        Intent session = new Intent(this, ListActivity.class);
        notification = new NotificationCompat.Builder(this);
        notification.setContentText(msgBody);
        notification.setContentTitle(msgHead);
        notification.setTicker("New Message !");
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setDefaults(Notification.DEFAULT_VIBRATE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000,
                session, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }

    private void startUnlockReciever(){
        Log.i("CloudService","Session Started");
        PackageManager pm  = CloudService.this.getPackageManager();
        ComponentName componentName = new ComponentName(CloudService.this, UnlockReceiver.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //Toast.makeText(getApplicationContext(), "activated", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor edit;
        edit = prefs.edit();
        edit.putString("SESSION_STATE","true");
        edit.commit();
    }

    private void stopUnlockReciever(){
        Log.i("CloudService","Session Stopped");
        PackageManager pm = CloudService.this.getPackageManager();
        ComponentName componentName = new ComponentName(CloudService.this, UnlockReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        //Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor edit;
        edit = prefs.edit();
        edit.putInt("NUM_UNLOCKS",0);
        edit.apply();
        edit.putString("SESSION_STATE","false");
        edit.commit();
    }


}
