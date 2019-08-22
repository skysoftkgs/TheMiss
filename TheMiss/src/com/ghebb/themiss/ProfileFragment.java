package com.ghebb.themiss;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.ProfileFollowersListCellAdapter;
import com.ghebb.themiss.adapter.ProfileMaleFollowersListCellAdapter;
import com.ghebb.themiss.adapter.ProfilePostsListCellAdapter;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.NotificationService;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.datamodel.UsersListModel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ProfileFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "ProfileFragment";
	final int ACTIVE_PROFILE_POST = 0;
	final int ACTIVE_PROFILE_FOLLOWERS = 1;
	
	final int FOLLOW_ME_STATUS = 0;
	final int FOLLOWING_STATUS = 1;
	final int EDIT_STATUS = 2;
	
	LinearLayout mFemaleInfoLayout;
	LinearLayout mFemaleCategoryLayout;
	LinearLayout mMaleCategoryLayout;
	LinearLayout mMostActiveUserLayout;
	
	RelativeLayout mSelfPostLayout;
	RelativeLayout mFollowersLayout;
	RelativeLayout mVotedPostLayout;
	RelativeLayout mMaleFollowersLayout;
	RelativeLayout mLoadMoreLayout;
	
	ImageView mProfileImageView;
	ImageView mCoverImageView;
	ImageView mCameraImageView;
	ImageView mFollowersImageView;
	ImageView mVoteImageView;
	ImageView mMaleFollowersImageView;
	
	TextView mSelfieTextView;
	TextView mFollowersTextView;
	TextView mSelfPostCountTextView;
	TextView mFollowersCountTextView;
	TextView mVotePostCountTextView;
	TextView mMaleFollowersCountTextView;
	TextView mMaleVoteTextView;
	TextView mMaleFollowerTextView;
	
	TextView mUserNameTextView;
	TextView mDescriptionTextView;
	TextView mSharesOfMonthCountTextView;
	TextView mSharesOfYearCountTextView;
	TextView mVotesOfMonthCountTextView;
	TextView mVotesOfYearCountTextView;
	TextView mMembersCountTextView;
	TextView mRankingTextView;
	TextView mYearTextView;
	TextView mMonthTextView;
		
	Button mFollowButton;
	Button mBackButton;
	public ImageButton mRefreshButton;
	public ProgressBar mRefreshingProgressBar;
	ListView mSelfPostListView;

	public ParseUser mUser;
	
	private int mActiveCategoryBtn;

	private boolean mRefreshingFollowing;
	private boolean mRefreshingPosts;
	private boolean mRefreshingFollowers;
	
	List<PostModel> mSelfPostsList = new ArrayList<PostModel>();
	List<PostModel> mVotePostsList = new ArrayList<PostModel>();
	ArrayList<UsersListModel> mFollowersModelList;
	
	ProfilePostsListCellAdapter mProfilePostsListCellAdapter;
	ProfileFollowersListCellAdapter mProfileFollowersListCellAdapter;
	ProfileMaleFollowersListCellAdapter mProfileMaleFollowersListCellAdapter;
	
	MainActivity mActivity;
	public int mYear;
	public int mMonth;
	private int mVotesOfYear = 0;
	private int mSharesOfYear = 0;
	private int mVotesOfMonth = 0;
	private int mSharesOfMonth = 0;
	int mFollowingStatus;
	int mAllPostPageNo;
	boolean mIsAllPostsLoaded;
	Calendar mCalendar;
	
	public static TextView mShareCountTextView;
	public static PostModel mSelectedPostObject;
	
	
	private final Observer shareNotification = new Observer()
	{
		public void update(Observable observable, Object data)
		{
			shareAction();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_profile, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();

		//init variables
		mCalendar = Calendar.getInstance();
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH);
		
		mSelfPostListView = (ListView) view.findViewById(R.id.lv_profile);
		View header = View.inflate(mActivity, R.layout.header_profile, null);
		mSelfPostListView.addHeaderView(header);
		
		mSelfPostListView.setAdapter(null);
		mSelfPostListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
		
		mFemaleInfoLayout = (LinearLayout) header.findViewById(R.id.layout_profile_female_info);
		mFemaleCategoryLayout = (LinearLayout) header.findViewById(R.id.layout_profile_female_category);
		mMaleCategoryLayout = (LinearLayout) header.findViewById(R.id.layout_profile_male_category);
		mMostActiveUserLayout = (LinearLayout) header.findViewById(R.id.layout_profile_most_active);
		
		mSelfPostLayout = (RelativeLayout) header.findViewById(R.id.layout_profile_self_post);
		mSelfPostLayout.setOnClickListener(this);
		mFollowersLayout = (RelativeLayout) header.findViewById(R.id.layout_profile_followers);
		mFollowersLayout.setOnClickListener(this);
		
		mVotedPostLayout = (RelativeLayout) header.findViewById(R.id.layout_profile_voted_post);
		mVotedPostLayout.setOnClickListener(this);
		mMaleFollowersLayout = (RelativeLayout) header.findViewById(R.id.layout_profile_male_followers);
		mMaleFollowersLayout.setOnClickListener(this);
		
		mCameraImageView = (ImageView) header.findViewById(R.id.iv_profile_camera);
		mFollowersImageView = (ImageView) header.findViewById(R.id.iv_profile_follower);
		mSelfieTextView = (TextView) header.findViewById(R.id.tv_profile_selfie);
		mFollowersTextView = (TextView) header.findViewById(R.id.tv_profile_followers);
		mSelfPostCountTextView = (TextView) header.findViewById(R.id.tv_profile_self_post_count);
		mFollowersCountTextView = (TextView) header.findViewById(R.id.tv_profile_followers_count);
		
		mVoteImageView = (ImageView) header.findViewById(R.id.iv_profile_male_vote);
		mMaleFollowersImageView = (ImageView) header.findViewById(R.id.iv_profile_male_follower);
		mMaleVoteTextView = (TextView) header.findViewById(R.id.tv_profile_male_vote);
		mMaleFollowerTextView = (TextView) header.findViewById(R.id.tv_profile_male_follower);
		mVotePostCountTextView = (TextView) header.findViewById(R.id.tv_profile_male_vote_count);
		mMaleFollowersCountTextView = (TextView) header.findViewById(R.id.tv_profile_male_followers_count);
		
		mUserNameTextView = (TextView) header.findViewById(R.id.tv_profile_username);
		mDescriptionTextView = (TextView) header.findViewById(R.id.tv_profile_description);
		mProfileImageView = (ImageView) header.findViewById(R.id.iv_profile_picture);
		mCoverImageView = (ImageView) header.findViewById(R.id.iv_header_bg);
		
		mSharesOfMonthCountTextView = (TextView)header.findViewById(R.id.tv_profile_photos_count);
		mSharesOfYearCountTextView = (TextView)header.findViewById(R.id.tv_profile_photos_year_count);
		mVotesOfMonthCountTextView = (TextView)header.findViewById(R.id.tv_profile_votes_count);
		mVotesOfYearCountTextView = (TextView)header.findViewById(R.id.tv_profile_votes_year_count);
		mMembersCountTextView = (TextView)header.findViewById(R.id.tv_profile_member_count);
		mRankingTextView = (TextView)header.findViewById(R.id.tv_profile_ranking);
		mYearTextView = (TextView) header.findViewById(R.id.tv_profile_year);
		mMonthTextView = (TextView) header.findViewById(R.id.tv_profile_month);
		
		
		mFollowButton = (Button) header.findViewById(R.id.btn_profile_follow);
		mFollowButton.setOnClickListener(this);
		
		mRefreshingProgressBar = (ProgressBar) mActivity.findViewById(R.id.progressbar_refresh);
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		mRefreshButton.setVisibility(View.INVISIBLE);
		mBackButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
						
		mUserNameTextView.setText(mUser.getUsername());
		mBackButton.setText(mUser.getUsername());
		
		//display profile image
		TheMissApplication.getInstance().displayUserProfileImage(mUser, mProfileImageView);
			
		//display cover image
		TheMissApplication.getInstance().displayUserCoverImage(mUser, mCoverImageView);
		
		if(mUser.getString("description") != null)
			mDescriptionTextView.setText(mUser.getString("description"));
		
		displayFollowStatus();
		if(mUser.getString("gender") != null && mUser.getString("gender").equalsIgnoreCase("male")){
			displayVotedPosts();
			mMaleCategoryLayout.setVisibility(View.VISIBLE);
			mFemaleCategoryLayout.setVisibility(View.GONE);
			mFemaleInfoLayout.setVisibility(View.GONE);
			
		}else{
			
			displaySelfPosts();
			mMaleCategoryLayout.setVisibility(View.GONE);
			mFemaleCategoryLayout.setVisibility(View.VISIBLE);
			mFemaleInfoLayout.setVisibility(View.VISIBLE);
		}
		 
		NotificationService.getInstance().addObserver(Constants.NOTIFICATION_SHARE_PROFILE_SUCCESS, shareNotification);
		view.setOnClickListener(null);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
				
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.INVISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.INVISIBLE);
				
		mBackButton.setOnClickListener(this);
		if(mUser != null)
			mBackButton.setText(mUser.getUsername());
		mBackButton.setVisibility(View.VISIBLE);
		
		mRefreshButton.setOnClickListener(this);

//		if(mRefreshingUserInformation == true || mRefreshingFollowers == true || mRefreshingPosts == true){
//			mRefreshButton.setVisibility(View.INVISIBLE);
//			mRefreshingProgressBar.setVisibility(View.VISIBLE);
//		}else{
//			mRefreshButton.setVisibility(View.VISIBLE);
//			mRefreshingProgressBar.setVisibility(View.INVISIBLE);
//		}
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		NotificationService.getInstance().removeObserver(Constants.NOTIFICATION_SHARE_PROFILE_SUCCESS, shareNotification);
		super.onDestroyView();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch(v.getId()){
			case R.id.layout_profile_self_post:
				if(mActiveCategoryBtn != ACTIVE_PROFILE_POST)
					displaySelfPosts();
				break;
			
			case R.id.layout_profile_voted_post:
				if(mActiveCategoryBtn != ACTIVE_PROFILE_POST)
					displayVotedPosts();
				break;
				
			case R.id.layout_profile_followers:
				if(mActiveCategoryBtn != ACTIVE_PROFILE_FOLLOWERS)
					displayFollowers();
				break;
				
			case R.id.layout_profile_male_followers:
				if(mActiveCategoryBtn != ACTIVE_PROFILE_FOLLOWERS)
					displayMaleFollowers();
				break;
				
			case R.id.ib_menu_refresh:
				
				if(mActiveCategoryBtn == ACTIVE_PROFILE_POST){
					if(mRefreshingPosts == false){
						mIsAllPostsLoaded = false;
						mAllPostPageNo = 0;
						
						if(AppManager.isFemale(mUser) == false){
							mVotePostsList.clear();
							fetchVotedPosts();
							
						}else{
							mSelfPostsList.clear();
							refreshSelfPosts();
						}
					}
				}else if(mActiveCategoryBtn == ACTIVE_PROFILE_FOLLOWERS){
					if(mRefreshingFollowers == false){
						if(AppManager.isFemale(mUser) == false){
							refreshMaleFollowers();
						}else{
							refreshFollowers();
						}
					}
				}
				break;
				
			case R.id.btn_profile_follow:
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
				if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==false){
					Toast.makeText(mActivity, "You must login first.", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(mFollowingStatus == FOLLOW_ME_STATUS)
					addFollower();
				else if(mFollowingStatus == FOLLOWING_STATUS)
					removeFollower();
				else if(mFollowingStatus == EDIT_STATUS){
					Intent intent = new Intent(mActivity, SettingsActivity.class);
					startActivity(intent);
				}
					
				break;
								
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
				
		}
	}
	
//	private DatePickerDialog createDialogWithoutDateField(){
//
//		Calendar c = Calendar.getInstance();
//	    DatePickerDialog dpd = new DatePickerDialog(mActivity, new DateSetListener(), mYear, mMonth, c.get(Calendar.DAY_OF_MONTH));
//	    try{
//		    java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
//		    for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) { 
//		        if (datePickerDialogField.getName().equals("mDatePicker")) {
//		            datePickerDialogField.setAccessible(true);
//		            DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
//		            java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
//		            for (java.lang.reflect.Field datePickerField : datePickerFields) {
//		            	Log.i("test", datePickerField.getName());
//		                if ("mDaySpinner".equals(datePickerField.getName())) {
//		                	datePickerField.setAccessible(true);
//		                	Object dayPicker = new Object();
//		                	dayPicker = datePickerField.get(datePicker);
//		                	((View) dayPicker).setVisibility(View.GONE);
//		                }
//		            }
//		        }
//	
//		    }
//	    }catch(Exception ex){
//	    }
//	    return dpd;
//
//	}
//	
//	class DateSetListener implements DatePickerDialog.OnDateSetListener {
//
//		@Override
//	    public void onDateSet(DatePicker view, int year, int monthOfYear,
//	            int dayOfMonth) {
//	        // TODO Auto-generated method stub
//	        // getCalender();
//	       
//	    	mCalendar.set(year, monthOfYear, dayOfMonth);
//	        mYear = year;
//	        mMonth = monthOfYear;
//	        refreshSelfPosts();
//	    }
//	}
	
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
	
	public void shareAction(){
		if(AppManager.mIsAlreadyShared == false){
			mShareCountTextView.setText(String.valueOf(mSelectedPostObject.getShareCount()+1));
			mSelectedPostObject.put("shareCount", mSelectedPostObject.getShareCount()+1);
			mSelectedPostObject.saveEventually();
			
			mActivity.saveShareCount(ParseUser.getCurrentUser(), mSelectedPostObject.getUser());
			
			mActivity.sendNotification(mSelectedPostObject, Constants.NOTIFICATION_KIND_SHARE);
			AppManager.mIsAlreadyShared = true;
		}
	}
			
	public void displaySelfPostsByDate(int year, int month, int day){
		mIsAllPostsLoaded = false;
		mAllPostPageNo = 0;
		mSelfPostsList.clear();
    	mCalendar.set(year, month, day);
        mYear = year;
        mMonth = month;
        refreshSelfPosts();	
	}

	public void displaySelfPosts(){
				
		mSelfPostLayout.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mCameraImageView.setBackgroundResource(R.drawable.ic_profile_white_camera);
		mSelfieTextView.setTextColor(Color.WHITE);
		mSelfPostCountTextView.setTextColor(Color.WHITE);
		
		mFollowersLayout.setBackgroundColor(Color.WHITE);
		mFollowersImageView.setBackgroundResource(R.drawable.ic_profile_red_follower);
		mFollowersTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		mFollowersCountTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		
		mMostActiveUserLayout.setVisibility(View.GONE);
		
		mActiveCategoryBtn = ACTIVE_PROFILE_POST;
		
		if(mSelfPostsList != null && mSelfPostsList.size()>0){
			mProfilePostsListCellAdapter = new ProfilePostsListCellAdapter(ProfileFragment.this, mSelfPostsList, mUser);
			mSelfPostListView.setAdapter(mProfilePostsListCellAdapter);
			
		}else
			refreshSelfPosts();
	}
	
	public void displayVotedPosts(){
		mVotedPostLayout.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mVoteImageView.setBackgroundResource(R.drawable.home_vote_white_btn);
		mVotePostCountTextView.setTextColor(Color.WHITE);
		mMaleVoteTextView.setTextColor(Color.WHITE);
		
		mMaleFollowersLayout.setBackgroundColor(Color.WHITE);
		mMaleFollowersImageView.setBackgroundResource(R.drawable.ic_profile_red_follower);
		mMaleFollowersCountTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		mMaleFollowerTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		
		mMostActiveUserLayout.setVisibility(View.GONE);
		
		mActiveCategoryBtn = ACTIVE_PROFILE_POST;
		
		if(mVotePostsList != null && mVotePostsList.size()>0){
			mProfilePostsListCellAdapter = new ProfilePostsListCellAdapter(ProfileFragment.this, mVotePostsList, mUser);
			mSelfPostListView.setAdapter(mProfilePostsListCellAdapter);

		}else
			fetchVotedPosts();
	}

	public void displayFollowers(){		
		mSelfPostLayout.setBackgroundColor(Color.WHITE);
		mCameraImageView.setBackgroundResource(R.drawable.ic_profile_red_camera);
		mSelfieTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		mSelfPostCountTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		
		mFollowersLayout.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mFollowersImageView.setBackgroundResource(R.drawable.ic_profile_white_follower);
		mFollowersTextView.setTextColor(Color.WHITE);
		mFollowersCountTextView.setTextColor(Color.WHITE);
		mActiveCategoryBtn = ACTIVE_PROFILE_FOLLOWERS;
		
		mMostActiveUserLayout.setVisibility(View.VISIBLE);
		
		if(mFollowersModelList != null && mFollowersModelList.size()>0){
			mProfileFollowersListCellAdapter = new ProfileFollowersListCellAdapter(ProfileFragment.this, mFollowersModelList);
			mSelfPostListView.setAdapter(mProfileFollowersListCellAdapter);

		}else{
			mSelfPostListView.setAdapter(null);
			refreshFollowers();
		}
	}
	
	public void displayMaleFollowers(){

		mVotedPostLayout.setBackgroundColor(Color.WHITE);
		mVoteImageView.setBackgroundResource(R.drawable.home_unvote_btn);
		mVotePostCountTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		mMaleVoteTextView.setTextColor(getResources().getColor(R.color.home_red_color));
		
		mMaleFollowersLayout.setBackgroundColor(getResources().getColor(R.color.home_red_color));
		mMaleFollowersImageView.setBackgroundResource(R.drawable.ic_profile_white_follower);
		mMaleFollowersCountTextView.setTextColor(Color.WHITE);
		mMaleFollowerTextView.setTextColor(Color.WHITE);
		
		mMostActiveUserLayout.setVisibility(View.GONE);
		
		mActiveCategoryBtn = ACTIVE_PROFILE_FOLLOWERS;
		
		if(mFollowersModelList != null && mFollowersModelList.size()>0){
			mProfileMaleFollowersListCellAdapter = new ProfileMaleFollowersListCellAdapter(ProfileFragment.this, mFollowersModelList);
			mSelfPostListView.setAdapter(mProfileMaleFollowersListCellAdapter);
			
		}else{
			mSelfPostListView.setAdapter(null);
			refreshMaleFollowers();
		}
	}
	
	public void refreshSelfPosts(){
		if(mRefreshingPosts == true) return;
				
		if(mIsAllPostsLoaded){
			return;
		}
		
		mRefreshingPosts = true;
		
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().pause();
		System.gc();
		
		if(mAllPostPageNo == 0){
			calculateSelfPostCount();
			calculateSelfPostRanking();
	        calculateVotesAndSharesOfSelfPost(mYear, mMonth);
		}
		
		try{				
			//display current year
			mYearTextView.setText(String.valueOf(mYear));
			
			//display current month
			SimpleDateFormat dateFormat= new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
			mMonthTextView.setText(dateFormat.format(mCalendar.getTime()));
			
			//get votes array
            ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
            query.whereEqualTo("user", mUser);
            query.whereGreaterThanOrEqualTo("createdAt", UtilityMethods.getDate(mYear, mMonth, 1));
            query.whereLessThan("createdAt", UtilityMethods.getDate(mYear, mMonth+1, 1));
            query.setSkip(mAllPostPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
            query.include("user");
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<PostModel>(){

					@Override
					public void done(List<PostModel> list,
							ParseException err) {
						// TODO Auto-generated method stub
									
						ImageLoader.getInstance().resume();
						mRefreshingPosts = false;
						
						if(err != null){
							return;
						}
						
						if(getActivity() != null && list != null){
							if (list.size() < Constants.PARSE_POST_LIMIT_COUNT){
								mIsAllPostsLoaded = true;
			                }	
							
							mSelfPostsList.addAll(list);
									
								
							if(mActiveCategoryBtn == ACTIVE_PROFILE_POST){
								if(mProfilePostsListCellAdapter != null && mAllPostPageNo > 0){
									mProfilePostsListCellAdapter.notifyDataSetChanged();
								}else{
									mProfilePostsListCellAdapter = new ProfilePostsListCellAdapter(ProfileFragment.this, mSelfPostsList, mUser);
									mSelfPostListView.setAdapter(mProfilePostsListCellAdapter);
								}
							}
							
							mAllPostPageNo++;
						}
						
						TheMissApplication.getInstance().hideProgressDialog();
					}
             	
            });
            
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
	}
	
	public void fetchVotedPosts(){
		if(mRefreshingPosts == true) return;
		
		if(mIsAllPostsLoaded && mVotePostsList.size() > 0){
			return;
		}
		mRefreshingPosts = true;
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());

		//display vote count of male
		ParseQuery<PostModel> query1 = new ParseQuery<PostModel>("Post");
        query1.whereContainedIn("voteUsers", Arrays.asList(mUser.getObjectId()));
        query1.countInBackground(new CountCallback() {
			
			@Override
			public void done(int count, ParseException arg1) {
				// TODO Auto-generated method stub
				mVotePostCountTextView.setText(String.valueOf(count));
			}
		});
        		
        //display vote posts
		try{
			//get votes array
            ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
            query.whereNotContainedIn("dislikeUsers", Arrays.asList(mUser.getObjectId()));
            query.whereContainedIn("voteUsers", Arrays.asList(mUser.getObjectId()));
            query.include("user");
            query.setSkip(mAllPostPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<PostModel>(){

					@Override
					public void done(List<PostModel> postList,
							ParseException err) {
						// TODO Auto-generated method stub
						TheMissApplication.getInstance().hideProgressDialog();
						mRefreshingPosts = false;
						
						if(getActivity() != null && postList != null){
							if (postList.size() < Constants.PARSE_POST_LIMIT_COUNT){
								mIsAllPostsLoaded = true;
			                }	
							
							mVotePostsList.addAll(postList);
							
						
							if(mActiveCategoryBtn == ACTIVE_PROFILE_POST){
								if(mProfilePostsListCellAdapter != null && mAllPostPageNo > 0){
									mProfilePostsListCellAdapter.notifyDataSetChanged();
									
								}else{
									mProfilePostsListCellAdapter = new ProfilePostsListCellAdapter(ProfileFragment.this, mVotePostsList, mUser);
									mSelfPostListView.setAdapter(mProfilePostsListCellAdapter);
								}
							}
							
							mAllPostPageNo++;
						}						
					}
             	
             });       
	                    
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
	}
	
	public void refreshFollowers(){
		
		if(mRefreshingFollowers == true) return;
		
		mRefreshingFollowers = true;
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
//		mRefreshingProgressBar.setVisibility(View.VISIBLE);
//		mRefreshButton.setVisibility(View.INVISIBLE);
				
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
		query.whereEqualTo("toUser", mUser);
		query.include("fromUser");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> followerList, ParseException e) {
				// TODO Auto-generated method stub
				TheMissApplication.getInstance().hideProgressDialog();
				
				if(e==null  && getActivity() != null){
					mFollowersModelList = new ArrayList<UsersListModel>();
					for(ParseObject item : followerList){
						if(item.get("fromUser") != null){
						
							ParseUser user = (ParseUser)item.get("fromUser");
							UsersListModel model;
							model = new UsersListModel();
							model.setUser(user);
							model.setTotalActionCount(0);
							mFollowersModelList.add(model);
						}
					}
					
					//get members and ranking
			        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Share"); 
//			        query1.whereGreaterThanOrEqualTo("createdAt", Utils.getFirstDateOfCurrentMonth());
			        query1.whereEqualTo("toUser", mUser);
			        query1.include("fromUser");
			        query1.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
			        query1.findInBackground(new FindCallback<ParseObject>(){

							@Override
							public void done(List<ParseObject> list,
									ParseException err) {
								// TODO Auto-generated method stub
								if(err == null && getActivity() != null){
																		
									for(int i=0;i<list.size();i++){
										ParseUser fromUser = list.get(i).getParseUser("fromUser");
										if(fromUser == null) continue;
										
										Log.d("", fromUser.getUsername());
//										if(Utils.containUser(mFollowersList, user) == false) continue;
											
										
										int index = UtilityMethods.getIndexOfUser(mFollowersModelList, fromUser);
										if(index < 0){
											
										}else{
											UsersListModel model;
											model = mFollowersModelList.get(index);
											model.setTotalActionCount(model.getTotalActionCount() + list.get(i).getInt("shareCount"));
										}
									}
									
									//sort user list by action count
									Collections.sort(mFollowersModelList, new Comparator<UsersListModel>() {
								        @Override
								        public int compare(UsersListModel s1, UsersListModel s2) {
								            return s2.getTotalActionCount() - s1.getTotalActionCount();
								        }
								    });
									
									mFollowersCountTextView.setText(String.valueOf(mFollowersModelList.size()));
									
									if(mActiveCategoryBtn == ACTIVE_PROFILE_FOLLOWERS){
										mProfileFollowersListCellAdapter = new ProfileFollowersListCellAdapter(ProfileFragment.this, mFollowersModelList);
										mSelfPostListView.setAdapter(mProfileFollowersListCellAdapter);
									}
								}
								
								mRefreshingFollowers = false;
								mRefreshingProgressBar.setVisibility(View.INVISIBLE);
								mRefreshButton.setVisibility(View.VISIBLE);
							}
			         	
			         });       
					
				}
			}
			
		});		
	}

	public void refreshMaleFollowers(){
			
		if(mRefreshingFollowers == true) return;
		
		mRefreshingFollowers = true;
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
//		mRefreshingProgressBar.setVisibility(View.VISIBLE);
//		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
		query.whereEqualTo("fromUser", mUser);
		query.include("toUser");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> followerList, ParseException e) {
				// TODO Auto-generated method stub
				TheMissApplication.getInstance().hideProgressDialog();
				
				if(e==null  && getActivity() != null){
					mFollowersModelList = new ArrayList<UsersListModel>();
					for(ParseObject item : followerList){
						if(item.get("toUser") != null){
						
							ParseUser user = (ParseUser)item.get("toUser");
							UsersListModel model;
							model = new UsersListModel();
							model.setUser(user);
							model.setTotalActionCount(0);
							mFollowersModelList.add(model);
						}
					}
					
					mMaleFollowersCountTextView.setText(String.valueOf(mFollowersModelList.size()));
					
					if(mActiveCategoryBtn == ACTIVE_PROFILE_FOLLOWERS){
						mProfileMaleFollowersListCellAdapter = new ProfileMaleFollowersListCellAdapter(ProfileFragment.this, mFollowersModelList);
						mSelfPostListView.setAdapter(mProfileMaleFollowersListCellAdapter);
					}
				}
				
				mRefreshingFollowers = false;
				mRefreshingProgressBar.setVisibility(View.INVISIBLE);
				mRefreshButton.setVisibility(View.VISIBLE);
				
//					//get members and ranking
//			        ParseQuery<PostModel> query1 = new ParseQuery<PostModel>("Post"); 
//			        query1.whereGreaterThanOrEqualTo("createdAt", UtilityMethods.getFirstDateOfCurrentMonth());
////			        query1.orderByDescending("totalActionCount");
//			        query1.include("user");
//			        query1.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
//			        query1.findInBackground(new FindCallback<PostModel>(){
//
//							@Override
//							public void done(List<PostModel> list,
//									ParseException err) {
//								// TODO Auto-generated method stub
//								if(err == null && getActivity() != null){
//															
//									for(int i=0;i<list.size();i++){
//										ParseUser user = list.get(i).getUser();
//										if(user == null) continue;
//										
//										Log.d("", user.getUsername());										
//										int index = UtilityMethods.getIndexOfUser(mFollowersModelList, user);
//										if(index < 0){
//											
//										}else{
//											UsersListModel model;
//											model = mFollowersModelList.get(index);
//											model.setTotalActionCount(model.getTotalActionCount() + list.get(i).getVoteCount() +
//													list.get(i).getCommentCount() + list.get(i).getInt("shareCount"));
//										}
//									}
//									
//									//sort user list by action count
//									Collections.sort(mFollowersModelList, new Comparator<UsersListModel>() {
//								        @Override
//								        public int compare(UsersListModel s1, UsersListModel s2) {
//								            return s2.getTotalActionCount() - s1.getTotalActionCount();
//								        }
//								    });
//									
//									mMaleFollowersCountTextView.setText(String.valueOf(mFollowersModelList.size()));
//									
//									if(mActiveCategoryBtn == ACTIVE_PROFILE_FOLLOWERS){
//										mProfileMaleFollowersListCellAdapter = new ProfileMaleFollowersListCellAdapter(ProfileFragment.this, mFollowersModelList);
//										mSelfPostListView.setAdapter(mProfileMaleFollowersListCellAdapter);
//									}
//								}
//								
//								mRefreshingFollowers = false;
//								mRefreshingProgressBar.setVisibility(View.INVISIBLE);
//								mRefreshButton.setVisibility(View.VISIBLE);
//							}
//			         });       
//				}
			}
			
		});		
	}

	public void addFollower(){
		final ParseUser currentUser = ParseUser.getCurrentUser();
		if(mUser == null || currentUser == null) return;
		if(mRefreshingFollowing == true) return;
		
		mRefreshingFollowing = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseObject follower = new ParseObject("Follower");
		follower.put("fromUser", currentUser);
		follower.put("toUser", mUser);
		follower.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException arg0) {
				// TODO Auto-generated method stub
				
				if(arg0 == null && getActivity() != null){
					Toast.makeText(mActivity, ProfileFragment.this.getResources().getString(R.string.followed_successfully), Toast.LENGTH_SHORT).show();
					mFollowButton.setText(ProfileFragment.this.getResources().getString(R.string.following));
					mFollowButton.setTextColor(Color.WHITE);
					mFollowButton.setBackgroundResource(R.drawable.profile_following_bg);
					mFollowingStatus = FOLLOWING_STATUS;
					
					mRefreshingFollowing = false;
					refreshFollowers();
					
					mActivity.sendFollowingNotification(currentUser, mUser);
				}else{
					mRefreshingProgressBar.setVisibility(View.INVISIBLE);
					mRefreshButton.setVisibility(View.VISIBLE);
				}
			}
			
		});
	}
	
	public void removeFollower(){
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if(mUser == null || currentUser == null) return;
		if(mRefreshingFollowing == true) return;
		
		mRefreshingFollowing = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.whereEqualTo("fromUser", currentUser);
		query.whereEqualTo("toUser", mUser);
		
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> list, ParseException err) {
				// TODO Auto-generated method stub
				mFollowingStatus = FOLLOW_ME_STATUS;
				mFollowButton.setText(ProfileFragment.this.getResources().getString(R.string.follow_me));
				mFollowButton.setTextColor(ProfileFragment.this.getResources().getColor(R.color.home_red_color));
				mFollowButton.setBackgroundResource(R.drawable.red_border_btn_bg);
				
				for(ParseObject obj: list){
					obj.deleteInBackground(new DeleteCallback(){
	
						@Override
						public void done(ParseException arg0) {
							// TODO Auto-generated method stub
							mRefreshingFollowing = false;
							refreshFollowers();
						}
					});
				}
			}
			
		});
	}
	
	public void displayFollowStatus(){
		
		if(mUser.getObjectId().equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())){
			mFollowButton.setText(ProfileFragment.this.getResources().getString(R.string.edit));
			mFollowButton.setTextColor(ProfileFragment.this.getResources().getColor(R.color.home_red_color));
			mFollowButton.setBackgroundResource(R.drawable.red_border_btn_bg);
			mFollowingStatus = EDIT_STATUS;
			return;
		}
		
		if(mUser.getString("gender") != null && mUser.getString("gender").equalsIgnoreCase("male")){
			mFollowButton.setVisibility(View.GONE);
			return;
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
		query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
		query.whereEqualTo("toUser", mUser);
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> list, ParseException err) {
				// TODO Auto-generated method stub
				if(err == null && getActivity() != null){
					int count = list.size();
					if(count>0){
						mFollowButton.setText(ProfileFragment.this.getResources().getString(R.string.following));
						mFollowButton.setTextColor(Color.WHITE);
						mFollowButton.setBackgroundResource(R.drawable.profile_following_bg);
						mFollowingStatus = FOLLOWING_STATUS;
					}else{
						mFollowButton.setText(ProfileFragment.this.getResources().getString(R.string.follow_me));
						mFollowButton.setTextColor(ProfileFragment.this.getResources().getColor(R.color.home_red_color));
						mFollowButton.setBackgroundResource(R.drawable.red_border_btn_bg);
						mFollowingStatus = FOLLOW_ME_STATUS;
					}
				}
			}
			
		});
	}
	
	public void calculateVotesAndSharesOfSelfPost(int year, int month){
		
		mVotesOfYear = 0;
		mSharesOfYear = 0;
		mVotesOfMonth = 0;
		mSharesOfMonth = 0;
			
		final String strMonth = String.format("%04d_%02d", year, month+1);
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", mUser);
		query.whereContains("postMonth", String.valueOf(year));
		query.findInBackground(new FindCallback<UserInfoModel>() {

			@Override
			public void done(List<UserInfoModel> list, ParseException err) {
				// TODO Auto-generated method stub
				if(err != null) return;
				
				for(UserInfoModel model : list){
					mVotesOfYear += model.getVoteCount();
					mSharesOfYear += model.getShareCount();
					if(model.getPostMonth().equals(strMonth)){
						mVotesOfMonth = model.getVoteCount();
						mSharesOfMonth = model.getShareCount();
					}
				}
									
				mVotesOfMonthCountTextView.setText(String.valueOf(mVotesOfMonth));
				mVotesOfYearCountTextView.setText(String.valueOf(mVotesOfYear));
				mSharesOfMonthCountTextView.setText(String.valueOf(mSharesOfMonth));
				mSharesOfYearCountTextView.setText(String.valueOf(mSharesOfYear));
			}
			
		});
	}
			
	public void calculateSelfPostCount(){
		//display vote count of male
		ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
		query.whereEqualTo("user", mUser);
        query.whereGreaterThanOrEqualTo("createdAt", UtilityMethods.getDate(mYear, mMonth, 1));
        query.whereLessThan("createdAt", UtilityMethods.getDate(mYear, mMonth+1, 1));
        query.countInBackground(new CountCallback() {
			
			@Override
			public void done(int count, ParseException arg1) {
				// TODO Auto-generated method stub
				mSelfPostCountTextView.setText(String.valueOf(count));
			}
		});
	}
	
	public void calculateSelfPostRanking(){
		//get members and ranking
		final String strMonth = String.format("%04d_%02d", mYear, mMonth+1);
	    ParseQuery<UserInfoModel> query1 = new ParseQuery<UserInfoModel>("UserInfo"); 
        query1.whereEqualTo("postMonth", strMonth);
        query1.whereGreaterThan("voteCount", 0);
        query1.orderByDescending("voteCount");
        query1.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
        query1.include("user");
        query1.include("lastPost");
        query1.findInBackground(new FindCallback<UserInfoModel>() {

			@Override
			public void done(List<UserInfoModel> list, ParseException arg1) {
				// TODO Auto-generated method stub
				if(list == null) return;
				
				List<UserInfoModel> activeList = new ArrayList<UserInfoModel>();
				for(UserInfoModel userInfo : list){
					ParseUser user = userInfo.getUser();
					if(user != null && user.getBoolean("deactive") == false){
						activeList.add(userInfo);
					}
				}
				
				int index = UtilityMethods.getRankingofMonth(activeList, mUser);
            	mRankingTextView.setText(String.valueOf(index+1));
				mMembersCountTextView.setText(String.valueOf(activeList.size()));
			}
		}); 
	}
}
