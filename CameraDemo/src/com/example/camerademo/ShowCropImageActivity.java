package com.example.camerademo;

import java.util.ArrayList;

import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import eu.janmuller.android.simplecropimage.CropImage;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodSession;
import android.view.inputmethod.InputMethod.SessionCallback;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowCropImageActivity extends Activity {
	private Bitmap bitmap;
	private UiLifecycleHelper uiLifecycleHelper;
	private Session.StatusCallback callback;
	
	private boolean waitToShareImage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_crop_image_layout);
		
		Bundle extra = getIntent().getExtras();
		bitmap = (Bitmap) extra.getParcelable(CropImage.RETURN_DATA_AS_BITMAP);
		
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageBitmap(bitmap);
		
		callback = new StatusCallback() {
			
			@Override
			public void call(Session session, SessionState state, Exception exception) {
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
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		uiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		uiLifecycleHelper.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}

	private void onSessionChangeState(Session session, SessionState state, Exception exception) {
		if(state.isOpened()) {
			if(waitToShareImage) {
				if(session.getPermissions().contains("publish_stream")) {
					shareImage(bitmap);
				} else {
					ArrayList<String> listPermission = new ArrayList<String>();
					listPermission.add("publish_stream");
					session.requestNewPublishPermissions(new NewPermissionsRequest(this, listPermission));
					Request request = Request.newMeRequest(session, new GraphUserCallback() {
						
						@Override
						public void onCompleted(GraphUser user, Response response) {
							if(response.getError() == null && waitToShareImage) {
								shareImage(bitmap);
								Toast.makeText(ShowCropImageActivity.this, "request permission success!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ShowCropImageActivity.this, "request permission fail!", Toast.LENGTH_SHORT).show();
							}
						}
					});
					request.executeAsync();
				}
			}
		}
	}
	
	private void shareImage(Bitmap bitmap) {
		if(bitmap != null) {
			Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), bitmap, new Callback() {
				
				@Override
				public void onCompleted(Response response) {
					if(response.getError() != null) {
						Toast.makeText(ShowCropImageActivity.this, "Cann't share image!", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ShowCropImageActivity.this, "Share image success!", Toast.LENGTH_SHORT).show();
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
	
}
