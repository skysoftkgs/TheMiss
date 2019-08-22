package com.ghebb.themiss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.gab.themiss.R;
import com.ghebb.fbphotopicker.FBPhotoPickerActivity;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.imageutil.ImageLoader;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ImportFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "ImportFragment";
	
	RelativeLayout mFacebookLayout;
	RelativeLayout mInstagramLayout;
	RelativeLayout mCameraLayout;
	Button mBackButton;
	TextView mFacebookUserNameTextView;
	ImageButton mMenuRefreshButton;
	ProgressBar mRefreshingProgressBar;
	
	ParseUser mCurrentUser;
	Dialog mProgressDialog;

	private final int PICK_FROM_CAMERA = 1001;
	private final int PICK_FROM_FILE = 1002;
	private final int PICK_FROM_INSTAGRAM = 1003;
	private final int PICK_FROM_FACEBOOK = 1004;
	private final int PICK_FROM_ADJUST = 1005;
	
	private String mPhotoOptionChosen;
		
	MainActivity mActivity;
	ImageLoader mImageLoader;
	int mCompressRate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_import, container, false);
		mActivity = (MainActivity)getActivity();
		mActivity.currentFragment = this.getClass();
		mCurrentUser = ParseUser.getCurrentUser();
		mImageLoader = new ImageLoader(mActivity);
		
		mFacebookLayout = (RelativeLayout) view.findViewById(R.id.layout_import_facebook);
		mFacebookLayout.setOnClickListener(this);
		
		mInstagramLayout = (RelativeLayout) view.findViewById(R.id.layout_import_instagram);
		mInstagramLayout.setOnClickListener(this);
		
		mCameraLayout = (RelativeLayout) view.findViewById(R.id.layout_import_camera);
		mCameraLayout.setOnClickListener(this);
		
		mFacebookUserNameTextView = (TextView) view.findViewById(R.id.tv_import_facebook_username);
				
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);	
		mMenuRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		
		mCurrentUser = ParseUser.getCurrentUser();
		HomeFragment.mRequiredRefresh = false;
		
		view.setOnClickListener(null);
	
		return view;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();
			
		if(mActivity != null && mActivity.currentFragment != null && !this.getClass().equals(mActivity.currentFragment)) return;
		
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.INVISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.INVISIBLE);
		
		Button backButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
		backButton.setOnClickListener(this);
		backButton.setText(getResources().getString(R.string.import_title));
		backButton.setVisibility(View.VISIBLE);
		
		Session session= Session.getActiveSession();
		if(mCurrentUser.getString("facebookID") != null && session != null && session.isOpened() == true){
			mFacebookUserNameTextView.setText(getResources().getString(R.string.connected_as) + " " + mCurrentUser.getUsername());
		}
		
		if(mMenuRefreshButton != null){
			mMenuRefreshButton.setAlpha(50);
			mMenuRefreshButton.setEnabled(false);
		}
		if(mActivity != null && mActivity.mMenuPlusButton != null){
			mActivity.mMenuPlusButton.setAlpha(50);
			mActivity.mMenuPlusButton.setEnabled(false);
		}
	}
		
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.layout_import_facebook:
				Session session = Session.getActiveSession();
				if(mCurrentUser.getString("facebookID") != null && session != null && session.isOpened() == true){
					Intent intent = new Intent(mActivity, FBPhotoPickerActivity.class);
					startActivityForResult(intent, PICK_FROM_FACEBOOK);
				
				}else{
//					Toast.makeText(mActivity, "You must login with facebook in settings page.", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(mActivity, SettingsActivity.class);
					intent.putExtra("ScrollToEnd", true);
					startActivity(intent);
				}
				break;
				
			case R.id.layout_import_instagram:
				if(mCurrentUser.getString("instagramID") != null){
					Intent intent = new Intent(mActivity, InstagramPhotoPickerActivity.class);
			        startActivityForResult(intent, PICK_FROM_INSTAGRAM);
				}
				else{
//					Toast.makeText(mActivity, "You must login with instagram in setting page.", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(mActivity, SettingsActivity.class);
					intent.putExtra("ScrollToEnd", true);
					startActivity(intent);
				}
				break;
				
			case R.id.layout_import_camera:
				selectImageFromCameraOrGallery();
				break;
				
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
				
		}
	}

	public void selectImageFromCameraOrGallery(){
		AlertDialog.Builder buildSingle = new AlertDialog.Builder(mActivity);
		buildSingle.setTitle(getResources().getString(R.string.upload_photo));
		final ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.select_dialog_item);
		aAdapter.add(getResources().getString(R.string.take_picture));
		aAdapter.add(getResources().getString(R.string.gallery));

		buildSingle.setNegativeButton(getResources().getString(R.string.cancel),
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
						mPhotoOptionChosen = aAdapter.getItem(which)
								.toString();
						System.gc();
						if (mPhotoOptionChosen.equals(getResources().getString(R.string.take_picture))) {
							
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");							
				            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				            ImportFragment.this.startActivityForResult(intent, PICK_FROM_CAMERA);
				            
						} else if (mPhotoOptionChosen.equals(getResources().getString(R.string.gallery))) {
							Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							intent.setType("image/*");
//							intent.setAction(Intent.ACTION_GET_CONTENT);
							ImportFragment.this.startActivityForResult(Intent.createChooser(
									intent, "Select File"),
									PICK_FROM_FILE);// one can be replced
													// with any action code
						}
					}
				});
		buildSingle.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 
	    super.onActivityResult(requestCode, resultCode, data);
	    System.out.println(" ****** R code"+requestCode+" res code"+resultCode+"data"+data);
	    
	    Bitmap bm = null;
	    System.gc();
	    if(resultCode != Activity.RESULT_OK){
	    	return;
	    }
	    
	    switch (requestCode) {
		    case PICK_FROM_ADJUST:
		    	Bitmap bitmap = UtilityMethods.getLocalBitmap(getActivity());
		    	uploadImage(bitmap);
		    	break;
		    	
			case PICK_FROM_CAMERA:
				File f = new File(Environment.getExternalStorageDirectory().toString());
				for(File temp : f.listFiles()){
					if(temp.getName().equals("temp.jpg")){
						f = temp;
						break;
					}
				}
                
				try{
					bm = mImageLoader.decodeFile(f, Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_HEIGHT);
//					bm = loadBitmapFromStream(f.getAbsolutePath(), Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_HEIGHT);
					if(bm != null){
						mCompressRate = 70;
						goAdjustScreen(UtilityMethods.rotateBitmap(bm, f.getAbsolutePath()));
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			 
				break;
				
			case PICK_FROM_FILE:
				Uri selectedImage = data.getData();
				if(selectedImage == null) return;
				
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = mActivity.getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String photoPath = cursor.getString(columnIndex);
				
				bm = mImageLoader.decodeFile(new File(photoPath), Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_HEIGHT);
				if(bm != null){
					mCompressRate = 70;
					goAdjustScreen(bm);
				}
				cursor.close();				
			
				break;
				
			case PICK_FROM_INSTAGRAM:
				Bundle bundle = data.getExtras();
				String url = bundle.getString("url");
//				Bitmap bm2 = mImageLoader.getBitmap(url, 0, 0);
				bm = com.nostra13.universalimageloader.core.ImageLoader.getInstance().loadImageSync(url);
				mCompressRate = 90;
				goAdjustScreen(bm);

				break;
				
			case PICK_FROM_FACEBOOK:
				Bundle bundle1 = data.getExtras();
				String url1 = bundle1.getString("photoUrl");
				bm = mImageLoader.getBitmap(url1, 0, 0);
				mCompressRate = 90;
				goAdjustScreen(bm);

				break;
				
		 	default:
				break;
	    }	    
	}
	
	public void goAdjustScreen(Bitmap bm){
		Intent intent = new Intent(getActivity(), AdjustImageActivity.class);
		UtilityMethods.setLocalBitmap(bm, getActivity());
		startActivityForResult(intent, PICK_FROM_ADJUST);
	}
	
	public void uploadImage(Bitmap bm){
		if(bm == null) return;
		
		mProgressDialog = ProgressDialog.show(mActivity, "", getResources().getString(R.string.uploading), true);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteArrayOutputStream streamThumbnail = new ByteArrayOutputStream();
		Bitmap thumbnail = null;
		byte[] byteArray = null;
		byte[] byteThumbnailArray = null;
		try {
			Bitmap cropBmp = UtilityMethods.getCropedBitmap(bm, 0);			
			if(cropBmp == null){
				mProgressDialog.dismiss();
				return;
			}
			
			if(cropBmp.getWidth() > AppManager.mScreenWidth)		//if image width < screen width, don't scale
				thumbnail = Bitmap.createScaledBitmap(cropBmp, cropBmp.getWidth()/3, cropBmp.getWidth()/3, false);
			
			cropBmp.compress(Bitmap.CompressFormat.JPEG, mCompressRate, stream);
			byteArray = stream.toByteArray();
			stream.flush();
			stream.close();
			
			if(thumbnail != null){
				thumbnail.compress(Bitmap.CompressFormat.JPEG, mCompressRate, streamThumbnail);
				byteThumbnailArray = streamThumbnail.toByteArray();
				streamThumbnail.flush();
				streamThumbnail.close();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		final PostModel post = new PostModel();
		ParseFile imageFile = new ParseFile("image.jpg", byteArray);
		post.setPhotoFile(imageFile);
		if(thumbnail != null){
			ParseFile thumbnailImageFile = new ParseFile("thumbnailImage.jpg", byteThumbnailArray);
			post.setThumbnailImageFile(thumbnailImageFile);
		}
		post.setUser(mCurrentUser);
		post.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				try{
					if (e == null && getActivity() != null) {
						Toast.makeText(mActivity,
								getResources().getString(R.string.image_uploaded_success),
								Toast.LENGTH_SHORT).show();

						HomeFragment fragment = new HomeFragment();
						mActivity.replaceContent(fragment);
						
						//send push notificaiton
						mActivity.sendNotification(post, Constants.NOTIFICATION_KIND_NEW_POST);
						
						//for month ranking
						updateLastPhoto(post);
						
					} else {
					 	Toast.makeText(mActivity,
								"Error saving: " + e.getMessage(),
								 Toast.LENGTH_LONG).show();
					}
					
				}catch(Exception excetption){
					excetption.printStackTrace();
				}
				
				if(mProgressDialog != null)
					mProgressDialog.dismiss();
			}
		});
		
	}
	
	public void updateLastPhoto(final PostModel post){
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", post.getUser());
		query.getFirstInBackground(new GetCallback<UserInfoModel>() {

			@Override
			public void done(UserInfoModel userInfo, ParseException err) {
				// TODO Auto-generated method stub
				if(userInfo != null){
					userInfo.setLastPost(post);
					userInfo.saveEventually();
					
				}else{
					UserInfoModel newUserInfo = new UserInfoModel();
					newUserInfo.setLastPost(post);
					newUserInfo.setUser(post.getUser());
					newUserInfo.saveEventually();
				}
			}		
		});
	}
}
