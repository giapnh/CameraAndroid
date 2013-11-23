package com.gnd.main;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
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
import com.gnd.main.bluetooth.BluetoothService;
import com.gnd.main.bluetooth.DeviceListActivity;
import com.gnd.main.camera.CropImage;
import com.gnd.main.network.Argument;
import com.gnd.main.network.Command;
import com.gnd.main.utils.D;

public class ShowCropImageActivity extends Activity implements OnTouchListener {
	private Bitmap bitmap;
	private UiLifecycleHelper uiLifecycleHelper;
	private Session.StatusCallback callback;

	private boolean waitToShareImage = false;
	ScaleGestureDetector scaleDetector;
	GestureDetector gestureDetector;
	public float scaleAmount = 1;
	ImageView imageView;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Bluetooth service
	// Name of the connected device
	public static String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	// Member object for the chat services

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					break;
				case BluetoothService.STATE_CONNECTING:
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
			case MESSAGE_READ:
				Command cmd = (Command) msg.obj;
				D.i(cmd.toString());
				Bitmap bm = BitmapFactory.decodeByteArray(
						cmd.getRaw(Argument.ARG_IMAGE), 0,
						cmd.getRaw(Argument.ARG_IMAGE).length);
				imageView.setImageBitmap(bm);
				bitmap.recycle();
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						BluetoothService.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_crop_image_layout);

		Bundle extra = getIntent().getExtras();
		bitmap = (Bitmap) extra.getParcelable(CropImage.RETURN_DATA_AS_BITMAP);

		imageView = (ImageView) findViewById(R.id.imageView);
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
		// Get local Bluetooth adapter
		BluetoothService.getInstance().mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
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
		// Stop the Bluetooth chat services
		if (BluetoothService.instance != null)
			BluetoothService.instance.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiLifecycleHelper.onResume();
		if (BluetoothService.instance != null) {
			if (BluetoothService.instance.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				BluetoothService.instance.start();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		BluetoothService.getInstance().requireEnable();
		setup();
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
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				setup();
			} else {
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	public void setup() {
		BluetoothService.instance.registContext(ShowCropImageActivity.this);
		BluetoothService.instance.registHandler(mHandler);
	}

	private void ensureDiscoverable() {
		if (BluetoothService.getInstance().mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = BluetoothService.getInstance().mBluetoothAdapter
				.getRemoteDevice(address);
		// Attempt to connect to the device
		BluetoothService.instance.connect(device, secure);
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

	public void onPair(View v) {
		if (BluetoothService.instance.getState() != BluetoothService.STATE_CONNECTED) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return;
		}
	}

	public void sendImg(View v) {
		// Check that we're actually connected before trying anything
		if (BluetoothService.instance.getState() != BluetoothService.STATE_CONNECTED) {
			ensureDiscoverable();
			return;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		BluetoothService.instance.sendImage(byteArray);
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
