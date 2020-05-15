package com.e.facex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ConfigActivity extends AppCompatActivity {

    private Switch faceDetectSwitch;
    private TextView recognitionScaleValue, checkFrequencyValue;
    private SeekBar recognitionScaleSeekbar, checkFrequencySeekbar;
    private Filer filer;
    private Object[] configValues = new Object[5];
    private static final int REQUEST_TAKE_PHOTO = 1;
    private boolean faceDetectBool;
    private boolean currentfaceDetectBool;
    private String currentPhotoPath;
    int face_x = 0;
    int face_y = 0;
    int face_width = 0;
    int face_height = 0;
    Bitmap bitmap = null;
    int imagenum = 0;

    RelativeLayout relativeLayoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        faceDetectSwitch = findViewById(R.id.switch2);
        recognitionScaleValue = findViewById(R.id.textView4);
        recognitionScaleSeekbar = findViewById(R.id.seekBar);
        checkFrequencyValue = findViewById(R.id.textView5);
        checkFrequencySeekbar = findViewById(R.id.seekBar2);
        filer = new Filer(this);
        filer.InitFile();
        relativeLayoutLoading = findViewById(R.id.relativeLayoutLoading);
        relativeLayoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void onStart(){
        super.onStart();

        {recognitionScaleSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                recognitionScaleValue.setText(String.valueOf(progress + 30));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        checkFrequencySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                checkFrequencyValue.setText(String.valueOf(progress + 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        faceDetectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        currentfaceDetectBool = true;
                    }else {
                        currentfaceDetectBool = false;
                    }
                }
            });
        }

        relativeLayoutLoading.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { }});

        configValues = filer.GetValues();
        faceDetectBool = Boolean.valueOf(String.valueOf(configValues[0]));
        if (faceDetectBool){
            faceDetectSwitch.setChecked(true);
        }else {
            faceDetectSwitch.setChecked(false);
        }
        Float progress = Float.valueOf(String.valueOf(configValues[1])) * 100;
        recognitionScaleValue.setText(String.valueOf(progress.intValue()+30));
        recognitionScaleSeekbar.setProgress(progress.intValue());
        Float progress2 = Float.valueOf(String.valueOf(configValues[2]));
        checkFrequencyValue.setText(String.valueOf(progress2.intValue()+100));
        checkFrequencySeekbar.setProgress(progress2.intValue());
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d("ImagePath", "Path is:" + currentPhotoPath);
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.e.facex.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            if (imagenum == 3){filer.SetFirstPhoto(currentPhotoPath);}
            if (imagenum == 4){filer.SetSecondPhoto(currentPhotoPath);}
            configValues = filer.GetValues();
            FindAndSaveFace(String.valueOf(configValues[imagenum]), 1);
            /*try {
            configValues = filer.GetValues();
            File f=new File(String.valueOf(configValues[imagenum]));
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (Exception e)
                {
                    e.printStackTrace();
                }*/
        }
    }

    public void ClickMakeFirstPhoto(View view){
        imagenum = 3;
        dispatchTakePictureIntent();
        Toast.makeText(this, "Wait for complete", Toast.LENGTH_LONG).show();
        relativeLayoutLoading.setVisibility(View.VISIBLE);
    }

    public void ClickMakeSecondPhoto(View view){
        imagenum = 4;
        dispatchTakePictureIntent();
        Toast.makeText(this, "Wait for complete", Toast.LENGTH_LONG).show();
        relativeLayoutLoading.setVisibility(View.VISIBLE);
    }

    public void FindAndSaveFace(String path, final int num) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        if (faces.size() != 0) {
                                            GetInfoFromImage(faces, image, num);
                                            Log.d("Detect", "Into GetInfoFromImage");
                                        }else{
                                            if (num == 1){
                                                filer.SetFirstPhoto("none");
                                                File delfile1 = new File(String.valueOf(configValues[3]));
                                                delfile1.delete();
                                                configValues = filer.GetValues();
                                                Log.d("Detect", "Here is none!");
                                                relativeLayoutLoading.setVisibility(View.GONE);
                                            }
                                            if (num == 2){
                                                filer.SetSecondPhoto("none");
                                                File delfile2 = new File(String.valueOf(configValues[4]));
                                                delfile2.delete();
                                                configValues = filer.GetValues();
                                                relativeLayoutLoading.setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        //List<FirebaseVisionFace> firebaseVisionFaces = result.getResult();
    }

    public void GetInfoFromImage(List<FirebaseVisionFace> faces, FirebaseVisionImage image, int num){
        face_x = 0;
        face_y = 0;
        face_width = 0;
        face_height = 0;

        FirebaseVisionFace trueface = null;
        //TODO

        try{
            for (FirebaseVisionFace face : faces) {
                trueface = face;
            }
        }catch (Exception e){

        }

        //TODO
        bitmap = null;
        Bitmap facebitmappart = null;

        if (trueface != null) {
            face_x = trueface.getBoundingBox().left;
            face_y = trueface.getBoundingBox().top;
            face_width = trueface.getBoundingBox().width();
            face_height = trueface.getBoundingBox().height();
            bitmap = image.getBitmap();

            initialize_face_params();
            //try {
            facebitmappart = Bitmap.createBitmap(bitmap, face_x, face_y, face_width, face_height);

            File file = new File(String.valueOf(configValues[num + 2]));
            file.delete();
            try {
                File facefile = new File(String.valueOf(configValues[num + 2]));
                FileOutputStream fOut = new FileOutputStream(facefile);
                facebitmappart.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
                relativeLayoutLoading.setVisibility(View.GONE);
                Toast.makeText(this, "Completed!", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize_face_params(){
        if (face_x < 0) face_x = 0;
        if (face_y < 0) face_y = 0;
        if ((face_x + face_width) > bitmap.getWidth()) face_width = bitmap.getWidth() - face_x;
        if ((face_y + face_height) > bitmap.getHeight()) face_height = bitmap.getHeight() - face_y;
    }

    public void ClickRestore(View view){
        faceDetectSwitch.setChecked(true);
        recognitionScaleSeekbar.setProgress(80 - 30);
        checkFrequencySeekbar.setProgress(600 - 100);
        recognitionScaleValue.setText(String.valueOf(80));
        checkFrequencyValue.setText(String.valueOf(600));
    }

    public void ClickConfirm(View view){
        configValues[0] = String.valueOf(currentfaceDetectBool);
        configValues[1] = String.valueOf(
                (Float.valueOf(String.valueOf(recognitionScaleValue.getText())) - 30) / 100);
        configValues[2] = String.valueOf(
                Integer.valueOf(String.valueOf(checkFrequencyValue.getText())) - 100);

        filer.SetValues(configValues);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
