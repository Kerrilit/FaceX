package com.e.facex;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Locker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        KeyguardManager keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);
        finish();
        super.onCreate(savedInstanceState);
    }

    public void Click(View view){

        finishAndRemoveTask();
    }

    public void onStart(){
        super.onStart();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            Intent nextFlow = new Intent(this, Locker.class);
            startActivity(nextFlow);

            return true;

        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {

        //Intent nextFlow = new Intent(this, AppActionActivity.class);

            Intent nextFlow = new Intent(this, Locker.class);
            startActivity(nextFlow);

            return true;

        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onPause(){
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
