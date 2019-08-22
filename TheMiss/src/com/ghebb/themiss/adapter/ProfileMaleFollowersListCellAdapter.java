package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.datamodel.UsersListModel;
import com.parse.ParseUser;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class ProfileMaleFollowersListCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    ProfileFragment mFragment;
    LayoutInflater mInflater;
    List<UsersListModel> mUsersList = new ArrayList<UsersListModel>();
   
    
    public ProfileMaleFollowersListCellAdapter(ProfileFragment fragment,
            List<UsersListModel> usersList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mUsersList = usersList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public class ViewHolder {
    	TextView userNameTextView;
		TextView voteTextView;
		ImageView profilePictureImageView;
		RelativeLayout layoutMaleFollower;
    }
 
    @Override
	public int getItemCount() {
        return mUsersList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mUsersList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
	protected View getItemView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_profile_male_followers, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_profile_male_followers_username);
            holder.voteTextView = (TextView) view.findViewById(R.id.tv_profile_male_followers_votes_count);
            holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_profile_male_followers_photo);
            holder.layoutMaleFollower = (RelativeLayout) view.findViewById(R.id.layout_profile_male_follower_item);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseUser user = mUsersList.get(position).getUser();
                
		//display username
        holder.userNameTextView.setText(user.getUsername());
        
        //display profile image.
        TheMissApplication.getInstance().displayUserProfileImage(user, holder.profilePictureImageView);
		    
        //display voted count
        holder.voteTextView.setText(String.valueOf(mUsersList.get(position).getTotalActionCount()));
              
        holder.layoutMaleFollower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ProfileFragment profileFragment = new ProfileFragment();
		    	profileFragment.mUser = user;
				MainActivity mainActivity = (MainActivity) mFragment.getActivity();
				mainActivity.addContent(profileFragment, ProfileFragment.TAG);
			}
		});
        
        return view;
    }
}