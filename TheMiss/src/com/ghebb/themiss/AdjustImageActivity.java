package com.ghebb.themiss;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import com.gab.themiss.R;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.UtilityMethods;
import com.polites.android.GestureImageView;

public class AdjustImageActivity extends Activity implements OnClickListener{

	GestureImageView mImageView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_adjust_image);
		
		mImageView = (GestureImageView) findViewById(R.id.imageView);
		ViewGroup.LayoutParams imageViewParams = mImageView.getLayoutParams();
	    imageViewParams.height = AppManager.mScreenWidth;
	    mImageView.setLayoutParams(imageViewParams);
		mImageView.setImageBitmap(UtilityMethods.getLocalBitmap(this));
		
		ImageButton acceptImageButton = (ImageButton) findViewById(R.id.imageButton_accept);
		acceptImageButton.setOnClickListener(this);
		
		ImageButton closeImageButton = (ImageButton) findViewById(R.id.imageButton_close);
		closeImageButton.setOnClickListener(this);
		
		ImageButton rotateImageButton = (ImageButton) findViewById(R.id.imageButton_rotate);
		rotateImageButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.imageButton_accept:
				Intent acceptIntent = new Intent();
				mImageView.buildDrawingCache();
				UtilityMethods.setLocalBitmap(mImageView.getDrawingCache(), this);
				setResult(RESULT_OK, acceptIntent);
				finish();
				break;
			
			case R.id.imageButton_close:
				Intent closeIntent = new Intent();
				setResult(RESULT_CANCELED, closeIntent);
				finish();
				break;
				
			case R.id.imageButton_rotate:
				Bitmap bm1 = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
				Bitmap bm2 = UtilityMethods.rotateBitmapBy90(bm1);
				mImageView.setLayout(false);
				mImageView.setImageBitmap(bm2);
				break;
			
		}
	}
	
}
