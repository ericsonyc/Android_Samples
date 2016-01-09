package com.learnopengles.android.lesson1;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LessonOneActivity extends Activity implements View.OnTouchListener {
    /**
     * Hold a reference to our GLSurfaceView
     */
    private GLSurfaceView mGLSurfaceView = null;
    private LessonOneRenderer renderer = null;

    private float previousX = 0.0f;
    private float previousY = 0.0f;

    public static final String TAG = "LessonOneActivity";

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float currentX = 0.0f;
        float currentY = 0.0f;
        float angleX = 0.0f;
        float angleY = 0.0f;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getRawX();
                previousY = event.getRawY();
                Log.i(TAG, "action_down");
                Log.i(TAG, "previousX:" + previousX + ",previousY:" + previousY);
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getRawX();
                currentY = event.getRawY();
                angleX = currentX - previousX;
//                angleX = (float) (angleX / (Math.PI) * 360);
                angleY = currentY - previousY;
//                angleY = (float) (angleY / Math.PI * 360);
                renderer.setCameraRotate(angleX, angleY);
                previousX = currentX;
                previousY = currentY;
                Log.i(TAG, "action_move");
                Log.i(TAG, "currentX:" + currentX + ",currentY:" + currentY);
                Log.i(TAG, "angleX:" + angleX + ",angleY:" + angleY);
                break;
            case MotionEvent.ACTION_UP:
                currentX = event.getRawX();
                currentY = event.getRawY();
                angleX = currentX - previousX;
                angleX = (float) (angleX / (Math.PI) * 360);
//                renderer.setCameraRotate((float) distance);
                Log.i(TAG, "action_up");
                Log.i(TAG, "currentX:" + currentX + ",currentY:" + currentY);
                Log.i(TAG, "distance:" + angleX);
                break;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setOnTouchListener(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000 || Build.FINGERPRINT.startsWith("generic");//保证在模拟器上运行opengl，在真机上运行opengl不需要该代码

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            renderer = new LessonOneRenderer();
            mGLSurfaceView.setRenderer(renderer);
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }
}