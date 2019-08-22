package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.CommentFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.NotificationFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.datamodel.NotificationListModel;
import com.parse.ParseUser;
 
public class NotificationListCellAdapter extends BaseAdapter {
 
	// Declare Variables
    NotificationFragment mFragment;
    LayoutInflater mInflater;
    List<NotificationListModel> mNotificationsList = new ArrayList<NotificationListModel>();
    ParseUser mCurrentUser;
    
    public NotificationListCellAdapter(NotificationFragment fragment,
            List<NotificationListModel> notificationsList) {
        mFragment = fragment;
        mNotificationsList = notificationsList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mCurrentUser = ParseUser.getCurrentUser();
    }
 
    public class ViewHolder {
    	TextView userNameTextView;
    	TextView timeTextView;
    	TextView hasVotedTextView;
    	TextView photoTextView;
		ImageView profilePictureImageView;
    }
 
    @Override
    public int getCount() {
        return mNotificationsList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mNotificationsList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_notification, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_notification_item_username);
            holder.timeTextView = (TextView) view.findViewById(R.id.tv_notification_item_time);
            holder.hasVotedTextView = (TextView) view.findViewById(R.id.tv_notification_has_voted);
            holder.photoTextView = (TextView) view.findViewById(R.id.tv_notification_photo);
            holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_notification_item_profile);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final NotificationListModel item = mNotificationsList.get(position);
                
		//display username
        if(item.getFromUser() != null)
        	holder.userNameTextView.setText(item.getFromUser().getUsername());

        //display time
        holder.timeTextView.setText(item.getDisplayTime());
        
        //display profile image.
        if(item.getFromUser() != null){
        	TheMissApplication.getInstance().displayUserProfileImage(item.getFromUser(), holder.profilePictureImageView);
        }
        
		if(mCurrentUser.getBoolean("admin") == true){
			holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.flagged));
		}else{
			String kind = item.getKind();
			if(kind == null) return view;
			if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_VOTE))
				holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.has_voted_one));
			else if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_SHARE))
				holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.has_shared_one));
			else if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_COMMENT)){
				if(item.getFromUser().getObjectId().equals(item.getToUser().getObjectId())){
					holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.has_commented_own));
				}else{
					holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.has_commented_one));
				}
			}
			else if(kind.equalsIgnoreCase(Constants.NOTIFICATION_KIND_NEW_POST))
				holder.hasVotedTextView.setText(mFragment.getResources().getString(R.string.posted_new));
		}
		
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goCommentPage(position);
			}
		});
		
		if(position == mNotificationsList.size() - 1){
        	mFragment.refreshNotification();
        }
		
        return view;
    }
    
    public void goCommentPage(int pos){
    	CommentFragment commentFragment = new CommentFragment();
		commentFragment.mPost = mNotificationsList.get(pos).getPost();
		if(mCurrentUser.getBoolean("admin") == true){
			commentFragment.mUser = mNotificationsList.get(pos).getFromUser();
		}else{
			commentFragment.mUser = mNotificationsList.get(pos).getToUser();
		}
		MainActivity mainActivity = (MainActivity) mFragment.getActivity();
		mainActivity.addContent(commentFragment, CommentFragment.TAG);
    }
}