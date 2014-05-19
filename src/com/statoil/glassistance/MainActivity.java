package com.statoil.glassistance;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity {
	private static final String API_URL = "http://hackathon1.azurewebsites.net/api/image";
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
				postQuestionAsJson(data);

			}
		});
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return gestureDetector.onMotionEvent(event);
	}

	void startCamera() {
		camera = Camera.open();
		text.setText("Swipe back to take a picture!");
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
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	void postQuestionAsJson(byte[] theQuestion) {
		try {
			AsyncHttpClient httpclient = new AsyncHttpClient();
			HttpEntity entity = new StringEntity("\"" + Base64.encodeToString(theQuestion, Base64.DEFAULT) + "\"");
			httpclient.post(getBaseContext(), API_URL, entity, "application/json", new PostResponseHandler());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Oops", e);
		}
		
	}
	
	void postQuestionAsFile(byte[] theQuestion) {
		// Create a new HttpClient and Post Header
		AsyncHttpClient httpclient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("question", new ByteArrayInputStream(theQuestion), "question.jpg");
		httpclient.post(getBaseContext(), API_URL, null, params,
				"application/json", new PostResponseHandler());
	}

	byte[] getAnswer() {
		InputStream inputStream = null;
		String resultAsString = "";
		byte[] pic;
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			URI url = URI.create(API_URL);
			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null) {
				resultAsString = convertInputStreamToString(inputStream);
				Log.e("Jepp", resultAsString);
				pic = Base64.decode(resultAsString, Base64.DEFAULT);
				return pic;
			} else
				resultAsString = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return null;
	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}
	
	private final class PostResponseHandler extends AsyncHttpResponseHandler {
		@Override
		public void onSuccess(String content) {
			text.setText("Great success!");
		}
		
		@Override
		public void onFailure(int statusCode, Throwable error, String content) {
			text.setText("Epic fail");
		}
	}
}
