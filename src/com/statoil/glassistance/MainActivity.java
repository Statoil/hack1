package com.statoil.glassistance;

import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            Log.d("Nils", "Camera found");
        }
        
        Camera camera = Camera.open();
        
       
        try {
			SurfaceView surfaceView = (SurfaceView)findViewById(R.id.capture_surface);
			camera.setPreviewDisplay(surfaceView.getHolder());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        camera.startPreview();
    }

}
