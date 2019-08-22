package com.ghebb.themiss.landing;

import java.util.List;

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
import android.widget.Toast;

import com.facebook.Session;
import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.UtilityMethods;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class MailLoginActivity extends Activity implements OnClickListener{

	Button mFacebookLoginButton;
	Button mLoginButton;
	Button mSignupButton;
	Button mForgotPasswordButton;
	Button mBackButton;
	
	EditText mEmailEditText;
	EditText mPasswordEditText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail_login);
		
		TheMissApplication.getInstance().setLanguage();
		
		mFacebookLoginButton = (Button) this.findViewById(R.id.btn_login_facebook);
		mFacebookLoginButton.setOnClickListener(this);

		mLoginButton = (Button) this.findViewById(R.id.btn_login_login);
		mLoginButton.setOnClickListener(this);
		
		mSignupButton = (Button) this.findViewById(R.id.btn_login_signup);
		mSignupButton.setOnClickListener(this);
		
		mForgotPasswordButton = (Button) this.findViewById(R.id.btn_login_forgot_password);
		mForgotPasswordButton.setOnClickListener(this);
		
		mBackButton = (Button) this.findViewById(R.id.btn_maillogin_back);
		mBackButton.setOnClickListener(this);
		
		mEmailEditText = (EditText) this.findViewById(R.id.et_login_email);
		mPasswordEditText = (EditText) this.findViewById(R.id.et_login_password);
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
			case R.id.btn_login_facebook:
				if(Session.getActiveSession() !=null)
					Session.getActiveSession().closeAndClearTokenInformation();
				
				new MainLoginActivity(this, 0);
				MainLoginActivity.mInstance.loginWithFacebook();
				break;
				
			case R.id.btn_login_login:
				loginWithEmail();
				break;
				
			case R.id.btn_login_signup:
				intent = new Intent(this, MailSignupActivity.class);
				startActivity(intent);
				break;
				
			case R.id.btn_login_forgot_password:
				intent = new Intent(this, PasswordResetActivity.class);
				startActivity(intent);

				break;
				
			case R.id.btn_maillogin_back:
//				intent = new Intent(this, LoginActivity.class);
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

	public void loginWithEmail(){
		if(UtilityMethods.checkEditText(MailLoginActivity.this, mEmailEditText, "Email") == false) return;
		if(UtilityMethods.checkEditText(MailLoginActivity.this, mPasswordEditText, "Password") == false) return;
		
		UtilityMethods.hideSoftInput(this, mEmailEditText);
		UtilityMethods.hideSoftInput(this, mPasswordEditText);
		
		TheMissApplication.getInstance().showProgressDialog(this, "Logging In...", null);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
		query.whereEqualTo("email", mEmailEditText.getText().toString());
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> arg0, ParseException arg1) {
				// TODO Auto-generated method stub
				
				if(arg0 == null || arg0.size()==0){
					Toast.makeText(MailLoginActivity.this, "Wrong username or password.", Toast.LENGTH_SHORT).show();
					TheMissApplication.getInstance().hideProgressDialog();
					return;
				}
				
				ParseUser user = (ParseUser) arg0.get(0);
				ParseUser.logInInBackground(user.getUsername(), mPasswordEditText.getText().toString(), new LogInCallback(){

					@Override
					public void done(ParseUser user, ParseException arg1) {
						// TODO Auto-generated method stub
						TheMissApplication.getInstance().hideProgressDialog();
						if(user != null){

							ParseInstallation installation = ParseInstallation.getCurrentInstallation();
							installation.put("user", user);
							installation.saveEventually();
							
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MailLoginActivity.this);
				            Editor edit = pref.edit();
				            if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 0)
				            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 1);
				            if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
				            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 2);
				            
				            edit.putBoolean(Constants.PREF_LOGGEDIN, true);
				            edit.commit();
				            					            
				            Intent intent;
//			            	if(pref.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
//			            		intent = new Intent(MailLoginActivity.this, TutorialAfterLoginActivity.class);
//			            	else
			            	intent = new Intent(MailLoginActivity.this, MainActivity.class);
				            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
				            MailLoginActivity.this.startActivity(intent);
				            
						}else{
							Toast.makeText(MailLoginActivity.this, "Wrong username or password.", Toast.LENGTH_SHORT).show();
						}
					}
					
				});
			}
			
		});
	}
}
