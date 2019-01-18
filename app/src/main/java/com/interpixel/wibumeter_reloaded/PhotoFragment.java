package com.interpixel.wibumeter_reloaded;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

public class PhotoFragment extends Fragment {

    private static final int REQ_IMAGE = 8008;
    private static final int PERMISSION_REQ_READ = 1920;

    private FirebaseVisionFaceDetector detector;
    private ProgressBar progressBar;
    private TextView status;
    private ImageView imageView;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private boolean photoExist;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //siapin detector akurat
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.1f)
                .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setTextSize(20);

        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.maven_pro_medium);
        paint.setTypeface(tf);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        status = view.findViewById(R.id.status);
        imageView = view.findViewById(R.id.imageView);
        view.findViewById(R.id.pickButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
                Random random = new Random();
                if(random.nextBoolean() && random.nextBoolean() && random.nextBoolean()){
                    AdHelper.getAdHelper().ShowAds();
                }
            }
        });
        view.findViewById(R.id.analyzeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzePhoto();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_IMAGE && resultCode == Activity.RESULT_OK){

            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                imageView.setImageBitmap(bitmap);
                progressBar.setSecondaryProgress(50);
                progressBar.setProgress(0);
                status.setText("Photo ready to be analyzed.");
                photoExist = true;
            }
        }
    }

    public void pickPhoto(){
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQ_READ);
        }else{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_IMAGE);
        }
    }

    public void analyzePhoto(){
        if(photoExist){
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> faces) {
                            progressBar.setProgress(progressBar.getMax());
                            status.setText("Photo analyzing completed.");
                            processResult(faces);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setProgress(0);
                            status.setText("Photo analyzing failed.");
                        }
                    });
            progressBar.setSecondaryProgress(progressBar.getMax());
            progressBar.setProgress(0);
            status.setText("Analyzing photo.");
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(progressBar.getProgress() < 98){
                        progressBar.incrementProgressBy(2);
                        status.setText("Analyzing photo " + progressBar.getProgress() + " %");
                        handler.postDelayed(this, 100);
                    }
                }
            });
        }else{
            status.setText("No photo selected.");
        }
    }

    private void processResult(List<FirebaseVisionFace> faces){
        if(faces.size() == 0){
            status.setText("Photo analyzing completed. No weeb detected.");
            return;
        }
        //Harus mutable bitmap
        Bitmap drawingBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(drawingBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        for(FirebaseVisionFace face : faces){
            /**
            float rotY = face.getHeadEulerAngleY();
            float rotZ = face.getHeadEulerAngleZ();
            float smile = face.getSmilingProbability();
            float leftEye = face.getLeftEyeOpenProbability();
            float rightEye = face.getRightEyeOpenProbability();
             */
            Rect rect = face.getBoundingBox();
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);

            FirebaseVisionFaceLandmark mouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
            FirebaseVisionFaceLandmark left_eye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
            FirebaseVisionFaceLandmark right_eye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
            FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);

            String wibu = "";

            if(mouth == null || left_eye == null || right_eye == null || nose == null){
                wibu = "INVALID";
            }else{
                wibu = "" + (int) Math.ceil(Math.abs(
                        mouth.getPosition().getX() * mouth.getPosition().getY() +
                        left_eye.getPosition().getX() * left_eye.getPosition().getY() +
                        right_eye.getPosition().getX() * right_eye.getPosition().getY() +
                        nose.getPosition().getX() * nose.getPosition().getY()
                )) % 10000;
            }

            setTextSize(paint, rect.width(), wibu);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(wibu, rect.left, rect.bottom + paint.getFontMetrics().descent - paint.getFontMetrics().ascent, paint);

            List<FirebaseVisionPoint> allContourPoints = face.getContour(FirebaseVisionFaceContour.ALL_POINTS).getPoints();

            for(FirebaseVisionPoint point : allContourPoints){
                canvas.drawPoint(point.getX(), point.getY(), paint);
            }

            Random random = new Random();
            paint.setARGB(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        imageView.setImageBitmap(drawingBitmap);
    }

    private void setTextSize(Paint paint, float desiredWidth, String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 24f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }
}
