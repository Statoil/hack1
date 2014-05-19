package com.statoil.glassistance;

import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MainActivity extends Activity {

    private GestureDetector gestureDetector;
	private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hilfe);

        gestureDetector = new GestureDetector(this);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                switch (gesture) {
                    case TAP:
                        startCamera();
                        return true;
                    case TWO_TAP:
                    	
                }
                return false;
            }
        });
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gestureDetector.onMotionEvent(event);
    }

    void startCamera() {
        camera = Camera.open();

        try {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            camera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();
    }
    
    @Override
    protected void onStop() {
    	if (camera != null)
    		camera.stopPreview();
    	
    	super.onStop();
    	
    }

}
