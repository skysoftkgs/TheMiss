package com.ghebb.themiss;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.gab.themiss.R;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.NotificationService;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.newrelic.agent.android.NewRelic;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends BaseActivity implements OnClickListener{

	final String TAG = "MainActivity";
	
	private Fragment mContent;
	ImageButton mMenuNavButton;
	Button mMenuLoginButton;
	ImageButton mRefreshButton;
	public ImageButton mMenuPlusButton;
	public ImageButton mMenuMessageButton;
	ProgressBar mRefreshingProgressBar;
	TextView mNotificationCountTextView;
	public Object currentFragment;
		
	public static MainActivity mInstance;
	int mBackStackEntryCount;
		
	private final Observer receivedNotification = new Observer()
	{
		public void update(Observable observable, Object data)
		{
			displayNotification();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		NewRelic.withApplicationToken(
				"AA95ce09fb306bcecb892ca6c90487493bba74e978"
				).start(this.getApplication());
		
		//set language
      	TheMissApplication.getInstance().setLanguage();
		setContentView(R.layout.content_frame);

		ParseAnalytics.trackAppOpened(getIntent());
		
		int SDK_INT = android.os.Build.VERSION.SDK_INT;
	    if (SDK_INT > 8) 
	    {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
	                .permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	    }

		mInstance = this;

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		
		if (mContent == null){
			//if from terms and conditions
			Bundle bundle = getIntent().getExtras();
			if(bundle != null && bundle.getBoolean("ShowRules") == true){
				mContent = new RulesFragment();
			}else
				mContent = new HomeFragment();	
		}
		
		getSupportFragmentManager().addOnBackStackChangedListener(getListener());
		
		initMenuBar();
		initGlobalVariables();
		              
		setLoginStatusChanged();
				
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new MenuFragment())
		.commit();
		
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
//		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.shadow);
//		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		NotificationService.getInstance().addObserver(Constants.Notification_NotificationReceived, receivedNotification);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
			      	
		FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
        int backStackEntryCount = manager.getBackStackEntryCount() + 1;
        	for(int i=0;i<backStackEntryCount;i++){
        		Fragment fragment = manager.getFragments().get(i);
	            if(fragment == null || fragment.isAdded()==false){
	            	Intent intent = new Intent(this, MainActivity.class);
	            	startActivity(intent);
	            	return;
	            }
            }
        }
        
		displayNotification();
		
		//go to specific page from notification
		Bundle bundle = getIntent().getExtras();
		if(bundle == null)
			return;
		
		try {
			if(bundle.getString("com.parse.Data") != null){
				JSONObject json = new JSONObject(bundle.getString("com.parse.Data"));
				if(json != null && json.getString("intent").equals("ProfileFragment")){
					String userId = json.getString("fromUser");
					ParseQuery<ParseUser> query = ParseUser.getQuery();
					query.whereEqualTo("objectId", userId);
					query.getFirstInBackground(new GetCallback<ParseUser>(){

						@Override
						public void done(ParseUser user, ParseException arg1) {
							// TODO Auto-generated method stub
							ProfileFragment profileFragment = new ProfileFragment();
							profileFragment.mUser = user;
							addContent(profileFragment, ProfileFragment.TAG);							
						}

					});	
				}else if(json != null && json.getString("intent").equals("CommentFragment")){
					String postId = json.getString("postId");
					ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
					query.whereEqualTo("objectId", postId);
					query.include("user");
					query.getFirstInBackground(new GetCallback<PostModel>(){

						@SuppressLint("SimpleDateFormat")
						@Override
						public void done(PostModel post, ParseException arg1) {
							// TODO Auto-generated method stub
							CommentFragment fragment = new CommentFragment();
							if(fragment != null){
								Bundle bundle = new Bundle();
								Date postDate = post.getCreatedAt();
							    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
								bundle.putString("time", formatter.format(postDate));
								bundle.putString("postImageUrl", post.getPhotoFile().getUrl());
								
								bundle.putBoolean("voteStatus", false);
								bundle.putString("voteCount", "0");
								bundle.putString("shareCount", "0");
								bundle.putSerializable("post", post);
								fragment.setArguments(bundle);
								fragment.mUser = post.getUser();
								addContent(fragment, CommentFragment.TAG);
							}						
						}

					});	
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void toggle() {
		// TODO Auto-generated method stub
		super.toggle();
		UtilityMethods.hideKeyboard(this);
	}
	
	private OnBackStackChangedListener getListener() {
	    OnBackStackChangedListener result = new OnBackStackChangedListener() {
	    	
			@SuppressWarnings("deprecation")
			public void onBackStackChanged() {
	            FragmentManager manager = getSupportFragmentManager();
	            if (manager != null) {
	            	
	            	if(TutorialFragment.mInstance != null) TutorialFragment.mInstance.removeVideo();
	            	setLoginStatusChanged();
	            	
	                int backStackEntryCount = manager.getBackStackEntryCount() + 1;
//	                if(backStackEntryCount == 0)
//	                	finish();
	                
//	                if(backStackEntryCount>mBackStackEntryCount)
//	                {
//	                	mBackStackEntryCount = backStackEntryCount;
//	                	return;
//	                }else{
//	                	mBackStackEntryCount = backStackEntryCount;
//	                }   
	                
	                	                
	                mRefreshingProgressBar.setVisibility(View.INVISIBLE);
	                mRefreshButton.setAlpha(255);
	        		mRefreshButton.setEnabled(true);
	        		mMenuPlusButton.setAlpha(255);
	        		mMenuPlusButton.setEnabled(true);
	        		mMenuMessageButton.setAlpha(255);
	        		mMenuMessageButton.setEnabled(true);
	        		mRefreshButton.setVisibility(View.VISIBLE);
	        		Fragment fragment = manager.getFragments().get(backStackEntryCount);

//	        		FragmentManager fm = getSupportFragmentManager();
//	                fm.beginTransaction()
//	                          .hide(fragment)
//	                          .commit();
//	               
//	                fm.beginTransaction()
//	                          .show(fragment)
//	                          .commit();
	        		
//	        		//if there is uploaded photo, when return home, refresh data.
//	        		if(fragment != null && fragment.getClass() == HomeFragment.class && ImportFragment.mUploaded == true){
//	        			HomeFragment.mRequiredRefresh = true;
//	        			ImportFragment.mUploaded = false;
//	        		}
	        		  
	        	
	        		if(fragment != null){
	        			currentFragment = fragment.getClass();
	        			fragment.onResume();
	        		}	 	 
	            }
	        }
	    };
	    return result;
	}
	
	public void setLoginStatusChanged(){
		
		ParseUser currentUser = ParseUser.getCurrentUser();
				
		if(AppManager.isLoggedIn(this) == true){
			if(currentUser.getBoolean("admin") != true)
				mMenuPlusButton.setVisibility(View.VISIBLE);
			else
				mMenuPlusButton.setVisibility(View.INVISIBLE);
			
			mMenuMessageButton.setVisibility(View.VISIBLE);
			mMenuLoginButton.setVisibility(View.INVISIBLE);
			if(MenuFragment.mInstance != null){
		        TheMissApplication.getInstance().displayUserProfileImage(currentUser, MenuFragment.mInstance.mProfileImageView);
		        if(currentUser != null && currentUser.getUsername() != null)
		        	MenuFragment.mInstance.mUserNameTextView.setText(currentUser.getUsername());
		        
				if(currentUser.getString("gender") != null){
					if(currentUser.getString("gender").equalsIgnoreCase("female") && currentUser.getBoolean("admin") != true){
						MenuFragment.mInstance.mImportLayout.setVisibility(View.VISIBLE);
					}else{
						MenuFragment.mInstance.mImportLayout.setVisibility(View.GONE);
					}
				}
				
				MenuFragment.mInstance.mSettingsLayout.setVisibility(View.VISIBLE);
				MenuFragment.mInstance.mLoginLayout.setVisibility(View.GONE);
				MenuFragment.mInstance.mUserInfoLayout.setVisibility(View.VISIBLE);
				if(currentUser.getBoolean("admin") == true){
					MenuFragment.mInstance.mInviteLayout.setVisibility(View.GONE);
				}else{
					MenuFragment.mInstance.mInviteLayout.setVisibility(View.VISIBLE);
				}
			}
						
		}else{
			mMenuPlusButton.setVisibility(View.INVISIBLE);
			mMenuMessageButton.setVisibility(View.INVISIBLE);
			mMenuLoginButton.setVisibility(View.VISIBLE);
			mNotificationCountTextView.setVisibility(View.INVISIBLE);
			if(MenuFragment.mInstance != null){
				MenuFragment.mInstance.mImportLayout.setVisibility(View.GONE);
				MenuFragment.mInstance.mInviteLayout.setVisibility(View.GONE);
				MenuFragment.mInstance.mSettingsLayout.setVisibility(View.GONE);
				MenuFragment.mInstance.mLoginLayout.setVisibility(View.VISIBLE);
				MenuFragment.mInstance.mUserInfoLayout.setVisibility(View.GONE);
			}
		}
				
	}
	
	public void addContent(Fragment fragment, String tag) {
		mContent = fragment;
		getSupportFragmentManager()
		.beginTransaction()
		.add(R.id.content_frame, fragment)
		.addToBackStack(tag)
		.commit();
		getSlidingMenu().showContent();
	}
	
	public void replaceContent(Fragment fragment) {
		mContent = fragment;
		
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		getSlidingMenu().showContent();
	}
	
	public void initMenuBar(){
		
		mMenuNavButton = (ImageButton) this.findViewById(R.id.ib_menu_nav);
		mMenuNavButton.setOnClickListener(this);
		
		mMenuLoginButton = (Button) this.findViewById(R.id.btn_menu_login);
		mMenuLoginButton.setOnClickListener(this);
		
		mMenuPlusButton = (ImageButton) this.findViewById(R.id.ib_menu_plus);
		mMenuPlusButton.setOnClickListener(this);
		
		mMenuMessageButton = (ImageButton) this.findViewById(R.id.ib_menu_message);
		mMenuMessageButton.setOnClickListener(this);
		
		mRefreshButton = (ImageButton) this.findViewById(R.id.ib_menu_refresh);
		mRefreshingProgressBar = (ProgressBar) this.findViewById(R.id.progressbar_refresh);
		
		mNotificationCountTextView = (TextView) this.findViewById(R.id.tv_menu_notification_count);
	}
	
	public void initGlobalVariables(){
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		AppManager.mScreenWidth = metrics.widthPixels;
		AppManager.mScreenHeight = metrics.heightPixels;
		
	}
	
	public void displayNotification(){
		ParseUser currentUser = ParseUser.getCurrentUser();
		if(currentUser.getBoolean("admin") == true){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("FlagedPicture");
			query.whereEqualTo("new", true);
			query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
			query.countInBackground(new CountCallback(){
	
				@Override
				public void done(int count, ParseException e) {
					// TODO Auto-generated method stub
					if(e == null){
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
						if(count>0 && pref.getBoolean(Constants.PREF_LOGGEDIN, false) == true){
							mNotificationCountTextView.setText(String.valueOf(count));
							mNotificationCountTextView.setVisibility(View.VISIBLE);
						}else{
							mNotificationCountTextView.setVisibility(View.INVISIBLE);
						}
					}
				}
			});
			
		}else{
			ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Notification");
			query1.whereEqualTo("toUser", ParseUser.getCurrentUser());
			query1.whereNotEqualTo("fromUser", ParseUser.getCurrentUser());
			
			ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Notification");
			query2.whereNotEqualTo("toUser", ParseUser.getCurrentUser());
			query2.whereContainsAll("commentUsers", Arrays.asList(ParseUser.getCurrentUser().getObjectId()));
				
			ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Follower");
			innerQuery.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
			innerQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
			ParseQuery<ParseObject> query3 = ParseQuery.getQuery("Notification");
			query3.whereNotEqualTo("toUser", ParseUser.getCurrentUser());
			query3.whereEqualTo("kind", Constants.NOTIFICATION_KIND_NEW_POST);
			query3.whereMatchesKeyInQuery("fromUser", "toUser", innerQuery);
			
			List <ParseQuery<ParseObject>> query0 = new ArrayList<ParseQuery<ParseObject>>();
			query0.add(query1);
			query0.add(query2);
			query0.add(query3);
			
			ParseQuery<ParseObject> query = ParseQuery.or(query0);
			query.whereEqualTo("new", true);
			query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
			query.countInBackground(new CountCallback(){
	
				@Override
				public void done(int count, ParseException e) {
					// TODO Auto-generated method stub
					if(e == null){
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
						if(count>0  && pref.getBoolean(Constants.PREF_LOGGEDIN, false) == true){
							mNotificationCountTextView.setText(String.valueOf(count));
							mNotificationCountTextView.setVisibility(View.VISIBLE);
						}else{
							mNotificationCountTextView.setVisibility(View.INVISIBLE);
						}
					}
				}
				
			});
		}
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent;
	
		switch(arg0.getId())
		{
		case R.id.ib_menu_nav:
			toggle();
			break;
			
		case R.id.btn_menu_login:
			intent = new Intent(this, MainLoginActivity.class);
			startActivity(intent);
			break;
			
		case R.id.ib_menu_plus:
			plusButtonAction();
//			Intent intent1 = new Intent(this, CameraActivity.class);
//			startActivity(intent1);
			break;
			
		case R.id.ib_menu_message:
			Fragment newContent = new NotificationFragment();
			addContent(newContent, NotificationFragment.TAG);
			break;
			
		}
	}
	
	public void plusButtonAction(){

		ParseUser currentUser = ParseUser.getCurrentUser();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		//if not logged in
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==false){
			Intent intent = new Intent(this, MainLoginActivity.class);
			startActivity(intent);
		}else{
		
			if(currentUser.getString("gender") != null && currentUser.getString("gender").equalsIgnoreCase("male")){
				Fragment newContent = new InviteFragment();
				addContent(newContent, InviteFragment.TAG);
			}else{
				Fragment newContent = new ImportFragment();
				addContent(newContent, ImportFragment.TAG);
			}
		}
	}
		

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
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

	public void goBack(){
		if(getSupportFragmentManager().getFragments().size()>2)
			getSupportFragmentManager().popBackStack();
		else
			super.onBackPressed();
	}

	public void sendNotification(final PostModel post, final String kind){
    	//for notification
				
		ParseObject notification = new ParseObject("Notification");
		notification.put("fromUser", ParseUser.getCurrentUser());
		notification.put("toUser", post.getUser());
		notification.put("new", true);
		notification.put("post", post);
		notification.put("kind", kind);
		if(post.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) &&
				kind.equals(Constants.NOTIFICATION_KIND_COMMENT)){
			notification.put("commentUsers", post.getList("commentUsers"));
		}
		notification.saveEventually(new SaveCallback(){

			@Override
			public void done(ParseException arg0) {
				// TODO Auto-generated method stub
				// Create our Installation query
				ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
				if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_COMMENT) && 
						post.getUser().getObjectId().equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())){
					List<String> commentUsers = UtilityMethods.removeUser(post.getCommentUserList(), ParseUser.getCurrentUser());
					
					ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
					innerQuery.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
					innerQuery.whereContainedIn("objectId", commentUsers);
					pushQuery.whereMatchesQuery("user", innerQuery);
					
				}else if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_NEW_POST)){
					ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
					query.whereEqualTo("toUser", ParseUser.getCurrentUser());
					query.include("fromUser");
					pushQuery.whereMatchesKeyInQuery("user", "fromUser", query);
					
				}else{
					if(!ParseUser.getCurrentUser().getObjectId().equals(post.getUser().getObjectId()))
						pushQuery.whereEqualTo("user", post.getUser());
					else
						return;
				}
				
				// Send push notification to query
				JSONObject data = null;
				try {
					if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_NEW_POST)){
						data = new JSONObject("{\"action\": \"com.ghebb.themiss.VOTE_ACTION\"," + 
								"\"postId\" :\""  + post.getObjectId() + "\"," +
								"\"alert\" :\""  + ParseUser.getCurrentUser().getUsername() + " posted new photo." + "\"," +
								"\"intent\" : \"CommentFragment\"}");
					}else{
						data = new JSONObject("{\"action\": \"com.ghebb.themiss.VOTE_ACTION\"}");
					}
					final ParsePush push = new ParsePush();
					push.setQuery(pushQuery); // Set our Installation query		
					push.setData(data);
					push.sendInBackground();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
    }
	
	public void sendFollowingNotification(ParseUser fromUser, ParseUser toUser){
    	//for notification
		JSONObject data = null;
		String message = fromUser.getUsername() + " " + getResources().getString(R.string.is_following_you);
		try {
			data = new JSONObject("{\"action\": \"com.ghebb.themiss.VOTE_ACTION\"," + 
					"\"alert\" :\""  + message + "\"," +
					"\"fromUser\" :\""  + fromUser.getObjectId() + "\"," +
					"\"intent\" : \"ProfileFragment\"}");
			ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
			pushQuery.whereEqualTo("user", toUser);
			final ParsePush push = new ParsePush();
			push.setQuery(pushQuery);
			push.setData(data);
			push.sendInBackground();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
	
	public void sendFlagNotification(){
		//send notification to admin user
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		 
		// Send push notification to query
		JSONObject data = null;
		try {
			data = new JSONObject("{\"action\": \"com.ghebb.themiss.VOTE_ACTION\"}");
			ParsePush push = new ParsePush();
			push.setQuery(pushQuery); // Set our Installation query
			push.setData(data);
			push.sendInBackground();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendNewUserSignupNotification(){
		//send notification to admin user
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		 
		// Send push notification to query
		JSONObject data = null;
		try {
			data = new JSONObject("{\"action\": \"com.ghebb.themiss.VOTE_ACTION\"," + 
					"\"intent\" : \"NewUserSignup\"}");
			ParsePush push = new ParsePush();
			push.setQuery(pushQuery); // Set our Installation query
			push.setData(data);
			push.sendInBackground();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveShareCount(final ParseUser fromUser, final ParseUser toUser){
		//for profile following ranking.
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Share");
		query.whereEqualTo("fromUser", fromUser);
		query.whereEqualTo("toUser", toUser);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject obj, ParseException arg1) {
				// TODO Auto-generated method stub
				if(obj != null){
					int shareCount = obj.getInt("shareCount");
					obj.put("shareCount", ++shareCount);
					obj.saveEventually();
					
				}else{
					ParseObject newObj = new ParseObject("Share");
					newObj.put("fromUser", fromUser);
					newObj.put("toUser", toUser);
					newObj.put("shareCount", 1);
					newObj.saveEventually();
				}
			}
		});
	}
	
	public void votePhoto(UserInfoModel userInfo, TextView totalTextView, PostModel post, TextView voteTextView, ImageButton voteImageButton ){
		if(AppManager.isLoggedIn(this) == false){
			Intent intent = new Intent(MainActivity.this, MainLoginActivity.class);
			startActivity(intent);
			return;
		}
				
		if(post == null) return;
		
		//if current user is super user
		if(AppManager.isSuperUser(ParseUser.getCurrentUser()) == true){
			voteTextView.setText(String.valueOf(post.getVoteCount() + 1));
			post.setVoteCountOfSuperUser(post.getVoteCountOfSuperUser() + 1);
			post.setTotalActionCount(post.getTotalActionCount() + 1);
			post.saveInBackground();
			
			if(userInfo != null){	//on the miss of month page
				totalTextView.setText(String.valueOf(userInfo.getVoteCount() + 1));
				userInfo.setVoteCount(userInfo.getVoteCount() + 1);
				userInfo.saveInBackground();
			
			}else{
				setVoteCount(post, 1);
			}
			return;
		}
		
		if(voteImageButton.isSelected()==false){
			voteImageButton.setSelected(true);
			voteTextView.setText(String.valueOf(post.getVoteCount() + 1));
			
			post.addVoteUser(ParseUser.getCurrentUser().getObjectId());
			post.setTotalActionCount(post.getTotalActionCount() + 1);
			post.saveInBackground();
			
			if(userInfo != null){	//on the miss of month page
				totalTextView.setText(String.valueOf(userInfo.getVoteCount() + 1));
				userInfo.setVoteCount(userInfo.getVoteCount() + 1);
				userInfo.saveInBackground();
			
			}else{
				setVoteCount(post, 1);
			}
			sendNotification(post, Constants.NOTIFICATION_KIND_VOTE);
			
		}else{
			if(post.getVoteUserList().size() <= 0) return;
			
			voteImageButton.setSelected(false);
			voteTextView.setText(String.valueOf(post.getVoteCount() - 1));
			
			post.removeVoteUser(ParseUser.getCurrentUser());
			post.setTotalActionCount(post.getTotalActionCount() - 1);
			post.saveInBackground();
					
			if(userInfo != null){	//on the miss of month page
				totalTextView.setText(String.valueOf(userInfo.getVoteCount() - 1));
				userInfo.setVoteCount(userInfo.getVoteCount() - 1);
				userInfo.saveEventually();
			
			}else{
				setVoteCount(post, -1);
			}
		}
	}
	
	@SuppressLint("DefaultLocale")
	public void setVoteCount(final PostModel post, final int amount){
		if(post == null) return;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(post.getCreatedAt());
		final String month = String.format("%04d_%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1);
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", post.getUser());
		query.whereEqualTo("postMonth", month);
		query.orderByDescending("voteCount");
		query.getFirstInBackground(new GetCallback<UserInfoModel>() {

			@Override
			public void done(UserInfoModel userInfo, ParseException err) {
				// TODO Auto-generated method stub
				if(userInfo != null){
					userInfo.setVoteCount(userInfo.getVoteCount() + amount);
					userInfo.setLastPost(post);
					userInfo.saveInBackground();
					
				}else{
					UserInfoModel newUserInfo = new UserInfoModel();
					newUserInfo.setLastPost(post);
					newUserInfo.setPostMonth(month);
					newUserInfo.setVoteCount(1);
					newUserInfo.setUser(post.getUser());
					newUserInfo.setUserName(post.getUser().getUsername());
					newUserInfo.saveInBackground();
				}
			}		
		});
	}
	
	@SuppressLint("DefaultLocale")
	public void setShareCount(final PostModel post){
		if(post == null) return;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(post.getCreatedAt());
		final String month = String.format("%04d_%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1);
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", post.getUser());
		query.whereEqualTo("postMonth", month);
		query.getFirstInBackground(new GetCallback<UserInfoModel>() {

			@Override
			public void done(UserInfoModel userInfo, ParseException err) {
				// TODO Auto-generated method stub
				if(userInfo != null){
					userInfo.setShareCount(userInfo.getShareCount() + 1);
					userInfo.saveEventually();
					
				}else{
					UserInfoModel newUserInfo = new UserInfoModel();
					newUserInfo.setLastPost(post);
					newUserInfo.setPostMonth(month);
					newUserInfo.setShareCount(1);
					newUserInfo.setUser(post.getUser());
					newUserInfo.setUserName(post.getUser().getUsername());
					newUserInfo.saveEventually();
				}
			}		
		});
	}
	public void setLastPost(final ParseUser user){
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", user);
		query.orderByDescending("voteCount");
		query.getFirstInBackground(new GetCallback<UserInfoModel>() {

			@Override
			public void done(final UserInfoModel userInfo, ParseException err) {
				
				ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
				query.whereEqualTo("user", user);
				query.orderByDescending("createdAt");
				query.getFirstInBackground(new GetCallback<PostModel>(){

					@Override
					public void done(PostModel post, ParseException arg1) {
						// TODO Auto-generated method stub
						
						if(post == null){
							userInfo.deleteInBackground();
							return;
						}
						
						//update with last post
						userInfo.setLastPost(post);
						userInfo.saveInBackground();
					}
				});
			}
		});		
	}
	
    public void shareImage(final PostModel post, final String imageUrl, final ParseUser user, final Fragment fragment){
    	AppManager.mIsAlreadyShared = false;
    	
    	AlertDialog.Builder buildSingle = new AlertDialog.Builder(fragment.getActivity());
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(fragment.getActivity(),
				R.layout.item_dialog_row);
		adapter.add(getResources().getString(R.string.share_facebook));
		adapter.add(getResources().getString(R.string.share_whatsapp));
		adapter.add(getResources().getString(R.string.share_to));
	
		buildSingle.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		buildSingle.setAdapter(adapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String chosen = adapter.getItem(which)
								.toString();
						String message;
						if(user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
							message = getResources().getString(R.string.share_message_end);
						}else{
							message = getResources().getString(R.string.share_message_first) + " " +
									user.getUsername() + " " + getResources().getString(R.string.share_message_middle) +
									getResources().getString(R.string.share_message_end);
						}
						
						if (chosen.equals(getResources().getString(R.string.share_facebook))) {
							shareViaFacebook(post, Session.getActiveSession(), message, imageUrl, user, fragment);			            
						} else if (chosen.equals(getResources().getString(R.string.share_whatsapp))) {
							share("whatsapp", post, imageUrl, message, fragment);
						} else if (chosen.equals(getResources().getString(R.string.share_to))) {
							shareToAll(post, imageUrl, message, fragment);
						} 
					}
				});
		AlertDialog a = buildSingle.create();
		a.show();
		Button bq = a.getButton(DialogInterface.BUTTON_NEGATIVE);
		bq.setTextColor(Color.GRAY);
    }
    
    @SuppressLint("SdCardPath")
	public void shareToAll(PostModel post, String imageUrl,String message, Fragment fragment) {
    	Bitmap icon = ImageLoader.getInstance().loadImageSync(imageUrl);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
		try {
		    f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		    fo.close();
		} catch (IOException e) {                       
		        e.printStackTrace();
		}
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
		share.putExtra(Intent.EXTRA_SUBJECT, "TheMiss subject");
		share.putExtra(Intent.EXTRA_TEXT, "TheMiss text");
		fragment.startActivityForResult(Intent.createChooser(share, "Share Image"), Constants.REQUEST_SHARE_ACTION);
		
		setShareCount(post);
    }
    
    @SuppressLint({ "DefaultLocale", "SdCardPath" })
	public void share(String nameApp, PostModel post, String imageUrl,String message, Fragment fragment) {
    	
//		Bitmap icon = ((BitmapDrawable)holder.postImageView.getDrawable()).getBitmap();
		Bitmap icon = ImageLoader.getInstance().loadImageSync(imageUrl);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
		try {
		    f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		    fo.close();
		} catch (IOException e) {                       
		        e.printStackTrace();
		}
		
    	try
    	{
    		List<Intent> targetedShareIntents = new ArrayList<Intent>();
    		Intent share = new Intent(android.content.Intent.ACTION_SEND);
    	    share.setType("*/*");
    	    List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
    	    if (!resInfo.isEmpty()){
	    	    for (ResolveInfo info : resInfo) {
	    	        Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
	    	        targetedShare.setType("image/*"); // put here your mime type
	    	        if (info.activityInfo.packageName.toLowerCase().contains(nameApp) || info.activityInfo.name.toLowerCase().contains(nameApp)) {
	    	            targetedShare.putExtra(Intent.EXTRA_SUBJECT, "Sample Photo");
	    	            targetedShare.putExtra(Intent.EXTRA_TEXT, message);
	    	            targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
	    	            targetedShare.setPackage(info.activityInfo.packageName);
	    	            targetedShareIntents.add(targetedShare);
	    	        }
	    	    }
	    	    Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
	    	    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
	    	    fragment.startActivityForResult(chooserIntent, Constants.REQUEST_SHARE_ACTION);
	    	    
	    	    setShareCount(post);
    	    }
    	}catch(Exception e){
    	      Log.v("VM","Exception while sending image on" + nameApp + " "+  e.getMessage());
    	      TheMissApplication.getInstance().showErrorDialog(fragment.getActivity(), "Not installed "+nameApp);
    	}
		
		
    }
    
    public void shareViaFacebook(final PostModel post, Session session, String paramMessage, String imageUrl, ParseUser user, final Fragment fragment)
	{
    	if(session == null || session.isOpened() == false)
    	{
//    		TheMissApplication.getInstance().showErrorDialog(fragment.getActivity(), "Please connect to Facebook in settings.");
    		Intent intent = new Intent(fragment.getActivity(), SettingsActivity.class);
			intent.putExtra("ScrollToEnd", true);
			fragment.startActivity(intent);
    		return;
    	}
    	
    	final Bundle postParams = new Bundle();
    	String userName = user.getUsername();
    	String caption;
    	if(AppManager.isItalian(ParseUser.getCurrentUser())){
    		caption = ParseUser.getCurrentUser().getUsername() + " ha condiviso una foto di " + userName;
    	}else{
    		caption = ParseUser.getCurrentUser().getUsername() + " shared " + userName + "'s photo.";
    	}
 	    postParams.putString("name", "TheMiss");
// 	    postParams.putString("message", paramMessage);
 	    postParams.putString("caption", caption);
 	    postParams.putString("description", paramMessage);
 	    postParams.putString("link", "http://www.themiss.tv");
// 	    postParams.putString("redirect_uri", "http://thismiss.com");
// 	    postParams.putString("display", "popup");
 	    postParams.putString("picture", imageUrl);

 	  
	    WebDialog feedDialog = (
		        new WebDialog.FeedDialogBuilder(fragment.getActivity(),
		            session,
		            postParams))
		        .setOnCompleteListener(new OnCompleteListener() {

		            @Override
		            public void onComplete(Bundle values,
		                FacebookException error) {
		                if (error == null) {
		                    // When the story is posted, echo the success
		                    // and the post Id.
		                    final String postId = values.getString("post_id");
		                    if (postId != null) {
		                        Toast.makeText(fragment.getActivity(),
		                            getResources().getString(R.string.shared_successfully), Toast.LENGTH_SHORT).show();
//		                        
		                        setShareCount(post);
		                        
		                        if(fragment.getClass().equals(HomeFragment.class))
		                        	NotificationService.getInstance().postNotification(Constants.NOTIFICATION_SHARE_HOME_SUCCESS, null);
		                        else if(fragment.getClass().equals(ProfileFragment.class))
		                        	NotificationService.getInstance().postNotification(Constants.NOTIFICATION_SHARE_PROFILE_SUCCESS, null);
		                        else if(fragment.getClass().equals(CommentFragment.class))
		                        	NotificationService.getInstance().postNotification(Constants.NOTIFICATION_SHARE_COMMENT_SUCCESS, null);
		                        
		                    } else {
		                        // User clicked the Cancel button
		                        Toast.makeText(fragment.getActivity(), 
		                            getResources().getString(R.string.sharing_cancelled), 
		                            Toast.LENGTH_SHORT).show();
		                    }
		                } else if (error instanceof FacebookOperationCanceledException) {
		                    // User clicked the "x" button
		                    Toast.makeText(fragment.getActivity(), 
		                    		getResources().getString(R.string.sharing_cancelled), 
		                        Toast.LENGTH_SHORT).show();
		                } else {
		                    // Generic, ex: network error
		                    Toast.makeText(fragment.getActivity(), 
		                        "Error posting story", 
		                        Toast.LENGTH_SHORT).show();
		                }
		            }
		        })
		    .build();
		    feedDialog.show();
	}
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		NotificationService.getInstance().removeObserver(Constants.Notification_NotificationReceived, receivedNotification);
	}
	
	
}
