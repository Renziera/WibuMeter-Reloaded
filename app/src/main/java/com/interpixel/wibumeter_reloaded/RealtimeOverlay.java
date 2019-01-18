package com.interpixel.wibumeter_reloaded;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.core.content.res.ResourcesCompat;

public class RealtimeOverlay {

    private Canvas canvas;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private boolean surfaceReady;
    private Random random = new Random();
    private List<Wibu> wibuList = new ArrayList<>();

    public RealtimeOverlay(SurfaceView surfaceView, Context context){
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
                Log.d("Hmm", "surfaceChanged: " + format + " width: " + width + " height: " + height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceReady = false;
            }
        });

        paint = new Paint();
        paint.setStrokeWidth(7);

        Typeface tf = ResourcesCompat.getFont(context, R.font.maven_pro_medium);
        paint.setTypeface(tf);
    }

    public void setSurfaceSize(int width, int height){
        surfaceHolder.setFixedSize(width, height);
    }

    public void processResult(List<FirebaseVisionFace> faces){
        //only draw if surface has been created
        if(surfaceReady){
            //get canvas
            canvas = surfaceHolder.lockCanvas();
            //clear the canvas, PorterDuff is quite interesting tho
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(50);
            canvas.drawText("WibuMeter Engine", 0, paint.getFontMetrics().descent - paint.getFontMetrics().ascent, paint);
            //iterate over the faces
            for(FirebaseVisionFace face : faces){
                Rect bounds = face.getBoundingBox();

                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                    String level = "";
                    int id = face.getTrackingId();  //mulai dari 0, wajah baru increment terus
                    if(id < wibuList.size()){   //sudah ada
                        Wibu wibu = wibuList.get(id);
                        paint.setColor(wibu.getColor());
                        level = wibu.getCurrentLevel();
                    }else{  //baru
                        paint.setARGB(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        wibuList.add(new Wibu(paint.getColor()));
                        level = wibuList.get(id).getCurrentLevel();
                    }
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(bounds, paint);
                    paint.setStyle(Paint.Style.FILL);
                    setTextSize(paint, bounds.width(), level);
                    canvas.drawText(level, bounds.left, bounds.bottom + paint.getFontMetrics().descent - paint.getFontMetrics().ascent, paint);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
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

    private class Wibu{
        //start --> turningPoint --> target --> fluctuates
        boolean hasReachedTarget;
        boolean hasReachedTurningPoint;
        boolean converge; //start > target
        int target;
        int current;
        int turningPoint;
        int step;
        int color;

        public Wibu(int color){
            current = random.nextInt(7000) + 500;
            if(random.nextBoolean()){
                target = current + random.nextInt(2000);
                turningPoint = target + random.nextInt(500);
                converge = false;
            }else{
                target = current - random.nextInt(2000);
                turningPoint = target - random.nextInt(500);
                converge = true;
            }
            step = random.nextInt(50);
            this.color = color;
        }

        public String getCurrentLevel(){
            if(hasReachedTarget){   //fluctuates
                if (random.nextBoolean()){
                    return "" + (target + random.nextInt(10));
                }else{
                    return "" + (target - random.nextInt(10));
                }
            }else{
                if (hasReachedTurningPoint){
                    if(converge){
                        current = current + step;
                        if(current > target){
                            hasReachedTarget = true;
                        }
                    }else{
                        current = current - step;
                        if(current < target){
                            hasReachedTarget = true;
                        }
                    }
                }else{
                    if(converge){
                        current = current - step;
                        if(current < turningPoint){
                            hasReachedTurningPoint = true;
                        }
                    }else{
                        current = current + step;
                        if(current > turningPoint){
                            hasReachedTurningPoint = true;
                        }
                    }
                }
            }
            return Integer.toString(current);
        }

        public int getColor(){
            return color;
        }

    }
}
