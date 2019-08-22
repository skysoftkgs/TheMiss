package com.ghebb.themiss.landing;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import br.com.dina.oauth.instagram.InstagramApp;
import br.com.dina.oauth.instagram.InstagramApp.OAuthAuthenticationListener;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.SettingsActivity;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.Constants;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;


public class MainLoginActivity extends Activity implements OnClickListener{

	public static final String TAG = "LoginActivity";
	
	Button mFacebookLoginButton;
	Button mMailButton;
	Button mBackButton;

	boolean bAllowPermission = false;
	boolean bCallMain = false;
	
	public static MainLoginActivity mInstance;
	public static InstagramApp mApp;
	static Context mContext;
	
	ProgressDialog mProgressDialog;
	
	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

		@Override
		public void onSuccess() {
			
			//if it is connecting to instagram from settings page.
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
			if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==true){
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.put("instagramID", mApp.getId());
				currentUser.saveInBackground(new SaveCallback(){

					@Override
					public void done(ParseException arg0) {
						// TODO Auto-generated method stub
						if(arg0 == null && SettingsActivity.mInstance != null){
				        	SettingsActivity.mInstance.setInstagramButtonStatus();
				        }
					}
					
				});
			}
			
			else{
				mProgressDialog = ProgressDialog.show(mContext, "", "Logging In...", true);
				
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereEqualTo("instagramID", mApp.getId());
				query.whereEqualTo("loggedInWay", "instagram");
				query.getFirstInBackground(new GetCallback<ParseUser>(){
	
					@Override
					public void done(ParseUser object, ParseException e) {
						// TODO Auto-generated method stub
						
					    if (e == null && object != null) {
		
//				    		if(object.getString("loggedInWay")!=null && object.getString("loggedInWay").equalsIgnoreCase("instagram")){
				    			ParseUser.logInInBackground(mApp.getUserName(), "", new LogInCallback(){

									@Override
									public void done(ParseUser arg0,
											ParseException arg1) {
										// TODO Auto-generated method stub
										
										if(arg1 == null){
											instagramLoginSuccess(arg0);
										}else {
											mProgressDialog.dismiss();
							    	    	Toast.makeText(mContext, "Logging in Error.", Toast.LENGTH_SHORT).show();
							    	    	return;
							    	    }
									}
				    				
				    			});
				    			
				    			return;
//				    		}				    	
					    	
					    }
					    
				    	// signup user
			    		final ParseUser user = new ParseUser();
				    	user.setUsername(mApp.getUserName());
				    	user.setPassword("");
				    	user.put("instagramID", mApp.getId());
				    	user.put("loggedInWay", "instagram");
				    	user.put("deactive", false);
				    	user.signUpInBackground(new SignUpCallback() {
				    		public void done(ParseException e) {
				    			mProgressDialog.dismiss();
				    			
					    	    if (e == null) {
					    	    	instagramLoginSuccess(user);
					    	    } else {
					    	    	Toast.makeText(mContext, "Signup Error.", Toast.LENGTH_SHORT).show();
					    	    	return;
					    	    }
				    		}
				    	});							 
					}
				});		
			}
		}

		@Override
		public void onFail(String error) {
			Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
		}
	};
	
	public void instagramLoginSuccess(ParseUser parseUser){
		
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put("user", parseUser);
		installation.saveEventually();
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor edit = pref.edit();
        edit.putBoolean(Constants.PREF_LOGGEDIN, true);
    	edit.commit();
    	   	
    	mProgressDialog.dismiss();
    	Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);	
	}
	
	public void initInstagram(){
		mApp = new InstagramApp(mContext, Constants.INSTAGRAM_CLIENT_ID,
				Constants.INSTAGRAM_CLIENT_SECRET, Constants.INSTAGRAM_CALLBACK_URL);
		mApp.setListener(listener);
	}

	public MainLoginActivity(){
		
	}
	
	//for connecting instagram in settings page.
	public MainLoginActivity(Context context, int loginWay){
		mInstance = this;
		mContext = context;
		if(loginWay == 1)		//if instagram
			initInstagram();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		TheMissApplication.getInstance().setLanguage();
		
		mFacebookLoginButton = (Button) this.findViewById(R.id.btn_login_facebook);
		mFacebookLoginButton.setOnClickListener(this);
		
		mMailButton = (Button) this.findViewById(R.id.btn_mail);
		mMailButton.setOnClickListener(this);
		
		Button termsButton = (Button) findViewById(R.id.btn_terms_conditions);
		termsButton.setOnClickListener(this);
		
		mBackButton = (Button) this.findViewById(R.id.btn_login_back);
		mBackButton.setOnClickListener(this);
		
		mContext = this;
		initInstagram();
		
		mInstance = this;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent;
		switch(arg0.getId())
		{
		case R.id.btn_login_facebook:
			if(Session.getActiveSession() !=null)
				Session.getActiveSession().closeAndClearTokenInformation();
						
			loginWithFacebook();
			
//			try {
//	        PackageInfo info = getPackageManager().getPackageInfo(
//	                "com.gab.themiss", 
//	                PackageManager.GET_SIGNATURES);
//	        for (Signature signature : info.signatures) {
//	            MessageDigest md = MessageDigest.getInstance("SHA");
//	            md.update(signature.toByteArray());
//	            String strHashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
//	            Log.e("Testing:", "Hi key ::  "+ strHashKey);
//	            
//	            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//	    		builder1.setIcon(android.R.drawable.ic_dialog_info);
//	    		builder1.setTitle("HashKey");
//	            builder1.setMessage("App Hashkey is " + strHashKey);
//	            builder1.setCancelable(true);
//	            builder1.setPositiveButton("OK",
//	                    new DialogInterface.OnClickListener() {
//	                public void onClick(DialogInterface dialog, int id) {
//	                    
//	                }
//	            });
//	            
//	            AlertDialog alert11 = builder1.create();
//	            alert11.show();
//	            }
//	    } catch (NameNotFoundException e) {
//
//	    } catch (NoSuchAlgorithmException e) {
//
//	    }
			break;
			
		case R.id.btn_mail:
			intent = new Intent(this, MailLoginActivity.class);
			startActivity(intent);
			break;
			
		case R.id.btn_terms_conditions:
			intent = new Intent(this, MainActivity.class);
			intent.putExtra("ShowRules", true);
			startActivity(intent);
			break;
			
		case R.id.btn_login_back:
			super.onBackPressed();
			break;
		}
	}
	
	@Override
	public void  onActivityResult(int requestCode, int resultCode, Intent data){
		
		super.onActivityResult(requestCode, resultCode, data);
				
//		if(Session.getActiveSession() != null)
//	    	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		
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

	public void loginWithFacebook(){
		mProgressDialog = ProgressDialog.show(mContext, "", "Logging in...", true);
		bAllowPermission = false;
	
		List<String> permissions = Arrays.asList("email", "user_photos");
		ParseFacebookUtils.logIn(permissions, (Activity)mContext, new LogInCallback(){

			@Override
			public void done(ParseUser user, ParseException err) {
				// TODO Auto-generated method stub
				mProgressDialog.dismiss();
				
				if(user == null){
					Log.d(MainLoginActivity.TAG, "Uh oh. The user cancelled the Facebook login.");
					
				}else{
	        		saveUserData();
				}
			}		
		});
	}
	 
	public static void loginWithInstagram(Context context){
		
//		if (!mApp.hasAccessToken()) {
			mApp.authorize(context);
//		} 
	}
	
	public void saveUserData(){
		final Session session = ParseFacebookUtils.getSession();
		if(session == null || session.isOpened() == false) return;
		
		mProgressDialog = ProgressDialog.show(mContext, "", "Saving user information...", true);
		Request request = Request.newMeRequest(session, 
				new Request.GraphUserCallback() {
					
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
						
						GraphObject ob = response.getGraphObject();
						JSONObject jobj = ob.getInnerJSONObject();
						
		                JSONObject locationJObj = null;
						try {
							locationJObj = (JSONObject) jobj.get("location");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						JSONObject coverJObj = null;
						try {
							coverJObj = (JSONObject) jobj.get("cover");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
		                final String facebookId = user.getId();
		                final String name = user.getName();
		                String gender = user.asMap().get("gender").toString();
		                String email = null;
		                if(user.asMap()!= null && user.asMap().get("email")!=null) 
		                	email = user.asMap().get("email").toString();
		                String birthday = user.getBirthday();
		                String first_name = user.getFirstName();
		                String last_name = user.getLastName();
		                String location = null;
						try {
							if(locationJObj != null)
								location = locationJObj.getString("name");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						 String cover = null;
						try {
							if(coverJObj != null)
								cover = coverJObj.getString("source");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
		                String relationship=null;
		                try{
		                	relationship = jobj.get("relationship_status").toString();
		                }catch(Exception e)
		                {
		                	e.printStackTrace();
		                }
		                
		                final ParseUser parseUser = ParseUser.getCurrentUser();
		                
		                if(facebookId!=null){
		                	parseUser.put("facebookID", facebookId);
		                	parseUser.put("profileUrl", "https://graph.facebook.com/" + facebookId + "/picture?type=large");
		                }
		                
		                if(gender!=null) parseUser.put("gender", gender);
		                if(email!=null) parseUser.put("email", email);
		                if(birthday!=null) parseUser.put("birthday", birthday);
		                if(relationship!=null) parseUser.put("relationship", relationship);
		                if(location!=null) parseUser.put("location", location);
		                if(first_name!=null) parseUser.put("firstName", first_name);
		                if(last_name!=null) parseUser.put("lastName", last_name);
		                if(location !=null)	parseUser.put("city", location);
		                if(cover != null) parseUser.put("coverUrl", cover);
		                parseUser.put("loggedInWay", "facebook");
		                parseUser.put("deactive", false);
		                
	    	            parseUser.setUsername(name);
	    	            parseUser.setPassword("");
		                           
						parseUser.saveInBackground(new SaveCallback(){
								
							@Override
							public void done(ParseException arg0) {
								// TODO Auto-generated method stub
								if(arg0==null){
//									if (!ParseFacebookUtils.isLinked(parseUser)) {
//										ParseFacebookUtils.link(parseUser, (Activity) mContext, new SaveCallback(){
//											
//											@Override
//											public void done(ParseException arg0) {
//												// TODO Auto-generated method stub
//												if(ParseFacebookUtils.isLinked(parseUser)){
//													Log.d("Wooho", "user logged in with Facebook");
////													ParseFacebookUtils.saveLatestSessionData(parseUser);
//												}
//											}
//											
//										});
//									}
										
									ParseInstallation installation = ParseInstallation.getCurrentInstallation();
									installation.put("user", parseUser);
									installation.saveInBackground(new SaveCallback(){
	
										@Override
										public void done(
												ParseException arg0) {
											// TODO Auto-generated method stub
											Log.d("------kgs-------", "success");
										}
									});
								}
									
							}
			                	
						});
		                
		                SharedPreferences pref1 = PreferenceManager.getDefaultSharedPreferences(mContext);
		                Editor edit = pref1.edit();
		                edit.putBoolean(Constants.PREF_LOGGEDIN, true);
		                if(pref1.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 0)
			            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 1);
			            if(pref1.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
			            	edit.putInt(Constants.PREF_LOGGEDIN_TIMES, 2);
		            	edit.commit();

		            	mProgressDialog.dismiss();
		                finish();
		                
		            	Intent intent;
//		            	if(pref1.getInt(Constants.PREF_LOGGEDIN_TIMES, 0) == 1)
//		            		intent = new Intent(mContext, TutorialAfterLoginActivity.class);
//		            	else
		            	intent = new Intent(mContext, MainActivity.class);
		                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
		                mContext.startActivity(intent);
		      		}
				});
		
		Bundle parameters = new Bundle();
		parameters.putString("fields", "name,email, location,gender,birthday,relationship_status,first_name,last_name,cover,picture");
		request.setParameters(parameters);
		request.executeAsync();
	}
	
}
