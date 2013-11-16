package com.example.camerademo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.views.CameraPreview;

import eu.janmuller.android.simplecropimage.CropImage;
import eu.janmuller.android.simplecropimage.Util;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Camera camera = null;
	private CameraInfo cameraInfo;
	private int orient = CameraInfo.CAMERA_FACING_BACK;
	
	private final int CROP_REQUEST_CODE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				camera.autoFocus(new AutoFocusCallback() {
					
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						Toast.makeText(MainActivity.this, "Focus " + success, Toast.LENGTH_LONG).show();
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
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						bitmap = Util.rotateImage(bitmap, cameraInfo.orientation);
						
						try {
							File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
							if(!file.exists()) {
								file.mkdir();
							}
							
							File imageFile = new File(file, "image.jpg");
							if(imageFile.exists()) {
								imageFile.createNewFile();
							}
							
							FileOutputStream outputStream = new FileOutputStream(imageFile);
							bitmap.compress(CompressFormat.JPEG, 100, outputStream);
							outputStream.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Intent intent = new Intent(MainActivity.this, CropImage.class);
				        intent.putExtra(CropImage.IMAGE_PATH, Environment.getExternalStorageDirectory() + "/DCIM/Camera/image.jpg");
						intent.putExtra(CropImage.RETURN_DATA, true);
				        intent.putExtra(CropImage.SCALE, true);

				        intent.putExtra(CropImage.ASPECT_X, 3);
				        intent.putExtra(CropImage.ASPECT_Y, 2);

				        startActivityForResult(intent, CROP_REQUEST_CODE);
				        
				        /*MainActivity.this.camera.stopPreview();
				        MainActivity.this.camera.release();
				        MainActivity.this.camera = null;*/
						
						/*Intent cropIntent = new Intent("com.android.camera.action.CROP");
						cropIntent.setType("image/*");
						Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/CameraTest/image.jpg");
						cropIntent.putExtra("data", bitmap);
						
						cropIntent.putExtra("crop", "true");
				        //indicate aspect of desired crop
				        cropIntent.putExtra("aspectX", 4);
				        cropIntent.putExtra("aspectY", 3);
				        //indicate output X and Y
				        cropIntent.putExtra("outputX", 800);
				        cropIntent.putExtra("outputY", 800);
				        
				        startActivity(cropIntent);*/
					}
				});
			}
		});
		
		Button switchButton = (Button) findViewById(R.id.switchCamera);
		switchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setMessage("Loading...");
				progressDialog.show();
				
				camera.stopPreview();
				camera.release();
				if(cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					camera = getCameraInstance(CameraInfo.CAMERA_FACING_FRONT);
					orient = CameraInfo.CAMERA_FACING_FRONT;
				} else {
					camera = getCameraInstance(CameraInfo.CAMERA_FACING_BACK);
					orient = CameraInfo.CAMERA_FACING_BACK;
				}
				
				final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.framlayout);
				frameLayout.removeAllViews();
				frameLayout.addView(new CameraPreview(MainActivity.this, camera));
				
				progressDialog.dismiss();
			}
		});
		
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(camera == null) {
			init();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	
	private void init() {
		if(checkCamera()) {
			camera = getCameraInstance(orient);
		} else {
			finish();
			return;
		}
		
		final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.framlayout);
		frameLayout.addView(new CameraPreview(this, camera));
		
		TextView numFocusAreaTextView = (TextView) findViewById(R.id.numFocusArea);
		//numFocusAreaTextView.setText("NumArea: " + camera.getParameters().getMaxNumFocusAreas());
		
		frameLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					/*float rateX = 2000/frameLayout.getWidth();
					float rateY = 2000/frameLayout.getHeight();
					int x = (int) (event.getX() * rateX - 1000);
					int y = (int) (event.getY() * rateY - 1000);
					
					Rect rect = new Rect(x - 50, y - 50, x + 50, y + 50);
					Area area = new Area(rect, 1000);
					List<Area> lisAreas = new ArrayList<Camera.Area>();
					lisAreas.add(area);
					
					camera.getParameters().setFocusAreas(lisAreas);*/
					
					camera.autoFocus(new AutoFocusCallback() {
						
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							Toast.makeText(MainActivity.this, "Focus " + success, Toast.LENGTH_LONG).show();
							
							TextView currentTextView = (TextView) findViewById(R.id.currentFocusArea);
							TextView distanceTextView = (TextView) findViewById(R.id.distanceToObject);
							
							currentTextView.setText("Current: " + event.getX() + " " + event.getY());
							float[] distance = new float[3];
							MainActivity.this.camera.getParameters().getFocusDistances(distance);
							distanceTextView.setText("Distance: " + distance[0] + " " + distance[1] + " " + distance[2]);
						}
					});
				}
				return false;
			}
		});
	}

	private boolean checkCamera() {
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Camera getCameraInstance(int orient) {
		Camera camera = null;
		int numCamera = Camera.getNumberOfCameras();
		
		cameraInfo = new CameraInfo();
		for(int i = 0; i < numCamera; i ++) {
			Camera.getCameraInfo(i, cameraInfo);
			if(cameraInfo.facing == orient) {
				camera = Camera.open(i);
				setCameraDisplayOrientation(this, i, camera);
				//camera.setDisplayOrientation(cameraInfo.orientation);
				return camera;
			}
		}
		
		return null;
	}
	
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CROP_REQUEST_CODE) {
			if(resultCode == RESULT_OK) {
				Bitmap bitmap = (Bitmap) data.getParcelableExtra(CropImage.RETURN_DATA_AS_BITMAP);
				
				Intent intent = new Intent(this, ShowCropImageActivity.class);
				intent.putExtra(CropImage.RETURN_DATA_AS_BITMAP, bitmap);
				startActivityForResult(intent, -CROP_REQUEST_CODE);
			}
		}
	}

}
