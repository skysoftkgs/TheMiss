package com.ghebb.themiss;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.HomeGridLastPicturesCellAdapter;
import com.ghebb.themiss.adapter.HomeLastPicturesListCellAdapter;
import com.ghebb.themiss.adapter.HomeMissOfMonthListCellAdapter;
import com.ghebb.themiss.adapter.HomeMissOfMonthShortListCellAdapter;
import com.ghebb.themiss.adapter.WinnerListCellAdapter;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.NotificationService;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.plattysoft.ui.GridItemClickListener;


public class HomeFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "HomeFragment";
	
	final int ACTIVE_DISPLAYMODE_LIST = 0;
	final int ACTIVE_DISPLAYMODE_GRID = 1;
	final int IMAGE_FULL_MODE = 0;
	final int IMAGE_THUMBNAIL_MODE = 1;
	final int GRID_VERTICAL_SPACING = 2;
	int mLastPicturesPageNo = 0;
	int mMissOfMonthPageNo = 0;
	
	Button mLastPicturesButton;
	Button mLastPicturesButton1;
	Button mMissOfMonthButton;
	Button mMissOfMonthButton1;
	Button mWinnersButton;
	Button mWinnersButton1;
	Button mSignupButton;
	Button mFemaleInviteButton;
	Button mMaleInviteButton;
	ImageButton mListModeImageButton;
	ImageButton mGridModeImageButton;
	
	Button mBackButton;
	ImageButton mMenuNavButton;
	ImageButton mCloseButton;
	ImageView mLogoImageView;
	public ImageButton mRefreshButton;
	public ProgressBar mRefreshingProgressBar;
	
	TextView mMonthTextView;
	TextView mCountDownTimeTextView;
	ListView mPostsListView;
	
	RelativeLayout mHomeTimeLayout;
	LinearLayout mHomeDisplayModeLayout;
	LinearLayout mHomeFemaleWinnersLayout;
	LinearLayout mHomeMaleWinnersLayout;
	LinearLayout mTopCategoryLayout;
	LinearLayout mMiddleCategoryLayout;
	RelativeLayout mBannerLayout;
	
	MainActivity mActivity;
	HomeLastPicturesListCellAdapter mLastPicturesListCellAdapter;
	HomeMissOfMonthListCellAdapter mMissOfMonthListCellAdapter;
	HomeGridLastPicturesCellAdapter mHomeGridLastPicturesCellAdapter;
	HomeMissOfMonthShortListCellAdapter mHomeMissOfMonthShortListCellAdapter;
	
	private int mActiveCategoryBtn = 0;
	private int mActiveDisplayModeBtn;
	private boolean mRefreshingLastPictures;
	private boolean mRefreshingMissOfMonth;
	private boolean mRefreshingWinner;
	private boolean mIsAllLastPicturesLoaded;
	private boolean mIsAllMissOfMonthPicturesLoaded;
	public static boolean mRequiredRefresh;
	public static PostModel mSelectedPostObject;
	public static TextView mShareCountTextView;
	
	boolean mListScrolling;

	private final Observer shareNotification = new Observer()
	{
		public void update(Observable observable, Object data)
		{
			shareAction();
		}
	};
	
	GridItemClickListener mGridItemClickListener = new GridItemClickListener(){

		@Override
		public void onGridItemClicked(View v, int position, long itemId) {
			// TODO Auto-generated method stub
			ProfileFragment profileFragment = new ProfileFragment();
			if(mActiveCategoryBtn == Constants.ACTIVE_HOME_LASTPICTURES){
				profileFragment.mUser = AppManager.mLastPicturesList.get(position).getUser();
			}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){
				profileFragment.mUser = AppManager.mMissOfMonthList.get(position).getUser();
			}
			mActivity.addContent(profileFragment, ProfileFragment.TAG);
		}
		
	};
	
	@SuppressLint("DefaultLocale")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		View header = View.inflate(mActivity, R.layout.header_home, null);
		mPostsListView = (ListView) view.findViewById(R.id.lv_post);
		mPostsListView.addHeaderView(header);
		mPostsListView.setAdapter(null);
		mPostsListView.setScrollingCacheEnabled(false);
		mPostsListView.setAnimationCacheEnabled(false);     
        
		mPostsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true){

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				super.onScroll(arg0, arg1, arg2, arg3);				
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, final int scrollState) {
				// TODO Auto-generated method stub
				
				super.onScrollStateChanged(arg0, scrollState);
				
				if(scrollState == 0)
					mListScrolling = false;
				else
					mListScrolling = true;
				
				if(mMiddleCategoryLayout == null || mTopCategoryLayout == null) return;
				
				if(scrollState == 0 && getYPosition(mMiddleCategoryLayout) < getYPosition(mTopCategoryLayout) && mPostsListView.getHeaderViewsCount()>0){
					mTopCategoryLayout.setVisibility(View.VISIBLE);
//					new Handler().postDelayed(new Runnable() {
//
//						@Override
//						public void run() {
//							if(getYPosition(mMiddleCategoryLayout) >= getYPosition(mTopCategoryLayout))
//								mTopCategoryLayout.setVisibility(View.INVISIBLE);
//						}
//					}, 1000);
				}
				else
					mTopCategoryLayout.setVisibility(View.INVISIBLE);
				}
			
		});

		mSignupButton = (Button) header.findViewById(R.id.btn_home_signup);
		mSignupButton.setOnClickListener(this);
		
		mLastPicturesButton = (Button) header.findViewById(R.id.btn_last_pictures);
		mLastPicturesButton.setOnClickListener(this);
		mLastPicturesButton1 = (Button) view.findViewById(R.id.btn_last_pictures1);
		mLastPicturesButton1.setOnClickListener(this);
		
		mMissOfMonthButton = (Button) header.findViewById(R.id.btn_miss_of_month);
		mMissOfMonthButton.setOnClickListener(this);
		mMissOfMonthButton1 = (Button) view.findViewById(R.id.btn_miss_of_month1);
		mMissOfMonthButton1.setOnClickListener(this);
		
		mWinnersButton = (Button) header.findViewById(R.id.btn_winners);
		mWinnersButton.setOnClickListener(this);
		mWinnersButton1 = (Button) view.findViewById(R.id.btn_winners1);
		mWinnersButton1.setOnClickListener(this);
		
		mFemaleInviteButton = (Button) header.findViewById(R.id.btn_home_winners_female_invite);
		mFemaleInviteButton.setOnClickListener(this);
		
		mMaleInviteButton = (Button) header.findViewById(R.id.btn_home_winners_male_invite);
		mMaleInviteButton.setOnClickListener(this);
		
		//check display signup or invite
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==true){
			mSignupButton.setText(getResources().getString(R.string.home_invite));
			mFemaleInviteButton.setText(getResources().getString(R.string.home_invite));
			mMaleInviteButton.setText(getResources().getString(R.string.home_invite));
		}else{
			mSignupButton.setText(getResources().getString(R.string.login_signup));
			mFemaleInviteButton.setText(getResources().getString(R.string.login_signup));
			mMaleInviteButton.setText(getResources().getString(R.string.login_signup));
		}
		
		mListModeImageButton = (ImageButton) header.findViewById(R.id.ib_home_listview);
		mListModeImageButton.setOnClickListener(this);
		
		mGridModeImageButton = (ImageButton) header.findViewById(R.id.ib_home_gridview);
		mGridModeImageButton.setOnClickListener(this);
			
		//set banner's height to be same as width
		mBannerLayout = (RelativeLayout) header.findViewById(R.id.layout_banner);
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==true){
			mBannerLayout.setVisibility(View.GONE);
		}else{
			ViewGroup.LayoutParams bannerParams = mBannerLayout.getLayoutParams();
			bannerParams.height = AppManager.mScreenWidth;
			mBannerLayout.setLayoutParams(bannerParams);
		}
		
		//display this month
		mMonthTextView = (TextView) header.findViewById(R.id.tv_home_miss_month);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		mMonthTextView.setText(dateFormat.format(cal.getTime()).toUpperCase());
		
		//set countdown timer
		mCountDownTimeTextView = (TextView) header.findViewById(R.id.tv_home_countdown_time);
		Timer timer = new Timer();
	    timer.schedule(new Updater(mCountDownTimeTextView), 1000, 1000);
			    
		mCloseButton = (ImageButton) header.findViewById(R.id.ib_home_close);
		mCloseButton.setOnClickListener(this);
		
		mMenuNavButton = (ImageButton) mActivity.findViewById(R.id.ib_menu_nav);
		mBackButton = (Button) mActivity.findViewById(R.id.btn_menu_back);
		mLogoImageView = (ImageView) mActivity.findViewById(R.id.iv_menu_themiss);
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);
		
		
		mHomeTimeLayout = (RelativeLayout) header.findViewById(R.id.layout_home_time);
		mHomeDisplayModeLayout = (LinearLayout) header.findViewById(R.id.layout_home_display_mode);
		mHomeFemaleWinnersLayout = (LinearLayout) header.findViewById(R.id.layout_home_female_winners);
		mHomeMaleWinnersLayout = (LinearLayout) header.findViewById(R.id.layout_home_male_winners);
		
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.VISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.VISIBLE);
		
		Button backButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
		backButton.setVisibility(View.INVISIBLE);
			
		if(AppManager.mHomeCloseHidden == true)
			mBannerLayout.setVisibility(View.GONE);

		//refresh at startup
		mRequiredRefresh = true;
		
		//for three category button position when scrolling
		mTopCategoryLayout = (LinearLayout) view.findViewById(R.id.layout_top_category);
		mMiddleCategoryLayout = (LinearLayout) header.findViewById(R.id.layout_middle_category);
		mTopCategoryLayout.setVisibility(View.INVISIBLE);
		
		NotificationService.getInstance().addObserver(Constants.NOTIFICATION_SHARE_HOME_SUCCESS, shareNotification);
		
		view.setOnClickListener(null);
		return view;
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(mActivity.currentFragment != null && !this.getClass().equals(mActivity.currentFragment)) return;
		
		mBackButton.setVisibility(View.INVISIBLE);
		mMenuNavButton.setVisibility(View.VISIBLE);
		mLogoImageView.setVisibility(View.VISIBLE);
	
		mRefreshButton.setOnClickListener(this);
		mActivity.setLoginStatusChanged();
						
		if(mRequiredRefresh == true){
			mRequiredRefresh = false;
			
			mIsAllMissOfMonthPicturesLoaded = false;
			mMissOfMonthPageNo = 0;
			AppManager.mMissOfMonthList.clear();
			
			mIsAllLastPicturesLoaded = false;
			mLastPicturesPageNo = 0;
			AppManager.mLastPicturesList.clear();
			
			if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){	
				displayMissOfMonth();
			}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_WINNERS){
				displayWinners();
			}else{
				displayLastPictures();
			}
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		NotificationService.getInstance().removeObserver(Constants.NOTIFICATION_SHARE_HOME_SUCCESS, shareNotification);
		
		super.onDestroyView();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == Constants.REQUEST_SHARE_ACTION){
//			if(resultCode == Activity.RESULT_OK){
				shareAction();
//			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.btn_home_signup:
			case R.id.btn_home_winners_female_invite:
			case R.id.btn_home_winners_male_invite:
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
				if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==true){
					InviteFragment fragment = new InviteFragment();
					mActivity.addContent(fragment, InviteFragment.TAG);
				}else{
					Intent intent = new Intent(mActivity, MainLoginActivity.class);
					startActivity(intent);
				}
				break;
				
			case R.id.btn_last_pictures:
			case R.id.btn_last_pictures1:
				if(mActiveCategoryBtn != Constants.ACTIVE_HOME_LASTPICTURES)
					displayLastPictures();
				break;
				
			case R.id.btn_miss_of_month:
			case R.id.btn_miss_of_month1:
				if(mActiveCategoryBtn != Constants.ACTIVE_HOME_MISSOFMONTH)
					displayMissOfMonth();
				break;
				
			case R.id.btn_winners:
			case R.id.btn_winners1:
				if(mActiveCategoryBtn != Constants.ACTIVE_HOME_WINNERS)
					displayWinners();
				break;
				
			case R.id.ib_home_listview:
				if(mActiveDisplayModeBtn != ACTIVE_DISPLAYMODE_LIST)
					setListDisplayMode();
				break;
				
			case R.id.ib_home_gridview:
				if(mActiveDisplayModeBtn != ACTIVE_DISPLAYMODE_GRID)
					setGridDisplayMode();
				break;
				
			case R.id.ib_menu_refresh:
									
				mPostsListView.setAdapter(null);
				if(mActiveCategoryBtn == Constants.ACTIVE_HOME_LASTPICTURES){
					if(mRefreshingLastPictures == false){
						
						mIsAllLastPicturesLoaded = false;
						mLastPicturesPageNo = 0;
						AppManager.mLastPicturesList.clear();
						fetchLastPictures();
					}
					
				}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){
					if(mRefreshingMissOfMonth == false){
								
						mIsAllMissOfMonthPicturesLoaded = false;
						mMissOfMonthPageNo = 0;
						AppManager.mMissOfMonthList.clear();
						fetchMissOfMonth();
					}
					
				}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_WINNERS){
					if(mRefreshingWinner == false){
						fetchWinners();
					}
				}
				break;
				
			case R.id.ib_home_close:
				mBannerLayout.setVisibility(View.GONE);
				AppManager.mHomeCloseHidden = true;
				break;
		}
	}

	public void shareAction(){
		if(AppManager.mIsAlreadyShared == false){
			mShareCountTextView.setText(String.valueOf(mSelectedPostObject.getShareCount()+1));
			mSelectedPostObject.put("shareCount", mSelectedPostObject.getShareCount()+1);
	//		mSelectedPostObject.put("totalActionCount", mSelectedPostObject.getTotalActionCount() + 1);
			mSelectedPostObject.saveEventually();
			
			mActivity.saveShareCount(ParseUser.getCurrentUser(), mSelectedPostObject.getUser());
			
			mActivity.sendNotification(mSelectedPostObject, Constants.NOTIFICATION_KIND_SHARE);
			AppManager.mIsAlreadyShared = true;
		}
	}
	
	public void displayLastPictures(){
	
		mLastPicturesButton.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mLastPicturesButton.setTextColor(Color.WHITE);
		mMissOfMonthButton.setBackgroundColor(Color.WHITE);
		mMissOfMonthButton.setTextColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton.setBackgroundColor(Color.WHITE);
		mWinnersButton.setTextColor(getResources().getColor(R.color.home_red_color));
		
		//for top category layout 
		mLastPicturesButton1.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mLastPicturesButton1.setTextColor(Color.WHITE);
		mMissOfMonthButton1.setBackgroundColor(Color.WHITE);
		mMissOfMonthButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton1.setBackgroundColor(Color.WHITE);
		mWinnersButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mTopCategoryLayout.setVisibility(View.INVISIBLE);
		
		mHomeTimeLayout.setVisibility(View.VISIBLE);
		mHomeDisplayModeLayout.setVisibility(View.VISIBLE);
		mHomeFemaleWinnersLayout.setVisibility(View.GONE);
		mHomeMaleWinnersLayout.setVisibility(View.GONE);

		mActiveCategoryBtn = Constants.ACTIVE_HOME_LASTPICTURES;
		mGridModeImageButton.setImageResource(R.drawable.home_gridview_btn);
		
		if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_LIST){
			setListDisplayMode();
		}else if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_GRID){
			setGridDisplayMode();
		}
	}
	
	public void displayMissOfMonth(){
		
		mLastPicturesButton.setBackgroundColor(Color.WHITE);
		mLastPicturesButton.setTextColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton.setTextColor(Color.WHITE);
		mWinnersButton.setBackgroundColor(Color.WHITE);
		mWinnersButton.setTextColor(getResources().getColor(R.color.home_red_color));
		
		//for top category layout 
		mLastPicturesButton1.setBackgroundColor(Color.WHITE);
		mLastPicturesButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton1.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton1.setTextColor(Color.WHITE);
		mWinnersButton1.setBackgroundColor(Color.WHITE);
		mWinnersButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mTopCategoryLayout.setVisibility(View.INVISIBLE);
		
		mHomeTimeLayout.setVisibility(View.VISIBLE);
		mHomeDisplayModeLayout.setVisibility(View.VISIBLE);
		mHomeFemaleWinnersLayout.setVisibility(View.GONE);
		mHomeMaleWinnersLayout.setVisibility(View.GONE);
		
		mActiveCategoryBtn = Constants.ACTIVE_HOME_MISSOFMONTH;
		mGridModeImageButton.setImageResource(R.drawable.home_shortview_btn);
		
		if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_LIST){
			setListDisplayMode();
		}else if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_GRID){
			setGridDisplayMode();
		}
	}
	
	public void displayWinners(){
		mActiveCategoryBtn = Constants.ACTIVE_HOME_WINNERS;
		
		mLastPicturesButton.setBackgroundColor(Color.WHITE);
		mLastPicturesButton.setTextColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton.setBackgroundColor(Color.WHITE);
		mMissOfMonthButton.setTextColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton.setTextColor(Color.WHITE);
		
		//for top category layout 
		mLastPicturesButton1.setBackgroundColor(Color.WHITE);
		mLastPicturesButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mMissOfMonthButton1.setBackgroundColor(Color.WHITE);
		mMissOfMonthButton1.setTextColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton1.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mWinnersButton1.setTextColor(Color.WHITE);
		mTopCategoryLayout.setVisibility(View.INVISIBLE);
		
		mHomeTimeLayout.setVisibility(View.GONE);
		mHomeDisplayModeLayout.setVisibility(View.GONE);
		mPostsListView.setAdapter(null);
		
		if(AppManager.mWinnerList.isEmpty()){
			fetchWinners();
		}else{
			WinnerListCellAdapter adapter = new WinnerListCellAdapter(this, AppManager.mWinnerList);
			mPostsListView.setAdapter(adapter);
		}
	}
	
//	public void setWinnersLayout(){
//		ParseUser currentUser = ParseUser.getCurrentUser();
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
//		
//		if(AppManager.mWinner != null){
//			
//			
//			
//		}else{
//			//if not logged in
//			if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==true &&
//					currentUser.getString("gender") != null && currentUser.getString("gender").equalsIgnoreCase("male")){
//				mWinnerPostLayout.setVisibility(View.GONE);
//				mHomeFemaleWinnersLayout.setVisibility(View.GONE);
//				mHomeMaleWinnersLayout.setVisibility(View.VISIBLE);
//				
//			}else{
//				mWinnerPostLayout.setVisibility(View.GONE);
//				mHomeFemaleWinnersLayout.setVisibility(View.VISIBLE);
//				mHomeMaleWinnersLayout.setVisibility(View.GONE);
//			}
//		}
//	}
	
	public void setListDisplayMode(){
		mActiveDisplayModeBtn = ACTIVE_DISPLAYMODE_LIST;
		
		mListModeImageButton.setBackgroundResource(R.drawable.home_display_mode_bg1);
		mGridModeImageButton.setBackgroundResource(R.drawable.home_display_mode_bg);
		mPostsListView.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.list_divider_height));

		if(mActiveCategoryBtn == Constants.ACTIVE_HOME_LASTPICTURES){
			if(AppManager.mLastPicturesList.size()>0)
			{
				mLastPicturesListCellAdapter = new HomeLastPicturesListCellAdapter(HomeFragment.this, AppManager.mLastPicturesList);
				mLastPicturesListCellAdapter.setNumColumns(1);
				mPostsListView.setAdapter(mLastPicturesListCellAdapter);
				
			}else{
				mPostsListView.setAdapter(null);
				fetchLastPictures();
			}
			
		}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){
			if(AppManager.mMissOfMonthList.size()>0)
			{
				mMissOfMonthListCellAdapter = new HomeMissOfMonthListCellAdapter(HomeFragment.this, AppManager.mMissOfMonthList);
				mMissOfMonthListCellAdapter.setNumColumns(1);
				mPostsListView.setAdapter(mMissOfMonthListCellAdapter);

			}else{
				mPostsListView.setAdapter(null);
				fetchMissOfMonth();
			}
		}
	}
	
	public void setGridDisplayMode(){
		mActiveDisplayModeBtn = ACTIVE_DISPLAYMODE_GRID;
		
		mListModeImageButton.setBackgroundResource(R.drawable.home_display_mode_bg);
		mGridModeImageButton.setBackgroundResource(R.drawable.home_display_mode_bg1);
		mPostsListView.setDividerHeight(GRID_VERTICAL_SPACING);
		
		if(mActiveCategoryBtn == Constants.ACTIVE_HOME_LASTPICTURES){
			if(AppManager.mLastPicturesList.size()>0)
			{
				mHomeGridLastPicturesCellAdapter = new HomeGridLastPicturesCellAdapter(HomeFragment.this, AppManager.mLastPicturesList);
				mHomeGridLastPicturesCellAdapter.setNumColumns(3);
				mHomeGridLastPicturesCellAdapter.setOnGridClickListener(mGridItemClickListener);
				mPostsListView.setAdapter(mHomeGridLastPicturesCellAdapter);

			}else{
				mPostsListView.setAdapter(null);
				fetchLastPictures();
			}
			
		}else if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){
			if(AppManager.mMissOfMonthList.size()>0)
			{
				mHomeMissOfMonthShortListCellAdapter = new HomeMissOfMonthShortListCellAdapter(HomeFragment.this, AppManager.mMissOfMonthList);
				mPostsListView.setAdapter(mHomeMissOfMonthShortListCellAdapter);
				
			}else{
				mPostsListView.setAdapter(null);
				fetchMissOfMonth();
			}
		}		
	}
	
	FindCallback<PostModel> getAllLastPictureObjects(){
		
	    return new FindCallback<PostModel>(){

			@Override
			public void done(List<PostModel> arg0,
					ParseException err) {
				// TODO Auto-generated method stub
				TheMissApplication.getInstance().hideProgressDialog();
				ImageLoader.getInstance().resume();
				mRefreshingLastPictures = false;
				
				if(err != null){
					return;
				}
				
				if(getActivity() != null && arg0 != null){
					if (arg0.size() < Constants.PARSE_POST_LIMIT_COUNT){
						mIsAllLastPicturesLoaded = true;
	                }	
					
					for(PostModel post : arg0){
						ParseUser user = post.getUser();
						if(user != null && user.getBoolean("deactive") == false){
							AppManager.mLastPicturesList.add(post);
						}
					}
															
    				if(mActiveCategoryBtn == Constants.ACTIVE_HOME_LASTPICTURES){
						if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_LIST){
							if(mLastPicturesListCellAdapter != null && mLastPicturesPageNo > 0){
								mLastPicturesListCellAdapter.notifyDataSetChanged();
							}else{
								mLastPicturesListCellAdapter = new HomeLastPicturesListCellAdapter(HomeFragment.this, AppManager.mLastPicturesList);
								mLastPicturesListCellAdapter.setNumColumns(1);
								mPostsListView.setAdapter(mLastPicturesListCellAdapter);
							}
							
						}else if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_GRID){
							if(mHomeGridLastPicturesCellAdapter != null && mLastPicturesPageNo > 0){
								mHomeGridLastPicturesCellAdapter.notifyDataSetChanged();
							}else{
								mHomeGridLastPicturesCellAdapter = new HomeGridLastPicturesCellAdapter(HomeFragment.this, AppManager.mLastPicturesList);
								mHomeGridLastPicturesCellAdapter.setNumColumns(3);
								mHomeGridLastPicturesCellAdapter.setOnGridClickListener(mGridItemClickListener);
								mPostsListView.setAdapter(mHomeGridLastPicturesCellAdapter);
							}
						}
					}	
    				
    				mLastPicturesPageNo++;
				}
			}     	
	    };
	}
	    
	FindCallback<UserInfoModel> getAllMissOfMonthObjects(){
		
	    return new FindCallback<UserInfoModel>(){

			@Override
			public void done(List<UserInfoModel> list,
					ParseException err) {
				// TODO Auto-generated method stub
				
				TheMissApplication.getInstance().hideProgressDialog();
				ImageLoader.getInstance().resume();
				mRefreshingMissOfMonth = false;
				
				if(err != null){
					return;
				}
				
				if(getActivity() != null && list != null){
					if (list.size() < Constants.PARSE_POST_LIMIT_COUNT){
						mIsAllMissOfMonthPicturesLoaded = true;
	                }	
					
					for(UserInfoModel userInfo : list){
						ParseUser user = userInfo.getUser();
						if(user != null && user.getBoolean("deactive") == false && userInfo.getLastPost() != null){
							AppManager.mMissOfMonthList.add(userInfo);
						}
					}
																													
					if(mActiveCategoryBtn == Constants.ACTIVE_HOME_MISSOFMONTH){
						if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_LIST){
							if(mMissOfMonthListCellAdapter != null && mMissOfMonthPageNo > 0){
								mMissOfMonthListCellAdapter.notifyDataSetChanged();
							}else{
								mMissOfMonthListCellAdapter = new HomeMissOfMonthListCellAdapter(HomeFragment.this, AppManager.mMissOfMonthList);
								mMissOfMonthListCellAdapter.setNumColumns(1);
								mPostsListView.setAdapter(mMissOfMonthListCellAdapter);
							}
							
						}else if(mActiveDisplayModeBtn == ACTIVE_DISPLAYMODE_GRID){
							if(mHomeMissOfMonthShortListCellAdapter != null && mMissOfMonthPageNo > 0){
								mHomeMissOfMonthShortListCellAdapter.notifyDataSetChanged();
							}else{
								mHomeMissOfMonthShortListCellAdapter = new HomeMissOfMonthShortListCellAdapter(HomeFragment.this, AppManager.mMissOfMonthList);
								mPostsListView.setAdapter(mHomeMissOfMonthShortListCellAdapter);
							}
						}
					}
					
					mMissOfMonthPageNo++;
					TheMissApplication.getInstance().hideProgressDialog();
					ImageLoader.getInstance().resume();
					
					mRefreshingMissOfMonth = false;
					mRefreshingProgressBar.setVisibility(View.INVISIBLE);
					mRefreshButton.setVisibility(View.VISIBLE);
				}
			}
	    };
	}

	public void fetchLastPictures(){
		if(mRefreshingLastPictures == true) return;
					
		if(mIsAllLastPicturesLoaded && AppManager.mLastPicturesList.size() > 0){
			return;
		}
		
//		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().pause();
		System.gc();
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
		
		mRefreshingLastPictures = true;
		
		try{
			ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
			query.whereGreaterThanOrEqualTo("createdAt", UtilityMethods.getFirstDateOfCurrentMonth());
            query.orderByDescending("createdAt");
            query.setSkip(mLastPicturesPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
            query.include("user");
            query.findInBackground(getAllLastPictureObjects());       
	             
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
	 }
	 
	public void fetchMissOfMonth(){
		if(mRefreshingMissOfMonth == true) return;
		
		if(mIsAllMissOfMonthPicturesLoaded && AppManager.mMissOfMonthList.size() > 0){
			return;
		}
		
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
		
		mRefreshingMissOfMonth = true;
		
		try{
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			String strMonth = String.format("%04d_%02d", year, month+1);
		    ParseQuery<UserInfoModel> query = new ParseQuery<UserInfoModel>("UserInfo");
		    query.whereEqualTo("postMonth", strMonth);
            query.whereGreaterThan("voteCount", 0);
            query.orderByDescending("voteCount");
            query.setSkip(mMissOfMonthPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
            query.include("user");
            query.include("lastPost");
            query.findInBackground(getAllMissOfMonthObjects());       
	             
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
	}
		
	public void fetchWinners(){
		
		if(mRefreshingWinner == true) return;

		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().pause();
		System.gc();
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
		
		mRefreshingWinner = true;
		
		try{
			Calendar calendar = Calendar.getInstance();
			final String strMonth = String.format("%04d_%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
			ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
			query.orderByDescending("postMonth");
			query.whereContains("postMonth", String.valueOf(calendar.get(Calendar.YEAR)));
            query.include("user");
            query.include("lastPost");
            query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
            query.findInBackground(new FindCallback<UserInfoModel>() {

				@Override
				public void done(List<UserInfoModel> list, ParseException err) {
					// TODO Auto-generated method stub
					TheMissApplication.getInstance().hideProgressDialog();
					mRefreshingWinner = false;
					AppManager.mWinnerList.clear();
					
					for(UserInfoModel userInfo : list){
						if(userInfo.getPostMonth().equals(strMonth)){
							continue;
						}
						
						if(AppManager.mWinnerList.size() > 0){
							UserInfoModel lastUserInfo = AppManager.mWinnerList.get(AppManager.mWinnerList.size() - 1);
							if(userInfo.getPostMonth().equals(lastUserInfo.getPostMonth())){
								if(userInfo.getVoteCount() > lastUserInfo.getVoteCount()){
									AppManager.mWinnerList.remove(AppManager.mWinnerList.size() - 1);
									AppManager.mWinnerList.add(userInfo);
								}
							}else{
								AppManager.mWinnerList.add(userInfo);
							}
							
						}else{
							AppManager.mWinnerList.add(userInfo);
						}
					}
					
					
					if(mActiveCategoryBtn == Constants.ACTIVE_HOME_WINNERS){
    					WinnerListCellAdapter adapter = new WinnerListCellAdapter(HomeFragment.this, AppManager.mWinnerList);
    					mPostsListView.setAdapter(adapter);
    				}
					
    				ImageLoader.getInstance().resume();
				}
			});
            
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
	}

	//*********************show clock*********************
	private class Updater extends TimerTask {
	    private final TextView remainTimeTextView;
	
	    public Updater(TextView subject) {
	        this.remainTimeTextView = subject;
	    }
	
	    @Override
	    public void run() {
	    	if(mListScrolling == true) return;
	    	
	    	remainTimeTextView.post(new Runnable() {
	
	            public void run() {
	           	    Calendar now = Calendar.getInstance();
	        	    long diff = (UtilityMethods.getFirstDateOfNextMonth().getTime() - now.getTimeInMillis())/1000;
	        	    int second = (int) (diff % 60);
	        	    int minute = (int) ((diff / 60) %60);
	        	    int hour = (int) ((diff / 3600) % 24);
	        	    int days = (int) (diff/3600/24);
	        	    remainTimeTextView.setText(String.format("%02d:%02d:%02d:%02d", days, hour, minute,second));
		        }
		    });
	    }
	}
		
	public int getYPosition(View view){
		int []viewLocation = new int[2];
		view.getLocationOnScreen(viewLocation);
		return viewLocation[1];
	}
}
