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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Visibility;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity {
	private static final String API_URL = "http://glassistance.herokuapp.com/image.jpg";
	private static final String API_URL_RESPONSE = "http://glassistance.herokuapp.com/image.jpg";
	private static final String TAG = "Glassistance";
	private GestureDetector gestureDetector;
	private Camera camera;
	private TextView text;
	private AsyncHttpClient httpclient;
	private SurfaceView cameraView;
	private SurfaceView responseView;
	private Paint paint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.hilfe);

		text = (TextView) findViewById(R.id.bottom_text);
		
		cameraView = (SurfaceView) findViewById(R.id.surface_view);
		responseView = (SurfaceView) findViewById(R.id.surface_view_answer);
		
		httpclient = new AsyncHttpClient();
		
		paint = new Paint();
		paint.setColor(0xFFFFFFFF);
	    paint.setStyle(Style.STROKE);
	    
	    setView(ViewType.CAMERA);

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
				case SWIPE_RIGHT:
					releaseCamera();
					getAnswer();
					return true;
				}
				return false;
			}
		});
	}

	protected void takePicture() {
		camera.takePicture(null, null, null, new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				releaseCamera();
				text.setText("Sending picture!");
				postQuestionAsJson(data);
			}
		});
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return gestureDetector.onMotionEvent(event);
	}

	void startCamera() {
		setView(ViewType.CAMERA);
		
		camera = Camera.open();
		
		text.setText("Swipe back to take a picture!");
		try {
			camera.setPreviewDisplay(cameraView.getHolder());
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
	
	enum ViewType {
		CAMERA, RESPONSE;
	}

	private void setView(ViewType type) {
		switch (type) {
		case CAMERA:
			cameraView.setVisibility(View.VISIBLE);
			responseView.setVisibility(View.INVISIBLE);
			break;
		case RESPONSE:
			cameraView.setVisibility(View.INVISIBLE);
			responseView.setVisibility(View.VISIBLE);
			break;
		}
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

	void getAnswer() {
		httpclient.get(API_URL_RESPONSE, new AnswerResponseHandler());
	}

	private final class AnswerResponseHandler extends
			AsyncHttpResponseHandler {
		@Override
		public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
			final Bitmap img = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
			paintBitmap(img, responseView);
			text.setText("Help is here!");
		}
		
		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			text.setText("Oh no!");
			text.setTextColor(Color.RED);
		}
	}

	private final class PostResponseHandler extends AsyncHttpResponseHandler {
		@Override
		public void onSuccess(String content) {
			text.setText("Swipe forward for response");
		}
		
		@Override
		public void onFailure(int statusCode, Throwable error, String content) {
			text.setText("Connection error");
		}
	}

	public void paintBitmap(final Bitmap img, final SurfaceView surface) {
		setView(ViewType.RESPONSE);
		final SurfaceHolder holder = surface.getHolder();
		final Canvas canvas = holder.lockCanvas();
		canvas.drawColor(Color.BLACK);
		Bitmap scaled = Bitmap.createScaledBitmap(img, canvas.getWidth(), canvas.getHeight(), true);
		canvas.drawBitmap(scaled, 0, 0, null);
		
		holder.unlockCanvasAndPost(canvas);
		Log.d(TAG, "Painted surface");

	}
	
	
}
