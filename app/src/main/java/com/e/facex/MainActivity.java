package com.e.facex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Switch switcher;
    private Context context;
    private Intent intent;
    private Button btn2, btn8;
    private Filer filer;
    private Object[] configvalues = new Object[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switcher = (Switch)findViewById(R.id.switch1);
        btn2 = (Button)findViewById(R.id.button2);
        btn8 = (Button)findViewById(R.id.button8);
        this.context = this;
        filer = new Filer(context);
        filer.InitFile();
    }

    @Override
    public void onStart(){
        super.onStart();

        configvalues = filer.GetValues();
        Log.d("PhotoCheck", "Config value 1: " + configvalues[3]);
        Log.d("PhotoCheck", "Config value 2: " + configvalues[4]);
        if ("none".equals(configvalues[3]) || "none".equals(configvalues[4])){
            switcher.setClickable(false);
            Toast.makeText(context, "You don`t have 2 face images!", Toast.LENGTH_LONG).show();
        }else{
            switcher.setClickable(true);
        }
        if (isMyServiceRunning(LongService.class)){
            switcher.setChecked(true);
            btn2.setEnabled(false);
        }else{
            switcher.setChecked(false);
            btn2.setEnabled(true);
        }

        if (CheckPermission()){
            btn8.setVisibility(View.GONE);
        }else { btn8.setVisibility(View.VISIBLE); }

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    startService(new Intent(context, LongService.class));
                    switcher.setChecked(true);
                    btn2.setEnabled(false);
                }else {
                    stopService(new Intent(context, LongService.class));
                    switcher.setChecked(false);
                    btn2.setEnabled(true);
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean CheckPermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                return true;
            }
            else { return false; }
        }else { return false; }
    }

    public void ClickConfig(View view){
        intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
        finish();
    }

    public void ClickAllow(View view){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }
}