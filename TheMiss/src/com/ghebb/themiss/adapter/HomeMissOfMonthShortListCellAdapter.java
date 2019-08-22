package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.HomeFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.custom.RoundedImageView;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.parse.ParseUser;
 
public class HomeMissOfMonthShortListCellAdapter extends BaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    public List<UserInfoModel> mUserInfosList = new ArrayList<UserInfoModel>();
    ParseUser mCurrentUser;
       
    public HomeMissOfMonthShortListCellAdapter(HomeFragment fragment,
            List<UserInfoModel> userInfosList) {
        mFragment = fragment;
        mUserInfosList = userInfosList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mCurrentUser = ParseUser.getCurrentUser();
    }
 
    public class ViewHolder {
    	TextView noTextView;
    	TextView userNameTextView;
		TextView voteTextView;
		RoundedImageView avatarImageView;
    }
 
    @Override
	public int getCount() {
        return mUserInfosList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mUserInfosList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
    	
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_home_missofmonth_shortlist, null);
            holder.noTextView = (TextView) view.findViewById(R.id.textView_no);
            holder.userNameTextView = (TextView) view.findViewById(R.id.textView_name);
            holder.voteTextView = (TextView) view.findViewById(R.id.textView_voteCount);
			holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.imageView_avatar);
			view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final UserInfoModel userInfo = mUserInfosList.get(position);
		final ParseUser user = userInfo.getUser();
		
		holder.noTextView.setText(String.valueOf(position + 1));
		
		if(user != null){
			//display username
	        holder.userNameTextView.setText(user.getUsername());
	        
	        //display profile image
	        TheMissApplication.getInstance().displayUserProfileImage(user, holder.avatarImageView);
		}
		  
        //display vote count
        holder.voteTextView.setText(String.valueOf(userInfo.getVoteCount()));
        
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
        	
        });
        
        if(position == mUserInfosList.size() - 1){
        	mFragment.fetchMissOfMonth();
        }
        
        return view;
    }

    public void showProfile(int position){
    	ProfileFragment profileFragment = new ProfileFragment();
    	profileFragment.mUser = mUserInfosList.get(position).getUser();
		MainActivity mainActivity = (MainActivity) mFragment.getActivity();
		mainActivity.addContent(profileFragment, ProfileFragment.TAG);
    }	
}