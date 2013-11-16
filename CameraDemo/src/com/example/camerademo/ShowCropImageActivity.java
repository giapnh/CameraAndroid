package com.example.camerademo;

import eu.janmuller.android.simplecropimage.CropImage;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowCropImageActivity extends Activity {
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_crop_image_layout);
		
		Bundle extra = getIntent().getExtras();
		bitmap = (Bitmap) extra.getParcelable(CropImage.RETURN_DATA_AS_BITMAP);
		
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageBitmap(bitmap);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		bitmap.recycle();
		bitmap = null;
	}
	
	
}
