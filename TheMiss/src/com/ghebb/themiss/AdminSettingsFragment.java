package com.ghebb.themiss;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.FlaggedPicturesListCellAdapter;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.UtilityMethods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class AdminSettingsFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "AdminSettingsFragment";
	
	ListView mFlaggedPicturesListView;
	EditText mUserNameEditText;
	EditText mEmailEditText;
	EditText mPasswordEditText;
	EditText mConfirmPasswordEditText;
	
	Spinner mSpinnerLanguage;
	Button mSaveButton;
	Button mLogoutButton;
	
	public ImageButton mRefreshButton;
	public ProgressBar mRefreshingProgressBar;
	public ProgressDialog mProgressDialog;

	private String[] language = {"English", "Italian"};
	boolean mRefreshing;
	MainActivity mActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_admin_settings, container, false);
		mActivity = (MainActivity)getActivity();
		mActivity.currentFragment = this.getClass();
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		mFlaggedPicturesListView = (ListView) view.findViewById(R.id.lv_settings_admin);
		mFlaggedPicturesListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		
		View footer = View.inflate(mActivity, R.layout.footer_admin_settings, null);
		mFlaggedPicturesListView.addFooterView(footer);
		mFlaggedPicturesListView.setAdapter(null);
		
		ArrayAdapter<String> adapter_language = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, language);
		mSpinnerLanguage = (Spinner) footer.findViewById(R.id.spinner_menu_settings_language);
		mSpinnerLanguage.setAdapter(adapter_language);
		if(currentUser.get("language") != null && currentUser.getString("language").equalsIgnoreCase("Italian")){
			mSpinnerLanguage.setSelection(1);
		}else{
			mSpinnerLanguage.setSelection(0);
		}
		
		mEmailEditText = (EditText) footer.findViewById(R.id.et_menu_settings_email);
		if(currentUser.getEmail() != null)
			mEmailEditText.setText(currentUser.getEmail());
		
		mPasswordEditText = (EditText) footer.findViewById(R.id.et_menu_settings_password);
		mConfirmPasswordEditText = (EditText) view.findViewById(R.id.et_settings_confirm_password);
		
		mSaveButton = (Button) footer.findViewById(R.id.btn_menu_settings_save);
		mSaveButton.setOnClickListener(this);
	
		mLogoutButton = (Button) footer.findViewById(R.id.btn_menu_settings_logout);
		mLogoutButton.setOnClickListener(this);
		
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		
		//refresh at startup
		refreshFlaggedPictures();
		
		view.setOnClickListener(null);
		return view;
	}
		
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.btn_menu_settings_save:
				saveUserData();
				break;
			case R.id.ib_menu_refresh:
				if(mRefreshing == false){
					refreshFlaggedPictures();
				}
				break;
				
			case R.id.btn_menu_settings_logout:
				Intent intent = new Intent(mActivity, LogoutActivity.class);
				startActivity(intent);
				break;
				
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if(mActivity.currentFragment != null && !this.getClass().equals(mActivity.currentFragment)) return;
		
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.INVISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.INVISIBLE);
		
		Button backButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
		backButton.setOnClickListener(this);
		backButton.setText("Settings");
		backButton.setVisibility(View.VISIBLE);
		
		mRefreshButton.setOnClickListener(this);
		
		if(mRefreshing == true){
			mRefreshButton.setVisibility(View.INVISIBLE);
			mRefreshingProgressBar.setVisibility(View.VISIBLE);
		}else{
			mRefreshButton.setVisibility(View.VISIBLE);
			mRefreshingProgressBar.setVisibility(View.INVISIBLE);
		}
		
		
	}

	public void saveUserData(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==false){
			Toast.makeText(mActivity, "You must login to save user information.", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(UtilityMethods.checkEditText(getActivity(), mEmailEditText, "Email") == false) return;
		if(mPasswordEditText.getText().toString().equalsIgnoreCase(mConfirmPasswordEditText.getText().toString()) == false){
			Toast.makeText(mActivity, "Password doesn't match.", Toast.LENGTH_LONG).show();
			return;
		}
		
		mProgressDialog = ProgressDialog.show(mActivity, "", getResources().getString(R.string.saving), true);
		ParseUser currentUser = ParseUser.getCurrentUser();
		if(mPasswordEditText.getText().toString().length()>0)
			currentUser.setPassword(mPasswordEditText.getText().toString());
		currentUser.setEmail(mEmailEditText.getText().toString());
		if(mSpinnerLanguage.getSelectedItemPosition() == 0)
			currentUser.put("language", "English");
		else if(mSpinnerLanguage.getSelectedItemPosition() == 1)
			currentUser.put("language", "Italian");
		currentUser.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					Toast.makeText(mActivity,
							getResources().getString(R.string.user_information_saved),
							Toast.LENGTH_SHORT).show();
					HomeFragment.mRequiredRefresh = true;

					Intent intent = new Intent(mActivity, MainActivity.class);
			        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
			        startActivity(intent);
						
				} else {
				 	Log.d(TAG, e.getMessage());
				 	Toast.makeText(mActivity,
							"Error saving: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
				
				if(mProgressDialog != null)
					mProgressDialog.dismiss();
			
			}
		});
		
	}
	
	public void refreshFlaggedPictures(){
		if(mRefreshing == true) return;
		
		mRefreshing = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("FlagedPicture");
		query.orderByDescending("createdAt");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.include("post");
		query.include("user");
		
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> list, ParseException arg1) {
				// TODO Auto-generated method stub
				if(arg1 == null){
					FlaggedPicturesListCellAdapter adapter= new FlaggedPicturesListCellAdapter(AdminSettingsFragment.this, list);
					mFlaggedPicturesListView.setAdapter(adapter);
					
					mRefreshing = false;
					mRefreshingProgressBar.setVisibility(View.INVISIBLE);
					mRefreshButton.setVisibility(View.VISIBLE);
					if(mProgressDialog!=null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
				}
			}			
		});
	}
}
