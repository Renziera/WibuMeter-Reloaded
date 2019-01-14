package com.interpixel.wibumeter_reloaded;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

public class DetectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        FirebaseVisionFaceDetectorOptions realTimeOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.2f)
                .enableTracking()
                .build();
    }
}
