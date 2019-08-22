package com.ghebb.themiss;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.UsersListCellAdapter;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.landing.MailLoginActivity;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MenuFragment extends Fragment implements OnClickListener{
	
	MainActivity mActivity;
	public static MenuFragment mInstance;
	
	ScrollView mMenusScrollView;
	LinearLayout mImportLayout;
	LinearLayout mInviteLayout;
	LinearLayout mSettingsLayout;
	LinearLayout mNotificationsLayout; 
	LinearLayout mLoginLayout;
	LinearLayout mUserInfoLayout;
	ImageView mProfileImageView;
	TextView mUserNameTextView;
	public EditText mSearchEditText;
	
	ListView mUsersListView;
	List<ParseUser> mAllUsersList;
	UsersListCellAdapter mUsersListCellAdapter;
	
	boolean mIsUserLoading = false;
	
	public MenuFragment() { 
		
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.menu, container, false);
		mActivity = (MainActivity) getActivity();
		mInstance = this;
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		mUserInfoLayout = (LinearLayout) view.findViewById(R.id.layout_user_info);
		mUserNameTextView = (TextView) view.findViewById(R.id.tv_menu_username);
		mUserNameTextView.setText(currentUser.getUsername());
		mUserNameTextView.setOnClickListener(this);
		
		mProfileImageView = (ImageView) view.findViewById(R.id.iv_menu_profile);
		mProfileImageView.setOnClickListener(this);
			
        TheMissApplication.getInstance().displayUserProfileImage(currentUser, mProfileImageView);
        
		//The contest buttons
		Button prizesButton = (Button) view.findViewById(R.id.btn_menu_prizes);
		prizesButton.setOnClickListener(this);
		
		Button tutorialButton = (Button) view.findViewById(R.id.btn_menu_tutorial);
		tutorialButton.setOnClickListener(this);
		
		Button rulesButton = (Button) view.findViewById(R.id.btn_menu_rules);
		rulesButton.setOnClickListener(this);
		
		//The Help buttons
		Button contactsButton = (Button) view.findViewById(R.id.btn_menu_contacts);
		contactsButton.setOnClickListener(this);
		
		Button faqButton = (Button) view.findViewById(R.id.btn_menu_faq);
		faqButton.setOnClickListener(this);
		
		mImportLayout = (LinearLayout) view.findViewById(R.id.layout_import);
		Button importButton = (Button) view.findViewById(R.id.btn_menu_import_pictures);
		importButton.setOnClickListener(this);
		
		mInviteLayout = (LinearLayout) view.findViewById(R.id.layout_invite);
		Button inviteButton = (Button) view.findViewById(R.id.btn_menu_invite_friends);
		inviteButton.setOnClickListener(this);
		
		//if male, remove "Import Pictures"
		if(currentUser != null && currentUser.getString("gender") != null){
			if(currentUser.getString("gender").equalsIgnoreCase("female")){
				mImportLayout.setVisibility(View.VISIBLE);
				
			}else if(currentUser.getString("gender").equalsIgnoreCase("male")){
				mImportLayout.setVisibility(View.GONE);
			}
		}
		
//		mNotificationsLayout = (LinearLayout) view.findViewById(R.id.layout_notifications);
//		Button notificationsButton = (Button) view.findViewById(R.id.btn_menu_notifications);
//		notificationsButton.setOnClickListener(this);
		
		mSettingsLayout = (LinearLayout) view.findViewById(R.id.layout_settings);
		Button settingsButton = (Button) view.findViewById(R.id.btn_menu_settings);
		settingsButton.setOnClickListener(this);
			
		mLoginLayout = (LinearLayout) view.findViewById(R.id.layout_menu_login);
		Button facebookButton = (Button) view.findViewById(R.id.btn_menu_facebook);
		facebookButton.setOnClickListener(this);
		ImageButton mailButton = (ImageButton) view.findViewById(R.id.ib_menu_mail);
		mailButton.setOnClickListener(this);

		mMenusScrollView = (ScrollView) view.findViewById(R.id.scrollView_menus);
		mMenusScrollView.setVisibility(View.VISIBLE);
		mUsersListView = (ListView) view.findViewById(R.id.lv_menu_users);
		mUsersListView.setVisibility(View.GONE);
		
		//init search box
		final ImageView clearImageView = (ImageView)view.findViewById(R.id.imageView_clear);
		mSearchEditText = (EditText) view.findViewById(R.id.et_menu_search);
		mSearchEditText.addTextChangedListener(new TextWatcher()
        {
			@Override
            public void afterTextChanged(Editable s)
            {
            	if(s.toString().length()>0){
            		clearImageView.setVisibility(View.VISIBLE);
            	}else{
            		clearImageView.setVisibility(View.INVISIBLE);
            		mMenusScrollView.setVisibility(View.VISIBLE);
            		mUsersListView.setVisibility(View.GONE);
            	}
            }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
        });
		
		mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            loadUsers(v.getText().toString());
		            return true;
		        }
		        return false;
		    }
		});
		//-----------click canel icon----------------
		clearImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchEditText.setText("");
				clearImageView.setVisibility(View.INVISIBLE);
			}
		});
		
		return view;
	}
		
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		Fragment newContent = null;
		Intent intent;
		UtilityMethods.hideSoftInput(mActivity, mSearchEditText);
		
		switch(v.getId()){
			case R.id.iv_menu_profile:
			case R.id.tv_menu_username:
				mActivity.toggle();
				ProfileFragment profileFragment = new ProfileFragment();
				profileFragment.mUser = ParseUser.getCurrentUser();
				mActivity.addContent(profileFragment, ProfileFragment.TAG);
				break;
								
			case R.id.btn_menu_prizes:
				newContent = new PrizesFragment();
				mActivity.addContent(newContent, PrizesFragment.TAG);
				break;
				
			case R.id.btn_menu_tutorial:
				newContent = new TutorialFragment();
				mActivity.addContent(newContent, TutorialFragment.TAG);
				break;
				
			case R.id.btn_menu_rules:
				newContent = new RulesFragment();
				mActivity.addContent(newContent, RulesFragment.TAG);
				break;

			case R.id.btn_menu_contacts:
				newContent = new ContactsFragment();
				mActivity.addContent(newContent, ContactsFragment.TAG);
				break;
				
			case R.id.btn_menu_faq:
				newContent = new FAQFragment();
				mActivity.addContent(newContent, FAQFragment.TAG);
				break;
				
			case R.id.btn_menu_import_pictures:
				if(AppManager.isLoggedIn(mActivity) == false) return;
				
				newContent = new ImportFragment();
				mActivity.addContent(newContent, ImportFragment.TAG);
				break;
			
			case R.id.btn_menu_invite_friends:
				if(AppManager.isLoggedIn(mActivity) == false) return;
				
				newContent = new InviteFragment();
				mActivity.addContent(newContent, InviteFragment.TAG);
				break;
				
			case R.id.btn_menu_notifications:
				newContent = new NotificationFragment();
				mActivity.addContent(newContent, NotificationFragment.TAG);
				break;
				
			case R.id.btn_menu_settings:
//				if(Utils.checkLoggedInUser(mActivity) == false) return;
				mActivity.toggle();
				ParseUser currentUser = ParseUser.getCurrentUser();
				if(currentUser.getBoolean("admin")==true){
					newContent = new AdminSettingsFragment();
					mActivity.addContent(newContent, AdminSettingsFragment.TAG);
					
				}else{
					intent = new Intent(mActivity, SettingsActivity.class);
					startActivity(intent);
				}
				break;
				
			case R.id.btn_menu_facebook:
				mActivity.toggle();
				new MainLoginActivity(mActivity, 0);
				MainLoginActivity.mInstance.loginWithFacebook();
				break;
										
			case R.id.ib_menu_mail:
				mActivity.toggle();
				intent = new Intent(mActivity, MailLoginActivity.class);
				startActivity(intent);
				break;
		}
	}
	
	public void loadUsers(String searchText){
		if(mIsUserLoading == true){
			return;
		}
		
		TheMissApplication.getInstance().showProgressFullScreenDialog(mActivity);
		
		mIsUserLoading = true;
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereMatches("username", searchText, "i");
		query.orderByAscending("username");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.findInBackground(new FindCallback<ParseUser>(){

			@Override
			public void done(List<ParseUser> list, ParseException err) {
				// TODO Auto-generated method stub
				TheMissApplication.getInstance().hideProgressDialog();
				
				if(err == null && list != null){
					mAllUsersList = new ArrayList<ParseUser>();
					for(ParseUser user : list){
						if(user.getString("loggedInWay") != null){
							mAllUsersList.add(user);
						}
					}
					
					mUsersListCellAdapter = new UsersListCellAdapter(MenuFragment.this, mAllUsersList);
	        		mUsersListView.setAdapter(mUsersListCellAdapter);
	        		
	        		mMenusScrollView.setVisibility(View.GONE);
            		mUsersListView.setVisibility(View.VISIBLE);
				}
				
				mIsUserLoading = false;
			}
			
		});
	}
}
