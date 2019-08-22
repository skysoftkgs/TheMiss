package com.ghebb.themiss;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;

public class TheMissApplication extends Application {

	static final String TAG = "The Miss";
	static volatile TheMissApplication application = null;
	private ProgressDialog mProgressDialog = null;
	private ArrayList<Toast> mToasts = new ArrayList<Toast>();
//	private static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
	
	public DisplayImageOptions options;
	
	public TheMissApplication(){
		super();
		application = this;
	}
	
	public static TheMissApplication getInstance(){
		return application;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
						
		initImageLoader(getApplicationContext());

		ParseObject.registerSubclass(PostModel.class);
		ParseObject.registerSubclass(UserInfoModel.class);
//		Parse.enableLocalDatastore(this);
			
		// Add your initialization code here
		Parse.initialize(this, Constants.PARSE_APPID, Constants.PARSE_CLIENTKEY);
		ParseFacebookUtils.initialize(getResources().getString(R.string.facebook_app_id));
			
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		defaultACL.setPublicWriteAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
		
		// Specify a Activity to handle all pushes by default.
		PushService.setDefaultPushCallback(this, MainActivity.class);
	}
	
	public void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.threadPoolSize(2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 100 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
				
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(android.R.color.transparent)
		.showImageForEmptyUri(android.R.color.transparent)
		.showImageOnFail(android.R.color.transparent)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	
	public void displayUserProfileImage(ParseUser user, ImageView imageView){
        ParseFile profileImageFile = (ParseFile) user.get("profileImage");
        if(profileImageFile != null){
        	displayImage(profileImageFile.getUrl(), imageView);
        	
        }else if(user.getString("profileUrl") != null) {
        	displayImage(user.getString("profileUrl"), imageView);
        	
        }else if(AppManager.isFemale(user) == true) {
        	imageView.setImageResource(R.drawable.user_female_256);
        	
        }else{
        	imageView.setImageResource(R.drawable.user_male_256);
        }
	}
	
	public void displayUserCoverImage(ParseUser user, ImageView imageView){
        ParseFile coverImageFile = (ParseFile) user.get("coverImage");
        if(coverImageFile != null){
        	displayImage(coverImageFile.getUrl(), imageView);
        }else{
        	if (user.getString("coverUrl") != null) {
        		displayImage(user.getString("coverUrl"), imageView);
        	}
        }
	}
	
	public void displayImage(String url, ImageView imageView){
		if(url == null) return;
		ImageLoader.getInstance().displayImage(url, imageView, options, null);
	}
	
	public void displayImage(String url, ImageView imageView, final ProgressBar progressBar){
		if(url == null) return;
		
		ImageLoader.getInstance().displayImage(url, imageView, options, new SimpleImageLoadingListener() {
        	
			 @Override
			 public void onLoadingStarted(String imageUri, View view) {
				 progressBar.setProgress(0);
				 progressBar.setVisibility(View.VISIBLE);
			 }

			 @Override
			 public void onLoadingFailed(String imageUri, View view,
					 FailReason failReason) {
				 progressBar.setVisibility(View.GONE);
			 }

			 @Override
			 public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				 progressBar.setVisibility(View.GONE);
//				 if (loadedImage != null) {
//					ImageView imageView = (ImageView) view;
//					boolean firstDisplay = !displayedImages.contains(imageUri);
//					if (firstDisplay) {
//						FadeInBitmapDisplayer.animate(imageView, 500);
//						displayedImages.add(imageUri);
//					}
//				}
			 }
		 }, new ImageLoadingProgressListener() {
			 @Override
			 public void onProgressUpdate(String imageUri, View view, int current,
					 int total) {
				 progressBar.setProgress(Math.round(100.0f * current / total));
			 }
		});
	}
	
	public void setLanguage(){
		ParseUser currentUser = ParseUser.getCurrentUser();
		Locale locale;
		if(currentUser.getString("language") == null){
			if(Locale.getDefault().getLanguage().equalsIgnoreCase("it")){
				locale = new Locale("it");
			}else{
				locale = new Locale("en");
			}
			
		}else{
			if(AppManager.isItalian(currentUser)){
				locale = new Locale("it");
			}else{
				locale = new Locale("en");
			}
		}
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getResources().updateConfiguration(config, null);
	}
	
	public String getString(Object stringOrId){
		
		String string = null;
		if(stringOrId instanceof String)
			string = (String)stringOrId;
		else if(stringOrId instanceof Integer)
			string = super.getString((Integer)stringOrId);
		
		return string;
	}

	/**
	 * Alert Dialog
	 */
	private Dialog commonOkDialog(Context context, Object titleOrId, Object messageOrId, final OnClickListener onClickListener){
		
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setTitle(getString(titleOrId));
		dialogBuilder.setMessage(getString(messageOrId));
		dialogBuilder.setNegativeButton("OK", onClickListener);
		
		return dialogBuilder.create();
	}
	
	public void showErrorDialog(Context context, Object messageOrId){
		showErrorDialog(context, messageOrId, null);
	}
	
	public void showErrorDialog(Context context, Object messageOrId, final OnClickListener onClickListener){
		hideProgressDialog();
		commonOkDialog(context, TheMissApplication.getInstance().getString(R.string.app_name), messageOrId, onClickListener).show();
	}
	
	public void showWarningDialog(Context context, Object messageOrId){
		showWarningDialog(context, messageOrId, null);
	}
	
	public void showWarningDialog(Context context, Object messageOrId, final OnClickListener onClickListener){
		hideProgressDialog();
		commonOkDialog(context, "Warning", messageOrId, onClickListener).show();
	}
	
	
	/**
	 * Progress Dialog
	 */
	private void showProgressDialog(Context context, Object messageOrId, boolean cancelable, final OnCancelListener onCancelListener){
		hideProgressDialog();
		
		if(mProgressDialog == null){
			mProgressDialog = new ProgressDialog(context){
				@Override
				public void onBackPressed(){
					if(onCancelListener != null)
						super.onBackPressed();
				}
			};
		}
		
		mProgressDialog.setMessage(getString(messageOrId));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCanceledOnTouchOutside(cancelable);
		mProgressDialog.setOnCancelListener(new OnCancelListener(){
			
			@Override
			public void onCancel(DialogInterface dialog){
				if(onCancelListener != null)
					onCancelListener.onCancel(dialog);
			}
		});
		
		mProgressDialog.setIndeterminate(true);
		
		mProgressDialog.show();
	}
	
	public void showProgressDialog(Context context, Object messageOrId, final OnCancelListener onCancelListener){
		showProgressDialog(context, messageOrId, false, onCancelListener);
	}
	
	public void showCancelableProgressDialog(Context context, Object messageOrId, final OnCancelListener onCancelListener){
		showProgressDialog(context, messageOrId, true, onCancelListener);
	}
	
	public void showProgressFullScreenDialog(Activity activity){
		hideProgressDialog();
		
		mProgressDialog = ProgressDialog.show(activity, null, null, true);
		mProgressDialog.setContentView(R.layout.progress_layout);
	}
	
	public void hideProgressDialog(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		
		mProgressDialog = null;
	}
	
	/*
	 * Toasts
	 */
	public void showToast(Context context, final Object messageOrId, int duration, boolean cancelAllPrevious)
	{
		if(cancelAllPrevious)
			cancelAllToasts();

		Toast toast = Toast.makeText(context, getString(messageOrId), duration);
		mToasts.add(toast);
		toast.show();
	}

	public void showToast(Context context, final Object messageOrId, int duration)
	{
		showToast(context, messageOrId, duration, true);
	}
	
	public void cancelAllToasts()
	{
		for (Toast toast : mToasts)
		{
			toast.cancel();
		}
		
		mToasts.clear();
	}    
}