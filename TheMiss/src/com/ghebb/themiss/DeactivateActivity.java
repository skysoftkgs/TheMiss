package com.ghebb.themiss;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gab.themiss.R;

public class DeactivateActivity extends Activity implements OnClickListener{
	
	public static final String TAG = "DeactivateActivity";
	MainActivity mActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deactivate);
		
		TheMissApplication.getInstance().setLanguage();
		
		ImageButton  noButton = (ImageButton) this.findViewById(R.id.ib_logout_no);
		noButton.setOnClickListener(this);
		
		TextView  yesTextView = (TextView) this.findViewById(R.id.tv_logout_yes);
		yesTextView.setOnClickListener(this);
		
		Button backButton = (Button) this.findViewById(R.id.btn_logout_back);
		backButton.setOnClickListener(this);
	}


	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch(v.getId()){
			case R.id.ib_logout_no:
				finish();
				break;
			
			case R.id.tv_logout_yes:
				deactivate();
				break;
				
			case R.id.btn_logout_back:
				super.onBackPressed();
				break;
		}
	}
	
	public void deactivate(){
		setResult(Activity.RESULT_OK);
		finish();		
	}
}
