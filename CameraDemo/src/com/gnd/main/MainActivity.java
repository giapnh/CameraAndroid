package com.gnd.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gnd.R;
import com.gnd.main.bluetooth.BluetoothService;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gnd_main_layout);
		BluetoothService.getInstance();
	}

	public void onFunnyFoto(View v) {
		// TODO
		Intent intent = new Intent(MainActivity.this, CameraViewActivity.class);
		startActivity(intent);
	}

	public void onFunnyGame(View v) {
		// TODO
	}

	public void onMyGallery(View v) {
		// TODO
	}
}
