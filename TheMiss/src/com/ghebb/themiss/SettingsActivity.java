package com.ghebb.themiss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.gab.themiss.R;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.imageutil.ImageLoader;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class SettingsActivity extends Activity implements OnClickListener{
	
	ImageView coverImageView;
	ImageView profileImageView;
	
	EditText mUserNameEditText;
	EditText mFirstNameEditText;
	EditText mLastNameEditText;
	EditText mCityEditText;
	EditText mMobileNumberEditText;
	EditText mDescriptionEditText;
	EditText mEmailEditText;
	EditText mPasswordEditText;
	EditText mConfirmPasswordEditText;
	ImageView mFacebookLinkImageView;
	ImageView mInstagramLinkImageView;
	RelativeLayout mFacebookLayout;
	RelativeLayout mInstagramLayout;
	Spinner mSpinnerGender;
	Spinner mSpinnerLanguage;
	Button mBackButton;
	Button mSaveButton;
	Button mLogoutButton;
	Button mDeactivateButton;
	ScrollView mScrollView;
	
	ImageButton mRefreshButton;
	ProgressBar mRefreshingProgressBar;
	
	public static final String TAG = "SettingsFragment";
	private final int PICK_FROM_CAMERA = 1001;
	private final int PICK_FROM_FILE = 1002;
	private final int REQUEST_DEACTIVATE = 1003;
		
	private String[] gender;
	private String[] language = {"English", "Italian"};
	
	private String photoOptionChosen;
	private int selectedChangeBtn;
	private final int CHANGE_COVER_BUTTON = 0;
	private final int CHANGE_PROFILE_BUTTON = 1;
	
	private Bitmap mCoverBitmap = null;
	private Bitmap mProfileBitmap = null;
	ImageLoader mImageLoader;
	
	public static SettingsActivity mInstance;
	
	private final List<String> PERMISSIONS = Arrays.asList("user_photos");
	private boolean bCallMain = false;
	private boolean bCallPermission = false;
	
	public static boolean fromSettings;
	
	ProgressDialog mProgressDialog;
	
	public SettingsActivity() { 
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		ParseUser currentUser = ParseUser.getCurrentUser();
		mImageLoader = new ImageLoader(this);
				
		coverImageView = (ImageView) this.findViewById(R.id.iv_menu_settings_cover);
		TheMissApplication.getInstance().displayUserCoverImage(currentUser, coverImageView);
		
		profileImageView = (ImageView) this.findViewById(R.id.iv_menu_settings_profile_picture);
		TheMissApplication.getInstance().displayUserProfileImage(currentUser, profileImageView);
		
		Button changeCoverButton = (Button) this.findViewById(R.id.ib_menu_settings_change_cover);
		changeCoverButton.setOnClickListener(this);
		
		Button changeProfilePictureButton = (Button) this.findViewById(R.id.ib_menu_settings_change_profile_picture);
		changeProfilePictureButton.setOnClickListener(this);
		
		mUserNameEditText = (EditText) this.findViewById(R.id.et_menu_settings_username);
		mUserNameEditText.setText(currentUser.getUsername());
		
		//set gender
		String female = getResources().getString(R.string.signup_female);
		String male = getResources().getString(R.string.signup_male);
		gender = new String[]{female, male};
		
		mSpinnerGender = (Spinner) this.findViewById(R.id.spinner_menu_settings_gender);
		if(currentUser.get("gender") != null){
			String userGender = currentUser.getString("gender");
			if(userGender.equalsIgnoreCase("female")){
				userGender = getResources().getString(R.string.signup_female);
			}else{
				userGender = getResources().getString(R.string.signup_male);
			}
			ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Arrays.asList(userGender));
			mSpinnerGender.setAdapter(adapter_gender);
			mSpinnerGender.setSelection(0);
			mSpinnerGender.setEnabled(false);
		}else{
			ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gender);
			mSpinnerGender.setAdapter(adapter_gender);
			mSpinnerGender.setEnabled(true);
		}
		
		ArrayAdapter<String> adapter_language = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, language);
		mSpinnerLanguage = (Spinner) this.findViewById(R.id.spinner_menu_settings_language);
		mSpinnerLanguage.setAdapter(adapter_language);
		if(currentUser.get("language") != null && currentUser.getString("language").equalsIgnoreCase("Italian")){
			mSpinnerLanguage.setSelection(1);
		}else{
			mSpinnerLanguage.setSelection(0);
		}
		
		mFirstNameEditText = (EditText) this.findViewById(R.id.et_menu_settings_firstname);
		if(currentUser.get("firstName") != null)
			mFirstNameEditText.setText(currentUser.get("firstName").toString());
		
		mLastNameEditText = (EditText) this.findViewById(R.id.et_menu_settings_lastname);
		if(currentUser.get("lastName") != null)
			mLastNameEditText.setText(currentUser.get("lastName").toString());
		
		mCityEditText = (EditText) this.findViewById(R.id.et_menu_settings_city);
		if(currentUser.get("city") != null)
			mCityEditText.setText(currentUser.get("city").toString());
		
		mMobileNumberEditText = (EditText) this.findViewById(R.id.et_menu_settings_mobile);
		if(currentUser.get("mobileNumber") != null)
			mMobileNumberEditText.setText(currentUser.get("mobileNumber").toString());
		
		mDescriptionEditText = (EditText) this.findViewById(R.id.et_menu_settings_description);
		if(currentUser.get("description") != null)
			mDescriptionEditText.setText(currentUser.get("description").toString());
		
		mEmailEditText = (EditText) this.findViewById(R.id.et_menu_settings_email);
		if(currentUser.getEmail() != null)
			mEmailEditText.setText(currentUser.getEmail());
		
		mPasswordEditText = (EditText) this.findViewById(R.id.et_menu_settings_password);
		mConfirmPasswordEditText = (EditText) this.findViewById(R.id.et_settings_confirm_password);
				
		mFacebookLinkImageView = (ImageView) this.findViewById(R.id.iv_settings_facebook);
		mInstagramLinkImageView = (ImageView) this.findViewById(R.id.iv_settings_instagram);
		
		mFacebookLayout = (RelativeLayout) this.findViewById(R.id.layout_menu_settings_facebook);
		mFacebookLayout.setOnClickListener(this);
		
		mInstagramLayout = (RelativeLayout) this.findViewById(R.id.layout_menu_settings_instagram);
		mInstagramLayout.setOnClickListener(this);
		
		//if gender is male, hide instagram section
		if(AppManager.isFemale(currentUser) == false){
			mInstagramLayout.setVisibility(View.GONE);
		}
		
		mSaveButton = (Button) this.findViewById(R.id.btn_menu_settings_save);
		mSaveButton.setOnClickListener(this);
		
		mLogoutButton = (Button) this.findViewById(R.id.btn_menu_settings_logout);
		mLogoutButton.setOnClickListener(this);
		
		mDeactivateButton = (Button) this.findViewById(R.id.btn_menu_settings_deactive);
		mDeactivateButton.setOnClickListener(this);
		
		mBackButton = (Button) this.findViewById(R.id.btn_settings_back);
		mBackButton.setOnClickListener(this);
		
		if(currentUser.getBoolean("deactive") == true){
			mDeactivateButton.setText(getResources().getString(R.string.activate_account));
		}else{
			mDeactivateButton.setText(getResources().getString(R.string.deactivate_account));
		}
		
		//If you want to add socail network, scroll to the end
		Bundle bundle = getIntent().getExtras();
		mScrollView = (ScrollView) this.findViewById(R.id.scrollView_settings);
		if(bundle != null && bundle.getBoolean("ScrollToEnd") == true){
			scrollToEnd();
		}
		
		mInstance = this;
	}
		
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ParseUser currentUser = ParseUser.getCurrentUser();
		Intent intent;
		
		switch(v.getId()){
			case R.id.ib_menu_settings_change_cover:
				selectedChangeBtn = CHANGE_COVER_BUTTON;
				selectImageFromCameraOrGallery();
				break;
				
			case R.id.ib_menu_settings_change_profile_picture:
				selectedChangeBtn = CHANGE_PROFILE_BUTTON;
				selectImageFromCameraOrGallery();
				break;
				
			case R.id.layout_menu_settings_facebook:
				Session session = Session.getActiveSession();
				if(currentUser.get("facebookID") != null && session != null && session.isOpened() == true){
					//if user logged in with facebook, can't disconnect.
					String logInWay = currentUser.getString("loggedInWay");
					if(logInWay != null && logInWay.equalsIgnoreCase("facebook")){
						Toast.makeText(this, "You can't disconnect from Facebook in this account.", Toast.LENGTH_LONG).show();
						return;
					}
					
					currentUser.remove("facebookID");
					mProgressDialog = ProgressDialog.show(this, "", "Unlinking...", true);
					
					try {
						if(ParseFacebookUtils.isLinked(currentUser))
							ParseFacebookUtils.unlink(currentUser);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
					if(Session.getActiveSession() != null) Session.getActiveSession().closeAndClearTokenInformation();
					
					currentUser.saveInBackground(new SaveCallback(){

						@Override
						public void done(ParseException err) {
							// TODO Auto-generated method stub
							if(mProgressDialog != null && mProgressDialog.isShowing())
								mProgressDialog.dismiss();
							
							if(err == null){
								bCallPermission = false;
								mFacebookLinkImageView.setImageResource(R.drawable.settings_link_unchecked);
							}
						}
						
					});
				}else{
					connectToFacebook();
				}
				break;
				
			case R.id.layout_menu_settings_instagram:
				if(currentUser.get("instagramID") == null){
					new MainLoginActivity(this, 1);
					MainLoginActivity.loginWithInstagram(this);
				}else{
					//if user logged in with instagram, can't disconnect.
					String logInWay = currentUser.getString("loggedInWay");
					if(logInWay != null && logInWay.equalsIgnoreCase("instagram")){
						Toast.makeText(this, "You can't disconnect from Instagram in this account.", Toast.LENGTH_LONG).show();
						return;
					}
					
					currentUser.remove("instagramID");
					mProgressDialog = ProgressDialog.show(this, "", "Unlinking...", true);
					currentUser.saveInBackground(new SaveCallback(){

						@Override
						public void done(ParseException err) {
							// TODO Auto-generated method stub
							if(mProgressDialog != null && mProgressDialog.isShowing())
								mProgressDialog.dismiss();
							
							if(err == null){
								mInstagramLinkImageView.setImageResource(R.drawable.settings_link_unchecked);
							}
						}
						
					});
				}
				break;
				
			case R.id.btn_menu_settings_save:
				saveUserData();
				break;
					
			case R.id.btn_menu_settings_logout:
				intent = new Intent(this, LogoutActivity.class);
				startActivity(intent);
				break;
				
			case R.id.btn_menu_settings_deactive:
				if(currentUser.getBoolean("deactive") == true){
					mProgressDialog = ProgressDialog.show(this, "", "Activating...", true);
					currentUser.put("deactive", false);
					currentUser.saveInBackground(new SaveCallback(){
		
						@Override
						public void done(ParseException err) {
							// TODO Auto-generated method stub
							if(err == null){
								mDeactivateButton.setText(getResources().getString(R.string.deactivate_account));
							}
							if(mProgressDialog != null && mProgressDialog.isShowing())
								mProgressDialog.dismiss();
						}
						
					});
					
				}else{
					intent = new Intent(this, DeactivateActivity.class);
					startActivityForResult(intent, REQUEST_DEACTIVATE);
				}
				break;
				
			case R.id.btn_settings_back:
				super.onBackPressed();
				break;
		}
	}
	
	public void selectImageFromCameraOrGallery(){
		AlertDialog.Builder buildSingle = new AlertDialog.Builder(this);
		buildSingle.setTitle(getResources().getString(R.string.upload_photo));
		final ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item);
		aAdapter.add(getResources().getString(R.string.take_picture));
		aAdapter.add(getResources().getString(R.string.gallery));

		buildSingle.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		buildSingle.setAdapter(aAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						photoOptionChosen = aAdapter.getItem(which)
								.toString();
						if (photoOptionChosen.equals(getResources().getString(R.string.take_picture))) {
							
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");							
				            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				            startActivityForResult(intent, PICK_FROM_CAMERA);
				            
						} else if (photoOptionChosen.equals(getResources().getString(R.string.gallery))) {
							Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							intent.setType("image/*");
//							intent.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(Intent.createChooser(
									intent, "Select File"),
									PICK_FROM_FILE);// one can be replced
													// with any action code
						}
					}
				});
		buildSingle.show();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		//set language
      	TheMissApplication.getInstance().setLanguage();
      	
		setFacebookButtonStatus();
		setInstagramButtonStatus();
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 
	    super.onActivityResult(requestCode, resultCode, data);
	    System.out.println(" ****** R code"+requestCode+" res code"+resultCode+"data"+data);
	    
	    Bitmap bm = null;
	    
	    switch (requestCode) {
	    	    
		case PICK_FROM_CAMERA:
			if(resultCode == Activity.RESULT_OK)
		    {
				File f = new File(Environment.getExternalStorageDirectory().toString());
				for(File temp : f.listFiles()){
					if(temp.getName().equals("temp.jpg")){
						f = temp;
						break;
					}
				}
				try{
					//display selected image
					
					if(selectedChangeBtn == CHANGE_COVER_BUTTON){
						bm = mImageLoader.decodeFile(new File(f.getAbsolutePath()), 800, 400);
						mCoverBitmap = UtilityMethods.rotateBitmap(bm, f.getAbsolutePath());
						if(mCoverBitmap != null){
							coverImageView.setImageBitmap(mCoverBitmap);
						}
						
					}else if(selectedChangeBtn == CHANGE_PROFILE_BUTTON){
						bm = mImageLoader.decodeFile(new File(f.getAbsolutePath()),400, 400);
						mProfileBitmap = UtilityMethods.rotateBitmap(bm, f.getAbsolutePath());
						if(mProfileBitmap != null){
							mProfileBitmap = UtilityMethods.getCropedBitmap(mProfileBitmap, getResources().getDimensionPixelSize(R.dimen.settings_profile_picture_width));
							profileImageView.setImageBitmap(mProfileBitmap);
						}
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
		    }
			break;
		case PICK_FROM_FILE:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
				String photoPath = cursor.getString(columnIndex);

				//display selected image
				
				if(selectedChangeBtn == CHANGE_COVER_BUTTON){
					bm = mImageLoader.decodeFile(new File(photoPath),1280, 1280);
					Log.e("cover image width:", String.valueOf(bm.getWidth()));
					Log.e("cover image height:", String.valueOf(bm.getHeight()));
					
					mCoverBitmap = UtilityMethods.rotateBitmap(bm, photoPath);
					if(mCoverBitmap != null){
						coverImageView.setImageBitmap(mCoverBitmap);
					}
					
				}else if(selectedChangeBtn == CHANGE_PROFILE_BUTTON){
					bm = mImageLoader.decodeFile(new File(photoPath),400, 400);
					mProfileBitmap = UtilityMethods.rotateBitmap(bm, photoPath);
					if(mProfileBitmap != null){
						mProfileBitmap = UtilityMethods.getCropedBitmap(mProfileBitmap, getResources().getDimensionPixelSize(R.dimen.settings_profile_picture_width));
						profileImageView.setImageBitmap(mProfileBitmap);
					}
				}
				cursor.close();				
			}
			break;
		
		case REQUEST_DEACTIVATE:
			
			//scroll to bottom
			mScrollView.post(new Runnable() {            
			    @Override
			    public void run() {
			    	mScrollView.fullScroll(View.FOCUS_DOWN);              
			    }
			});
			
			if(resultCode == Activity.RESULT_OK) {
				mProgressDialog = ProgressDialog.show(this, "", "Deactivating...", true);
				
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.put("deactive", true);
				currentUser.saveInBackground(new SaveCallback(){
	
					@Override
					public void done(ParseException err) {
						// TODO Auto-generated method stub
						if(err == null){
							if(mProgressDialog != null && mProgressDialog.isShowing())
								mProgressDialog.dismiss();
							mDeactivateButton.setText(getResources().getString(R.string.activate_account));
						}
					}
					
				});
			}
			break;
			
	 	default:
	 		if(Session.getActiveSession() != null)
		    	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
			break;
	    }	    
	}
	
	public void saveUserData(){
		
		//check if input value is valid
		if(UtilityMethods.checkEditText(this, mUserNameEditText, "UserName") == false) return;
		if(UtilityMethods.checkEditText(this, mEmailEditText, "Email") == false) return;
		
		if(mPasswordEditText.getText().toString().equalsIgnoreCase(mConfirmPasswordEditText.getText().toString()) == false){
			Toast.makeText(this, "Password doesn't match.", Toast.LENGTH_LONG).show();
			return;
		}
		mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.saving), true);
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		//get cover bitmap byte array
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] coverBitmapByteArray = null;
		try {
			if(mCoverBitmap != null){
				mCoverBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
				coverBitmapByteArray = stream.toByteArray();
				stream.flush();
				stream.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//get profile bitmap byte array
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		byte[] profileBitmapByteArray = null;
		try {
			if(mProfileBitmap != null){
				mProfileBitmap.compress(Bitmap.CompressFormat.PNG, 70, stream1);
				profileBitmapByteArray = stream1.toByteArray();
				stream1.flush();
				stream1.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		if(coverBitmapByteArray != null){
			ParseFile coverImageFile = new ParseFile("coverImage.png", coverBitmapByteArray);
			currentUser.put("coverImage", coverImageFile);
		}
		
		if(profileBitmapByteArray != null){
			ParseFile profileImageFile = new ParseFile("profileImage.png", profileBitmapByteArray);
			currentUser.put("profileImage", profileImageFile);
		}
		
		currentUser.setUsername(mUserNameEditText.getText().toString());
		if(mPasswordEditText.getText().toString().length()>0)
			currentUser.setPassword(mPasswordEditText.getText().toString());
		currentUser.setEmail(mEmailEditText.getText().toString());
		currentUser.put("firstName", mFirstNameEditText.getText().toString());
		currentUser.put("lastName", mLastNameEditText.getText().toString());
		currentUser.put("city", mCityEditText.getText().toString());
		currentUser.put("mobileNumber", mMobileNumberEditText.getText().toString());
		currentUser.put("description", mDescriptionEditText.getText().toString());
		
		if(mSpinnerLanguage.getSelectedItemPosition() == 0)
			currentUser.put("language", "English");
		else if(mSpinnerLanguage.getSelectedItemPosition() == 1)
			currentUser.put("language", "Italian");
		
		if(currentUser.get("gender") == null){
			if(mSpinnerGender.getSelectedItemPosition() == 0)
				currentUser.put("gender", "female");
			else if(mSpinnerLanguage.getSelectedItemPosition() == 1)
				currentUser.put("gender", "male");
		}
		
		currentUser.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				
				if (e == null) {
										
					Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
			        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
			        SettingsActivity.this.startActivity(intent);
			        
			        TheMissApplication.getInstance().setLanguage();
			        Toast.makeText(SettingsActivity.this,
							getResources().getString(R.string.user_information_saved),
							Toast.LENGTH_SHORT).show();
						
				} else {
				 	Log.d(TAG, e.getMessage());
				 	Toast.makeText(SettingsActivity.this,
							"Server error: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
				
				if(mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			}
		});
		
	}

	public void setInstagramButtonStatus(){
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if(currentUser.get("instagramID") == null){
			mInstagramLinkImageView.setImageResource(R.drawable.settings_link_unchecked);
		}else{
			mInstagramLinkImageView.setImageResource(R.drawable.settings_link_checked);
		}
	}
	
	public void setFacebookButtonStatus(){
		ParseUser currentUser = ParseUser.getCurrentUser();
		Session session = Session.getActiveSession();
		
		if(currentUser.get("facebookID") != null && session != null && session.isOpened() == true){
			mFacebookLinkImageView.setImageResource(R.drawable.settings_link_checked);
		}else{
			mFacebookLinkImageView.setImageResource(R.drawable.settings_link_unchecked);
		}
	}
	
	public void connectToFacebook(){
		if(Session.getActiveSession() != null)
			Session.getActiveSession().closeAndClearTokenInformation();

		Session.openActiveSession(this, true, new Session.StatusCallback() {

			@Override
	        public void call(final Session session, SessionState state, Exception exception) {
	      	  
				Log.d("Testing", "session opened  "+session.isOpened());
	      		Log.d("Testing", "session closed  "+session.isClosed());
	      		
	      		if (session.isOpened() && bCallPermission == false) {
	      			allowPermission(session);
	      			bCallPermission = true;	    	      
	      		}
	        }
		});
	}
	
	private void allowPermission(Session session)
	{
		if (session != null){
	      	
	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
//	            pendingPublishReauthorization = true;
	        	session.addCallback(_callback);
	            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
	            session.requestNewPublishPermissions(newPermissionsRequest);
	            
	        }else{
	        	saveFacebookId(session);
      			
      			bCallMain = true;
	        }
  		}
		
	}
	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	protected Session.StatusCallback _callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	if(bCallMain == false)
        	{
        		saveFacebookId(session);
        		bCallMain = true;
        	}
        }
    };
	
	public void saveFacebookId(Session session){
		
		if(session == null || session.isOpened() == false) return;
		
		mProgressDialog = ProgressDialog.show(this, "", "Linking...", true);
		
		Request request = Request.newMeRequest(session, 
				new Request.GraphUserCallback() {
					
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
					
		                final ParseUser parseUser = ParseUser.getCurrentUser();
		                parseUser.put("facebookID", user.getId());
		             		               
						 parseUser.saveInBackground(new SaveCallback(){
								
								@Override
								public void done(ParseException arg0) {
									// TODO Auto-generated method stub
									
									if(mProgressDialog != null && mProgressDialog.isShowing())
										mProgressDialog.dismiss();
									
									if(arg0==null){										
										
										if(SettingsActivity.mInstance != null){
											scrollToEnd();
							            	SettingsActivity.mInstance.setFacebookButtonStatus();
							            }
										
//										if (!ParseFacebookUtils.isLinked(parseUser)) {
//											ParseFacebookUtils.link(parseUser, SettingsActivity.this, new SaveCallback(){
//												
//												@Override
//												public void done(ParseException arg0) {
//													// TODO Auto-generated method stub
//													if(ParseFacebookUtils.isLinked(parseUser)){
//														Log.d("Wooho", "user logged in with Facebook");
//														ParseFacebookUtils.saveLatestSessionData(parseUser);
//													}
//												}
//												
//											});
//										}
									}
									
								}
			                	
						 });
								
					}
			});
		
		request.executeAsync();
	}
	
	public void scrollToEnd(){
		mScrollView.postDelayed(new Runnable() {            
		    @Override
		    public void run() {
		    	mScrollView.fullScroll(View.FOCUS_DOWN);
		    }
		}, 100);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Log.e("Settings", " Passed");
	}
	
	
}
