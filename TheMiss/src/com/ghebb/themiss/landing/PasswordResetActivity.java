package com.ghebb.themiss.landing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Session;
import com.gab.themiss.R;
import com.ghebb.themiss.TheMissApplication;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class PasswordResetActivity extends Activity implements OnClickListener{

	Button mFacebookLoginButton;
	Button mSendButton;
	Button mBackButton;
	Button mCancelButton;
	
	EditText mEmailEditText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		TheMissApplication.getInstance().setLanguage();
		
		mFacebookLoginButton = (Button) this.findViewById(R.id.btn_login_facebook);
		mFacebookLoginButton.setOnClickListener(this);

		mSendButton = (Button) this.findViewById(R.id.btn_reset_password_send);
		mSendButton.setOnClickListener(this);
		
		mCancelButton = (Button) this.findViewById(R.id.btn_password_reset_cancel);
		mCancelButton.setOnClickListener(this);
		
		mBackButton = (Button) this.findViewById(R.id.btn_reset_password_back);
		mBackButton.setOnClickListener(this);
		
		mEmailEditText = (EditText) this.findViewById(R.id.et_login_email);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch(arg0.getId())
		{
			case R.id.btn_login_facebook:
				if(Session.getActiveSession() !=null)
					Session.getActiveSession().closeAndClearTokenInformation();
				
				new MainLoginActivity(this, 0);
				MainLoginActivity.mInstance.loginWithFacebook();
				break;

			case R.id.btn_reset_password_send:
				resetPassword();
				break;
				
			case R.id.btn_password_reset_cancel:
			case R.id.btn_reset_password_back:
				super.onBackPressed();
				break;
		}
	}
	
	@Override
	public void  onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		try{
			if(data != null && requestCode == 32665){
//				if (requestCode == 32665 || requestCode == 64206)
					ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
			}
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(this, "Sorry, please try once more.", Toast.LENGTH_LONG).show();
		}		
	}

	public void resetPassword(){
		ParseUser.requestPasswordResetInBackground(mEmailEditText.getText().toString(), 
				new RequestPasswordResetCallback(){

					@Override
					public void done(ParseException e) {
						// TODO Auto-generated method stub
						if(e == null){
							Toast.makeText(PasswordResetActivity.this, "Please check your email.", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PasswordResetActivity.this, "That email was not be registered or unknown error.", Toast.LENGTH_SHORT).show();
						}
					}
			
		});
	}
}
