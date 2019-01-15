package com.interpixel.wibumeter_reloaded;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;

public class DetectionActivity extends AppCompatActivity {

    private static final String TAG = "Hmm";
    private static final int PERMISSION_REQ_CAM = 1412;

    private FirebaseVisionFaceDetector detector;
    private FirebaseVisionImage image;
    private Surface cameraSurface;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback cameraStateCallback;
    private CameraCaptureSession captureSession;
    private String[] cameraId;  //kamera depan atau belakang (atau samping wkwk)
    private boolean surfaceReady = false;
    private boolean isPreviewing = false;
    private boolean useFrontCamera;

    private void toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        useFrontCamera = getIntent().getBooleanExtra("front", false);

        //Siapin options face detector
        FirebaseVisionFaceDetectorOptions realTimeOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.1f)
                .enableTracking()
                .build();
        //bikin face detector nya
        detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOptions);

        //Siapin kamera
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList();
            for (String id : cameraId){
                Log.d(TAG, "Camera Id: " + id);
            }
        }catch (CameraAccessException e){
            toast("Error accessing camera");
            e.printStackTrace();
            finish();
        }
        cameraStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG, "Camera opened");
                cameraDevice = camera;
                startPreviewCamera();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(TAG, "Camera disconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(TAG, "Camera Error");
            }
        };


        //Siapin surface view (untuk preview kamera)
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");
                cameraSurface = holder.getSurface();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged");
                cameraSurface = holder.getSurface();
                surfaceReady = true;
                startPreviewCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed");
                surfaceReady = false;
            }
        };
        surfaceHolder.addCallback(surfaceHolderCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //request permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQ_CAM);
        }else{
            Log.d(TAG, "Camera permission granted");
            try {
                if(useFrontCamera && cameraId.length > 1){
                    cameraManager.openCamera(cameraId[1], cameraStateCallback, new Handler());
                }else{
                    cameraManager.openCamera(cameraId[0], cameraStateCallback, new Handler());
                }
            }catch (CameraAccessException e){
                toast("Error accessing camera");
                e.printStackTrace();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Release semua resource
        if(captureSession != null){
            try {
                captureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            captureSession.close();
            captureSession = null;
        }
        isPreviewing = false;
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //cek permission
        if (requestCode == PERMISSION_REQ_CAM){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Camera permission granted");
                try {
                    cameraManager.openCamera(cameraId[0], cameraStateCallback, new Handler());
                }catch (CameraAccessException e){
                    toast("Error accessing camera");
                    e.printStackTrace();
                    finish();
                }
            }else{
                toast("Camera permission not granted");
                finish();
            }
        }
    }

    private void startPreviewCamera(){
        //mulai preview kamera ke surface
        if(surfaceReady && cameraDevice != null && !isPreviewing){
            // prepare list of surfaces to be used in capture requests
            List<Surface> sfl = new ArrayList<Surface>();
            sfl.add(cameraSurface);
            //Bikin capture session
            try{
                cameraDevice.createCaptureSession(sfl, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "Capture session configured");
                        captureSession = session;

                        try{
                            CaptureRequest.Builder previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            previewRequestBuilder.addTarget(cameraSurface);
                            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "Capture session failed to be configured");
                    }
                }, null);
            }catch(CameraAccessException e){
                e.printStackTrace();
            }
            Log.d(TAG, "Camera preview started");
            isPreviewing = true;
        }
    }

    private void parseFromCamera(Image mediaImage, int rotation){
        image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

    }



    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("Hmm", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

}
