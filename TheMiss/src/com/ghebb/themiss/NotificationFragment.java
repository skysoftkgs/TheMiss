package com.ghebb.themiss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.NotificationListCellAdapter;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.datamodel.NotificationListModel;
import com.ghebb.themiss.datamodel.PostModel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class NotificationFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "NotificationFragment";

	MainActivity mActivity;
	List<NotificationListModel>  mNotificationList = new ArrayList<NotificationListModel>();;
	
	ListView mNotificationListView;
	ProgressBar mRefreshingProgressBar;
	ImageButton mRefreshButton;
	TextView mNotificationCountTextView;
	
	boolean mRefreshingNotification;
	boolean mIsAllNotificationsLoaded; 
	NotificationListCellAdapter mAdapter;
	int mNotificationPageNo = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_notification, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		mNotificationListView = (ListView) view.findViewById(R.id.lv_notification);
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		mNotificationCountTextView = (TextView)  getActivity().findViewById(R.id.tv_menu_notification_count);
		
		//refresh at startup
		refreshNotification();

		view.setOnClickListener(null);
		return view;
	}

	@SuppressWarnings("deprecation")
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
		backButton.setText(getResources().getString(R.string.notification_title));
		backButton.setVisibility(View.VISIBLE);
		
		mRefreshButton.setOnClickListener(this);
		
//		if(mRefreshingNotification == true){
//			mRefreshButton.setVisibility(View.INVISIBLE);
//			mRefreshingProgressBar.setVisibility(View.VISIBLE);
//		}else{
//			mRefreshButton.setVisibility(View.VISIBLE);
//			mRefreshingProgressBar.setVisibility(View.INVISIBLE);
//		}
		
		if(mActivity != null && mActivity.mMenuMessageButton !=null){
			mActivity.mMenuMessageButton.setAlpha(50);
			mActivity.mMenuMessageButton.setEnabled(false);
		}
		
		mNotificationCountTextView.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
		switch(v.getId()){
		case R.id.ib_menu_refresh:
			if(mRefreshingNotification == false){
				mIsAllNotificationsLoaded = false;
				mNotificationPageNo = 0;
				mNotificationList.clear();
				refreshNotification();
			}
			break;
			
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
		}
	}
	
	public void refreshNotification(){
		if(mRefreshingNotification == true) return;
		
		if(mIsAllNotificationsLoaded){
//			Toast.makeText(getActivity(), "No more data!", Toast.LENGTH_SHORT).show();
			return;
		}
		
//		ImageLoader.getInstance().clearMemoryCache();
//		ImageLoader.getInstance().pause();
//		System.gc();
		TheMissApplication.getInstance().showProgressFullScreenDialog(getActivity());
				
		mRefreshingNotification = true;
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if(currentUser.getBoolean("admin")==true){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("FlagedPicture");
			query.orderByDescending("createdAt");
			query.include("post");
			query.include("user");
			query.setSkip(mNotificationPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
			query.findInBackground(new FindCallback<ParseObject>(){
	
				@Override
				public void done(List<ParseObject> list, ParseException e) {
					// TODO Auto-generated method stub
					
					TheMissApplication.getInstance().hideProgressDialog();
//					ImageLoader.getInstance().resume();
					mRefreshingNotification = false;
					
					if(e != null){
						return;
					}
					
					if(getActivity() != null && list != null){
						if (list.size() < Constants.PARSE_POST_LIMIT_COUNT){
							mIsAllNotificationsLoaded = true;
		                }	
					
						for(ParseObject item: list){
							NotificationListModel model = new NotificationListModel();
							model.setFromUser((ParseUser)item.get("user"));
							model.setDisplayTime(item.getCreatedAt());
							model.setPost((PostModel) item.getParseObject("post"));
							if(item.getBoolean("new") == true){
								item.put("new", false);
								item.saveEventually();
							}
							if(item.get("user") != null)
								mNotificationList.add(model);
						}
						
						if(mAdapter != null && mNotificationPageNo > 0){
							mAdapter.notifyDataSetChanged();
						}else{
							mAdapter = new NotificationListCellAdapter(NotificationFragment.this, mNotificationList);
							mNotificationListView.setAdapter(mAdapter);
						}
						
						mNotificationPageNo++;
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
			
//			ParseQuery<ParseObject> query3 = ParseQuery.getQuery("Notification");
//			query3.whereNotEqualTo("toUser", ParseUser.getCurrentUser());
//			query3.whereEqualTo("kind", Constants.NOTIFICATION_KIND_NEW_POST);
			
			List <ParseQuery<ParseObject>> query0 = new ArrayList<ParseQuery<ParseObject>>();
			query0.add(query1);
			query0.add(query2);
			query0.add(query3);
			
//			ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Follower");
//			innerQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
			ParseQuery<ParseObject> query = ParseQuery.or(query0);
			query.orderByDescending("createdAt");
			query.include("fromUser");
			query.include("toUser");
			query.include("post");
			query.setSkip(mNotificationPageNo * Constants.PARSE_POST_LIMIT_COUNT);
            query.setLimit(Constants.PARSE_POST_LIMIT_COUNT);
//			query.whereMatchesKeyInQuery("fromUser", "toUser", innerQuery);
			query.findInBackground(new FindCallback<ParseObject>(){
	
				@Override
				public void done(List<ParseObject> list, ParseException e) {
					// TODO Auto-generated method stub
					TheMissApplication.getInstance().hideProgressDialog();
//					ImageLoader.getInstance().resume();
					mRefreshingNotification = false;
					
					if(e != null){
						return;
					}
					
					if(getActivity() != null && list != null){
						if (list.size() < Constants.PARSE_POST_LIMIT_COUNT){
							mIsAllNotificationsLoaded = true;
		                }	
						
						for(ParseObject item: list){
							NotificationListModel model = new NotificationListModel();
							model.setFromUser((ParseUser)item.get("fromUser"));
							model.setToUser((ParseUser)item.get("toUser"));
							model.setDisplayTime(item.getCreatedAt());
							model.setPost((PostModel) item.getParseObject("post"));
							model.setKind(item.getString("kind"));
							if(item.getBoolean("new") == true){
								item.put("new", false);
								item.saveEventually();
							}
							mNotificationList.add(model);
						}
						
						if(mAdapter != null && mNotificationPageNo > 0){
							mAdapter.notifyDataSetChanged();
						}else{
							mAdapter = new NotificationListCellAdapter(NotificationFragment.this, mNotificationList);
							mNotificationListView.setAdapter(mAdapter);
						}
						
						mNotificationPageNo++;
						
						
					}
				}
				
			});
		}
	}
}
