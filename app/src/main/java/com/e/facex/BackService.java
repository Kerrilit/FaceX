package com.e.facex;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import android.media.Image;
import android.util.Size;
import android.widget.Toast;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackService extends Service {

    FaceXLifecycleOwner faceXLifecycleOwner = new FaceXLifecycleOwner();

    PreviewConfig pConfig = null;
    Preview preview = null;
    FirebaseVisionFaceDetectorOptions detectorOptions = null;
    FirebaseVisionFaceDetector detector = null;

    Image mediaImage = null;
    FirebaseVisionImage imagevision = null;
    Bitmap bitmap = null;
    Bitmap facebitmappart = null;
    FirebaseVisionFace trueface = null;
    String modelFile = "_Model_.tflite";
    Interpreter tflite;

    int face_x = 0;
    int face_y = 0;
    int face_width = 0;
    int face_height = 0;

    Filer filer = new Filer(this);
    Object[] confvalues = new Object[5];

    public void onCreate(){
        super.onCreate();
        Log.d("BackSerice", "onCreate");
        CameraX.unbindAll();
        filer.InitFile();
        confvalues = filer.GetValues();

        try {
            tflite=new Interpreter(loadModelFile(this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .build();
        preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput previewOutput) { }
                });

        initializeFaceDetectorAndOptions();

        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setTargetResolution(new Size(200, 300))
                        .setLensFacing(CameraX.LensFacing.FRONT)
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy imageProxy, int rotationDegrees) {
                        mediaImage = imageProxy.getImage();
                        int rotation = degreesToFirebaseRotation(rotationDegrees);
                        if (mediaImage == null) {
                            return;
                        }
                        imagevision = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

                        Task<List<FirebaseVisionFace>> result =
                                detector.detectInImage(imagevision)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                                    @Override
                                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                                        if ("false".equals(String.valueOf(confvalues[0])) && faces.size() != 0){
                                                            WakeUpNeo();
                                                        }else {
                                                            if (imagevision == null) return;
                                                            if (faces.size() != 0) {
                                                                getInfoFromDetectedImage(faces, imagevision);
                                                                imagevision = null;
                                                            } else {
                                                                imagevision = null;
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



                    }
                });
        CameraX.bindToLifecycle(faceXLifecycleOwner, imageAnalysis, preview);
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        faceXLifecycleOwner.start();
        Log.d("BackSerice", "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy(){
        CameraX.unbindAll();
        faceXLifecycleOwner.tearDown();
        Log.d("BackSerice", "onDestroy");

        super.onDestroy();
    }

    public IBinder onBind(Intent intent){
        Log.d("myLogs", "onBind");
        return null;
    }

    private void initializeFaceDetectorAndOptions(){
        //initializing detector options
        detectorOptions =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                        .build();

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(detectorOptions);
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    private void getInfoFromDetectedImage(List<FirebaseVisionFace> faces, FirebaseVisionImage image){
        face_x = 0;
        face_y = 0;
        face_width = 0;
        face_height = 0;

        trueface = null;
        //TODO

        try{
            for (FirebaseVisionFace face : faces) {
                trueface = face;
            }
        }catch (Exception e){

        }

        //TODO
        bitmap = null;
        facebitmappart = null;

        if (trueface != null){

            face_x = trueface.getBoundingBox().left;
            face_y = trueface.getBoundingBox().top;
            face_width = trueface.getBoundingBox().width();
            face_height = trueface.getBoundingBox().height();
            bitmap = image.getBitmap();

            initialize_face_params();
            //try {
            facebitmappart = Bitmap.createBitmap(bitmap, face_x, face_y, face_width, face_height);
            //}catch (Exception e){
            //   return;
            //}
            confvalues = filer.GetValues();
            File f1 = new File(String.valueOf(confvalues[3]));
            File f2 = new File(String.valueOf(confvalues[4]));

            //faces images (1-2) ///////////////////////
            TensorImage tImage1 = null;
            try {
                Bitmap icon1 = BitmapFactory.decodeStream(new FileInputStream(f1));
                ImageProcessor imageProcessor1 =
                        new ImageProcessor.Builder()
                                .add(new ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
                                .add(new NormalizeOp(0, 255))
                                .build();
                tImage1 = new TensorImage(DataType.FLOAT32);
                tImage1.load(icon1);
                tImage1 = imageProcessor1.process(tImage1);
            }catch (Exception e){
                e.printStackTrace();
            }

            TensorImage tImage2 = null;
            try {
                Bitmap icon2 = BitmapFactory.decodeStream(new FileInputStream(f2));
                ImageProcessor imageProcessor2 =
                        new ImageProcessor.Builder()
                                .add(new ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
                                .add(new NormalizeOp(0, 255))
                                .build();
                tImage2 = new TensorImage(DataType.FLOAT32);
                tImage2.load(icon2);
                tImage2 = imageProcessor2.process(tImage2);
            }catch (Exception e){
                e.printStackTrace();
            }

            //new image (3) ///////////////////////
            ImageProcessor imageProcessor3 =
                    new ImageProcessor.Builder()
                            .add(new ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
                            .add(new NormalizeOp(0, 255))
                            .build();
            TensorImage tImage3 = new TensorImage(DataType.FLOAT32);
            tImage3.load(facebitmappart);
            tImage3 = imageProcessor3.process(tImage3);

            //Output
            Map<Integer, Object> output1 = new HashMap<Integer, Object>();
            Map<Integer, Object> output2 = new HashMap<Integer, Object>();
            float[][] outarr1 = new float[1][1];
            float[][] outarr2 = new float[1][1];
            output1.put(0, outarr1);
            output2.put(0, outarr2);
            //Run
            Object[] input1 = new Object[]{tImage1.getBuffer(), tImage3.getBuffer()};
            Object[] input2 = new Object[]{tImage2.getBuffer(), tImage3.getBuffer()};
            try {
                tflite.runForMultipleInputsOutputs(input1, output1);
                tflite.runForMultipleInputsOutputs(input2, output2);
                float[][] result1 = (float[][])output1.get(0);
                float[][] result2 = (float[][])output2.get(0);
                float number1 = (result1[0][0]);
                float number2 = (result2[0][0]);
                WakeUpNeo(number1, number2);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }else{
            Toast.makeText(BackService.this.getApplicationContext(), "None!", Toast.LENGTH_SHORT).show();
        }
    }

    private void WakeUpNeo(float flo, float at){
        float proverochka = Float.valueOf(String.valueOf(confvalues[1]));
        if (flo < proverochka || at < proverochka){
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "PowerManager:back");
            wakeLock.acquire();
            Log.d("Waker", "Wakeed with check!");
            startActivity(new Intent(this, Locker.class));
            stopSelf();
        }
    }

    private void WakeUpNeo(){
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "PowerManager:back");
        wakeLock.acquire();
        Log.d("Waker", "Wakeed without check!");
        startActivity(new Intent(this, Locker.class));
        stopSelf();
    }

    private void initialize_face_params(){

        if (face_x < 0) face_x = 0;
        if (face_y < 0) face_y = 0;
        if ((face_x + face_width) > bitmap.getWidth()) face_width = bitmap.getWidth() - face_x;
        if ((face_y + face_height) > bitmap.getHeight()) face_height = bitmap.getHeight() - face_y;
    }

    private MappedByteBuffer loadModelFile(Context context, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
