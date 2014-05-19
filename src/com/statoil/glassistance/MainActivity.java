package com.statoil.glassistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MainActivity extends Activity {
    private static final String TAG = "Glassistance";
    private GestureDetector gestureDetector;
	private Camera camera;
	private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.hilfe);
        
        text = (TextView) findViewById(R.id.bottom_text);

        setupGestures();
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                switch (gesture) {
                    case TAP:
                        startCamera();
                        return true;
                    case SWIPE_LEFT:
                        takePicture();
                        return true;
                }
                return false;
            }
        });
    }

    protected void takePicture() {
		// TODO Auto-generated method stub
		camera.takePicture(null, null, null, new Camera.PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				releaseCamera();
				text.setText("Jolly good!");
				postQuestion(data);
				
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
    	releaseCamera();
    	super.onStop();
    	
    }
    
    @Override
    protected void onPause() {
    	releaseCamera();
    	super.onPause();
    }
    
    void releaseCamera() {
    	if(camera != null) {
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}
    }

    void postQuestion(byte[] theQuestion) {
    	try {
    		HttpClient httpclient = new DefaultHttpClient();
    		URI url = URI.create("http://hackathon1.azurewebsites.net/api/image");
    		
    	} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
    }
    byte[] getAnswer() {
		InputStream inputStream = null;
		String resultAsString = "";
		byte[] pic;
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			URI url = URI.create("http://hackathon1.azurewebsites.net/api/image");
			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url ));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null) {
				resultAsString = convertInputStreamToString(inputStream);
				Log.e("Jepp", resultAsString);
				pic = Base64.decode(resultAsString, Base64.DEFAULT);
			    return pic;
			}
			else
				resultAsString = "Did not work!";
		    
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return null;
	}
	 
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
    }
}
