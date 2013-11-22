package com.gnd.main;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.gnd.R;
import com.gnd.main.camera.CropImage;

public class ShowCropImageActivity extends Activity implements OnTouchListener {
	private Bitmap bitmap;
	private UiLifecycleHelper uiLifecycleHelper;
	private Session.StatusCallback callback;

	private boolean waitToShareImage = false;
	ScaleGestureDetector scaleDetector;
	GestureDetector gestureDetector;
	public float scaleAmount = 1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_crop_image_layout);

		Bundle extra = getIntent().getExtras();
		bitmap = (Bitmap) extra.getParcelable(CropImage.RETURN_DATA_AS_BITMAP);

		final ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageBitmap(bitmap);

		callback = new StatusCallback() {

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				onSessionChangeState(session, state, exception);
			}
		};
		uiLifecycleHelper = new UiLifecycleHelper(this, callback);
		uiLifecycleHelper.onCreate(savedInstanceState);

		Button shareButton = (Button) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				waitToShareImage = true;
				startLogin();
			}
		});

		scaleDetector = new ScaleGestureDetector(this,
				new OnScaleGestureListener() {

					@Override
					public void onScaleEnd(ScaleGestureDetector detector) {

						System.out.println("Scale end");
					}

					@Override
					public boolean onScaleBegin(ScaleGestureDetector detector) {
						System.out.println("Scale begin");
						return true;
					}

					float MAX = 4;
					float MIN = 0.5f;

					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						if (scaleAmount + detector.getScaleFactor() - 1 > MIN && //
								scaleAmount + detector.getScaleFactor() - 1 < MAX) {
							scaleAmount = scaleAmount
									+ detector.getScaleFactor() - 1;
							imageView.setScaleX(imageView.getScaleX()
									+ detector.getScaleFactor() - 1);
							imageView.setScaleY(imageView.getScaleY()
									+ detector.getScaleFactor() - 1);
						}
						System.out.println("On Scale");
						return true;
					}
				});

		
		gestureDetector = new GestureDetector(new OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
				System.out.println("Single tap up");
				return false;
			}

			@Override
			public void onShowPress(MotionEvent paramMotionEvent) {
				System.out.println("Show press");
			}

			@Override
			public boolean onScroll(MotionEvent paramMotionEvent1,
					MotionEvent paramMotionEvent2, float paramFloat1,
					float paramFloat2) {
				System.out.println("onScroll");
				return false;
			}

			@Override
			public void onLongPress(MotionEvent paramMotionEvent) {

			}

			@Override
			public boolean onFling(MotionEvent paramMotionEvent1,
					MotionEvent paramMotionEvent2, float paramFloat1,
					float paramFloat2) {
				System.out.println("Fling");
				return false;
			}

			@Override
			public boolean onDown(MotionEvent paramMotionEvent) {
				System.out.println("Down");
				return false;
			}
		});

		imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				gestureDetector.onTouchEvent(arg1);
				return false;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scaleDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		uiLifecycleHelper.onDestroy();
		bitmap.recycle();
		bitmap = null;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		uiLifecycleHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiLifecycleHelper.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}

	private void onSessionChangeState(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			if (waitToShareImage) {
				if (session.getPermissions().contains("publish_stream")) {
					shareImage(bitmap);
				} else {
					ArrayList<String> listPermission = new ArrayList<String>();
					listPermission.add("publish_stream");
					session.requestNewPublishPermissions(new NewPermissionsRequest(
							this, listPermission));
					Request request = Request.newMeRequest(session,
							new GraphUserCallback() {

								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (response.getError() == null
											&& waitToShareImage) {
										shareImage(bitmap);
										Toast.makeText(
												ShowCropImageActivity.this,
												"request permission success!",
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(
												ShowCropImageActivity.this,
												"request permission fail!",
												Toast.LENGTH_SHORT).show();
									}
								}
							});
					request.executeAsync();
				}
			}
		}
	}

	private void shareImage(Bitmap bitmap) {
		if (bitmap != null) {
			Request request = Request.newUploadPhotoRequest(
					Session.getActiveSession(), bitmap, new Callback() {

						@Override
						public void onCompleted(Response response) {
							if (response.getError() != null) {
								Toast.makeText(ShowCropImageActivity.this,
										"Cann't share image!",
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ShowCropImageActivity.this,
										"Share image success!",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
			request.executeAsync();
		}

		waitToShareImage = false;
	}

	private void startLogin() {
		Session.openActiveSession(this, true, callback);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		scaleDetector.onTouchEvent(event);
		return false;
	}

}
