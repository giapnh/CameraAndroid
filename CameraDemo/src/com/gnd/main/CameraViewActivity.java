package com.gnd.main;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gnd.R;
import com.gnd.main.camera.CameraPreview;
import com.gnd.main.camera.CropImage;
import com.gnd.main.camera.Util;
import com.gnd.main.config.Config;

public class CameraViewActivity extends Activity {
	private Camera camera = null;
	private CameraInfo cameraInfo;
	private int orient = CameraInfo.CAMERA_FACING_BACK;

	private final int CROP_REQUEST_CODE = 10;
	Context context;

	View screen;
	Bitmap bmScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gnd_camera_layout);

		screen = (View) findViewById(R.id.frameLayout);

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				camera.autoFocus(new AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						Toast.makeText(CameraViewActivity.this,
								"Focus " + success, Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		Button captureButton = (Button) findViewById(R.id.capture);
		captureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				camera.takePicture(null, null, new PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
								data.length);
						bitmap = Util.rotateImage(bitmap,
								cameraInfo.orientation);
						try {
							File file = new File(Environment
									.getExternalStorageDirectory()
									+ "/DCIM/Camera");
							if (!file.exists()) {
								file.mkdir();
							}
							File imageFile = new File(file, "image.jpg");
							if (imageFile.exists()) {
								imageFile.createNewFile();
							}

							FileOutputStream outputStream = new FileOutputStream(
									imageFile);
							bitmap.compress(CompressFormat.JPEG, 100,
									outputStream);
							outputStream.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Intent intent = new Intent(CameraViewActivity.this,
								CropImage.class);
						intent.putExtra(CropImage.IMAGE_PATH,
								Environment.getExternalStorageDirectory()
										+ "/DCIM/Camera/image.jpg");
						intent.putExtra(CropImage.RETURN_DATA, true);
						intent.putExtra(CropImage.SCALE, true);

						intent.putExtra(CropImage.ASPECT_X, Config.ASPECT_X);
						intent.putExtra(CropImage.ASPECT_Y, Config.ASPECT_Y);

						startActivityForResult(intent, CROP_REQUEST_CODE);
					}
				});
			}
		});

		Button switchButton = (Button) findViewById(R.id.switchCamera);
		switchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProgressDialog progressDialog = new ProgressDialog(
						CameraViewActivity.this);
				progressDialog.setMessage("Loading...");
				progressDialog.show();

				camera.stopPreview();
				camera.release();
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					camera = getCameraInstance(CameraInfo.CAMERA_FACING_FRONT);
					orient = CameraInfo.CAMERA_FACING_FRONT;
				} else {
					camera = getCameraInstance(CameraInfo.CAMERA_FACING_BACK);
					orient = CameraInfo.CAMERA_FACING_BACK;
				}

				final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera);
				frameLayout.removeAllViews();
				frameLayout.addView(new CameraPreview(CameraViewActivity.this,
						camera));

				progressDialog.dismiss();
			}
		});

		init();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (camera == null) {
			init();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	private void init() {
		if (checkCamera()) {
			camera = getCameraInstance(orient);
		} else {
			finish();
			return;
		}

		final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera);
		frameLayout.addView(new CameraPreview(this, camera));

		frameLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					camera.autoFocus(new AutoFocusCallback() {

						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							Toast.makeText(CameraViewActivity.this,
									"Focus " + success, Toast.LENGTH_LONG)
									.show();

							float[] distance = new float[3];
							CameraViewActivity.this.camera.getParameters()
									.getFocusDistances(distance);
						}
					});
				}
				return false;
			}
		});
	}

	private boolean checkCamera() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	public Camera getCameraInstance(int orient) {
		Camera camera = null;
		int numCamera = Camera.getNumberOfCameras();

		cameraInfo = new CameraInfo();
		for (int i = 0; i < numCamera; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == orient) {
				camera = Camera.open(i);
				setCameraDisplayOrientation(this, i, camera);
				// camera.setDisplayOrientation(cameraInfo.orientation);
				return camera;
			}
		}

		return null;
	}

	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CROP_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bitmap bitmap = (Bitmap) data
						.getParcelableExtra(CropImage.RETURN_DATA_AS_BITMAP);

				Intent intent = new Intent(this, EditableActivity.class);
				intent.putExtra(CropImage.RETURN_DATA_AS_BITMAP, bitmap);
				startActivityForResult(intent, -CROP_REQUEST_CODE);
			}
		}
	}

}
