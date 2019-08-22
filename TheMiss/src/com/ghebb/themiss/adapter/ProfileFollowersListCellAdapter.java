package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.datamodel.UsersListModel;
import com.parse.ParseUser;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class ProfileFollowersListCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    ProfileFragment mFragment;
    LayoutInflater mInflater;
    List<UsersListModel> mUsersList = new ArrayList<UsersListModel>();
   
    
    public ProfileFollowersListCellAdapter(ProfileFragment fragment,
            List<UsersListModel> usersList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mUsersList = usersList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public class ViewHolder {
    	TextView rankingTextView;
    	TextView userNameTextView;
		TextView voteTextView;
		ImageView profilePictureImageView;
		LinearLayout layoutFollower;
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
            view = mInflater.inflate(R.layout.item_profile_followers, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_profile_followers_username);
            holder.voteTextView = (TextView) view.findViewById(R.id.tv_profile_followers_votes_count);
            holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_profile_followers_photo);
			holder.rankingTextView = (TextView) view.findViewById(R.id.tv_profile_followers_no);
			holder.layoutFollower = (LinearLayout) view.findViewById(R.id.layout_profile_female_follower_item);
			
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseUser user = mUsersList.get(position).getUser();
                
		//display username
        if(user.getString("gender") != null && user.getString("gender").equalsIgnoreCase("male"))
        	holder.userNameTextView.setTextColor(Color.BLACK);
        else
        	holder.userNameTextView.setTextColor(mFragment.getResources().getColor(R.color.home_red_color));
        
        holder.userNameTextView.setText(user.getUsername());
        
        //display profile image.
        TheMissApplication.getInstance().displayUserProfileImage(user, holder.profilePictureImageView);
		    
        //display voted count
        holder.voteTextView.setText(String.valueOf(mUsersList.get(position).getTotalActionCount()));
        
        //display ranking No
        holder.rankingTextView.setText(String.valueOf(position+1));
             
        holder.layoutFollower.setOnClickListener(new OnClickListener() {
			
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