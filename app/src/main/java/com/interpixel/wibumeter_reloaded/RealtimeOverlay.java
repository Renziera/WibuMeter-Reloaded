package com.interpixel.wibumeter_reloaded;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RealtimeOverlay {

    private Canvas canvas;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private boolean surfaceReady;
    private Random random = new Random();
    private List<Wibu> wibuList = new ArrayList<>();

    public RealtimeOverlay(SurfaceView surfaceView){
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceReady = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceReady = false;
            }
        });

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void processResult(List<FirebaseVisionFace> faces){
        //only draw if surface has been created
        if(surfaceReady){
            //get canvas
            canvas = surfaceHolder.lockCanvas();
            //clear the canvas, PorterDuff is quite interesting tho
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            //iterate over the faces
            for(FirebaseVisionFace face : faces){
                Rect bounds = face.getBoundingBox();
                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                    int id = face.getTrackingId();
                    Log.d("Hmm", "face id " + id + " " + bounds.toString() + " rotY: " + rotY + " rotZ: " + rotZ);
                }
                paint.setColor(Color.BLUE);//TODO random color
                canvas.drawRect(bounds, paint);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private class Wibu{
        //start --> turningPoint --> target --> fluctuates
        boolean hasReachedTarget;
        boolean hasReachedTurningPoint;
        int target;
        int current;
        int turningPoint;
        int step;

        public Wibu(int start, int turning, int target, int step){
            current = start;
            this.target = target;
            this.step = step;
            if(start < target){
                turningPoint = target + turning;
            }else{
                turningPoint = target - turning;
            }
        }
    }
}
