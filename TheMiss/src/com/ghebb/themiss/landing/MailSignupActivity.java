package com.ghebb.themiss.landing;

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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.UtilityMethods;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MailSignupActivity extends Activity implements OnClickListener{

	Button mFacebookLoginButton;
	Button mLoginButton;
	Button mSignupButton;
	Button mForgotPasswordButton;
	Button mBackButton;
	
	EditText mUserNameEditText;
	EditText mEmailEditText;
	EditText mPasswordEditText;
	EditText mConfirmPasswordEditText;
	
	RadioButton mFemaleRadioButton;
	TextView mTermsAndConditionTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail_signup);
		
		TheMissApplication.getInstance().setLanguage();
		
		mFacebookLoginButton = (Button) this.findViewById(R.id.btn_signup_facebook);
		mFacebookLoginButton.setOnClickListener(this);

		mLoginButton = (Button) this.findViewById(R.id.btn_signup_login);
		mLoginButton.setOnClickListener(this);
		
		mSignupButton = (Button) this.findViewById(R.id.btn_signup_signup);
		mSignupButton.setOnClickListener(this);
		
		mForgotPasswordButton = (Button) this.findViewById(R.id.btn_signup_forgot_password);
		mForgotPasswordButton.setOnClickListener(this);
		
		mBackButton = (Button) this.findViewById(R.id.btn_signup_back);
		mBackButton.setOnClickListener(this);
		
		mUserNameEditText = (EditText) this.findViewById(R.id.et_signup_username);
		mEmailEditText = (EditText) this.findViewById(R.id.et_signup_email);
		mPasswordEditText = (EditText) this.findViewById(R.id.et_signup_password);
		mConfirmPasswordEditText = (EditText) this.findViewById(R.id.et_signup_confirm_password);
		
		mFemaleRadioButton = (RadioButton) this.findViewById(R.id.radio_female);
		mTermsAndConditionTextView = (TextView) this.findViewById(R.id.tv_signup_terms_conditions);
		mTermsAndConditionTextView.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		TheMissApplication.getInstance().hideProgressDialog();
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch(arg0.getId())
		{
			case R.id.btn_signup_facebook:
				if(Session.getActiveSession() !=null)
					Session.getActiveSession().closeAndClearTokenInformation();
				
				new MainLoginActivity(this, 0);
				MainLoginActivity.mInstance.loginWithFacebook();
				break;
							
			case R.id.btn_signup_login:
				intent = new Intent(this, MailLoginActivity.class);
				startActivity(intent);
				break;
				
			case R.id.btn_signup_signup:
				signupWithEmail();
				break;
				
			case R.id.btn_signup_forgot_password:
				intent = new Intent(this, PasswordResetActivity.class);
				startActivity(intent);
				break;
				
			case R.id.tv_signup_terms_conditions:
				intent = new Intent(this, MainActivity.class);
				intent.putExtra("ShowRules", true);
				startActivity(intent);
				break;
				
			case R.id.btn_signup_back:
//				intent = new Intent(this, MailLoginActivity.class);
//				startActivity(intent);
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

	public void signupWithEmail(){
		if(UtilityMethods.checkEditText(MailSignupActivity.this, mUserNameEditText, "Username") == false) return;
		if(UtilityMethods.checkEditText(MailSignupActivity.this, mEmailEditText, "Email") == false) return;
		if(UtilityMethods.checkEditText(MailSignupActivity.this, mPasswordEditText, "Password") == false) return;
		if(UtilityMethods.checkEditText(MailSignupActivity.this, mConfirmPasswordEditText, "Confirm Password") == false) return;
		if(mPasswordEditText.getText().toString().equalsIgnoreCase(mConfirmPasswordEditText.getText().toString()) == false){
			Toast.makeText(this, "Password doesn't match.", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!UtilityMethods.isValidEmail(mEmailEditText.getText().toString())){
			Toast.makeText(this, "Please correct email.", Toast.LENGTH_LONG).show();
			return;
		}
		
		final ParseUser parseUser = new ParseUser();
		parseUser.setUsername(mUserNameEditText.getText().toString());
		parseUser.setPassword(mPasswordEditText.getText().toString());
		parseUser.setEmail(mEmailEditText.getText().toString());
		parseUser.put("loggedInWay", "mail");
		parseUser.put("deactive", false);
		
		if(mFemaleRadioButton.isChecked())
			parseUser.put("gender", "female");
		else
			parseUser.put("gender", "male");
		
		UtilityMethods.hideSoftInput(this, mUserNameEditText);
		UtilityMethods.hideSoftInput(this, mEmailEditText);
		UtilityMethods.hideSoftInput(this, mPasswordEditText);
		TheMissApplication.getInstance().showProgressDialog(this, "Signing Up...", null);
		parseUser.signUpInBackground(new SignUpCallback(){

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				TheMissApplication.getInstance().hideProgressDialog();
				
				if(e==null){
					ParseInstallation installation = ParseInstallation.getCurrentInstallation();
					installation.put("user", parseUser);
					installation.saveEventually();
					
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MailSignupActivity.this);
	                Editor edit = pref.edit();
	                if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 0)
		            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 1);
		            if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
		            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 2);
	                edit.putBoolean(Constants.PREF_LOGGEDIN, true);
	            	edit.commit();
	            	
					Toast.makeText(MailSignupActivity.this, "Signup completed successfully", Toast.LENGTH_SHORT).show();
					
					Intent intent;
//	            	if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
//	            		intent = new Intent(MailSignupActivity.this, TutorialAfterLoginActivity.class);
//	            	else
	            	intent = new Intent(MailSignupActivity.this, MainActivity.class);
			        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
			        MailSignupActivity.this.startActivity(intent);
			        
			        MainActivity.mInstance.sendNewUserSignupNotification();
					
				}else{
					Toast.makeText(MailSignupActivity.this, "Error. Username or Email may be already exists.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
