package com.ghebb.themiss;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.Session;
import com.gab.themiss.R;
import com.ghebb.themiss.common.Constants;
import com.parse.ParseUser;

public class LogoutActivity extends Activity implements OnClickListener{
	
	public static final String TAG = "LogoutActivity";
	MainActivity mActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logout);
		
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
				logOut();
				break;
				
			case R.id.btn_logout_back:
				super.onBackPressed();
				break;
		}
	}
	
	public void logOut(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = pref.edit();
        edit.putBoolean(Constants.PREF_LOGGEDIN, false);
     	edit.commit();
     	
		if(Session.getActiveSession() != null){
			Session.getActiveSession().closeAndClearTokenInformation();
		}
		ParseUser.logOut();

		Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
	}
}
