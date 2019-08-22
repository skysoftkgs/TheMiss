package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.MenuFragment;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.UtilityMethods;
import com.parse.ParseUser;
 
public class UsersListCellAdapter extends BaseAdapter {
 
    // Declare Variables
    MenuFragment mFragment;
    LayoutInflater mInflater;
    List<ParseUser> mUsersList = new ArrayList<ParseUser>();
    
    
    public UsersListCellAdapter(MenuFragment fragment,
            List<ParseUser> usersList) {
        mFragment = fragment;
        mUsersList = usersList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public static class ViewHolder {
      	TextView userNameTextView;
		ImageView profilePictureImageView;
    }
 
    @Override
	public int getCount() {
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
 
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_menu_users, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_menu_users_username);            
            holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_menu_users_photo);			
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseUser user = mUsersList.get(position);
                
		//display username
        holder.userNameTextView.setText(user.getUsername());
        if(user.getString("gender") != null && user.getString("gender").equalsIgnoreCase("male"))
        	holder.userNameTextView.setTextColor(Color.BLACK);
        else
        	holder.userNameTextView.setTextColor(mFragment.getResources().getColor(R.color.home_red_color));
        
        //display profile image.
        TheMissApplication.getInstance().displayUserProfileImage(user, holder.profilePictureImageView);
               
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				UtilityMethods.hideSoftInput(mFragment.getActivity(), mFragment.mSearchEditText);
				showProfile(position);
			}
        	
        });
                
        return view;
    }
    
    public void showProfile(int position){
    	MainActivity mActivity = (MainActivity) mFragment.getActivity();
    	mActivity.toggle();
		ProfileFragment profileFragment = new ProfileFragment();
		profileFragment.mUser = mUsersList.get(position);
		mActivity.addContent(profileFragment, ProfileFragment.TAG);
    }
}