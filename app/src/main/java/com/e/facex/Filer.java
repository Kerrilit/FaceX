package com.e.facex;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Filer {

    //Log tag
    private String TAG = "Filer";
    //Context for "getFilesDir()"
    private Context context;
    //Config file
    private File file;
    //File writer
    private FileWriter fw;
    //Scanner for file read
    private Scanner scanner;
    //Array with values:
    //recognition = true/false (boolean)
    //sensitivity = 0.3-0.9 (float)
    //frequency = 0.1-2 (float)
    private Object[] values = new Object[5];

    //Constructor with Context
    public Filer(Context context){
        this.context = context;
    }

    //Initialization of file and values
    public void InitFile(){
        try {
            file = new File(context.getFilesDir(), "config.txt");
            if (file.createNewFile()) {
                Log.d(TAG, "Config file created.");
                fw = new FileWriter(file);
                fw.write("true\n0.5\n500\nnone\nnone");
                fw.flush();
                fw.close();
                file = new File(context.getFilesDir(), "config.txt");
                scanner = new Scanner(file);
                values[0] = Boolean.valueOf(scanner.nextLine());
                values[1] = Float.valueOf(scanner.nextLine());
                values[2] = Float.valueOf(scanner.nextLine());
                values[3] = String.valueOf(scanner.nextLine());
                values[4] = String.valueOf(scanner.nextLine());
                scanner.close();
            } else {
                Log.d(TAG, "Config file already exist.");
                scanner = new Scanner(file);
                values[0] = Boolean.valueOf(scanner.nextLine());
                values[1] = Float.valueOf(scanner.nextLine());
                values[2] = Float.valueOf(scanner.nextLine());
                values[3] = String.valueOf(scanner.nextLine());
                values[4] = String.valueOf(scanner.nextLine());
                scanner.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Setting new values and write them into config.txt
    public void SetValues(Object[] val){
        this.values[0] = val[0];
        Log.d(TAG, "Set value 1: " + String.valueOf(val[0]));
        this.values[1] = val[1];
        Log.d(TAG, "Set value 2: " + String.valueOf(val[1]));
        this.values[2] = val[2];
        Log.d(TAG, "Set value 3: " + String.valueOf(val[2]));
        try{
            fw = new FileWriter(file);
            fw.write(String.valueOf(val[0])+"\n"+
                    String.valueOf(val[1])+"\n"+
                    String.valueOf(val[2])+"\n"+
                    String.valueOf(val[3]+"\n"+
                    String.valueOf(val[4])));
            fw.flush();
            fw.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void SetFirstPhoto(String s){
        file = new File(context.getFilesDir(), "config.txt");
        try {
            scanner = new Scanner(file);
            values[0] = Boolean.valueOf(scanner.nextLine());
            values[1] = Float.valueOf(scanner.nextLine());
            values[2] = Float.valueOf(scanner.nextLine());
            values[3] = s;
            values[4] = String.valueOf(scanner.nextLine());
            scanner.close();
            fw = new FileWriter(file);
            fw.write(String.valueOf(values[0])+"\n"+
                            String.valueOf(values[1])+"\n"+
                            String.valueOf(values[2])+"\n"+
                            values[3]+"\n"+
                            String.valueOf(values[4]));
            fw.flush();
            fw.close();
            InitFile();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SetSecondPhoto(String s){
        file = new File(context.getFilesDir(), "config.txt");
        try {
            scanner = new Scanner(file);
            values[0] = Boolean.valueOf(scanner.nextLine());
            values[1] = Float.valueOf(scanner.nextLine());
            values[2] = Float.valueOf(scanner.nextLine());
            values[3] = String.valueOf(scanner.nextLine());
            values[4] = s;
            scanner.close();
            fw = new FileWriter(file);
            fw.write(String.valueOf(values[0])+"\n"+
                    String.valueOf(values[1])+"\n"+
                    String.valueOf(values[2])+"\n"+
                    String.valueOf(values[3])+"\n"+
                    values[4]);
            fw.flush();
            fw.close();
            InitFile();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Getting values from values array
    public Object[] GetValues(){
        Log.d(TAG, "Get value 1: " + String.valueOf(values[0]));
        Log.d(TAG, "Get value 2: " + String.valueOf(values[1]));
        Log.d(TAG, "Get value 3: " + String.valueOf(values[2]));
        Log.d(TAG, "Get value 4: " + String.valueOf(values[3]));
        Log.d(TAG, "Get value 5: " + String.valueOf(values[4]));
        return this.values;
    }
}
