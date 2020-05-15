package com.e.facex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class ScreenOnReceiver extends BroadcastReceiver {

    boolean waked = true;

    public ScreenOnReceiver(){

    }

    @Override
    public void onReceive(final Context context, Intent intent){
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Log.d("BroadcastReceiver", "Action Screen On");
            context.startService(new Intent(context, BackService.class));
            waked = true;
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            context.stopService(new Intent(context, BackService.class));
            Log.d("BroadcastReceiver", "Action User Present");
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            context.stopService(new Intent(context, BackService.class));
            Log.d("BroadcastReceiver", "Action Screen Off");
            waked = false;
        }
        if (intent.getAction().equals("StartBackServiceLoop")){
            Log.d("Stage", "Waked? " + String.valueOf(waked));
            if (!waked) {
                context.startService(new Intent(context, BackService.class));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        context.stopService(new Intent(context, BackService.class));
                    }
                }, 1000);
                Log.d("BroadcastReceiver", "Unlock");
            }
        }

    }
}
