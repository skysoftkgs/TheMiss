package com.ghebb.themiss.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.HomeFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.custom.RoundedImageView;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.parse.ParseUser;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class WinnerListCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    public List<UserInfoModel> mWinnerList = new ArrayList<UserInfoModel>();
    ParseUser mCurrentUser;

    public WinnerListCellAdapter(HomeFragment fragment,
            List<UserInfoModel> winnerList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mWinnerList = winnerList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mCurrentUser = ParseUser.getCurrentUser();
    }
 
    public static class ViewHolder {
    	TextView monthTextView;
    	TextView totalActionTextView;
    	TextView userNameTextView;
		TextView voteTextView;
		TextView shareTextView;
		ImageView postImageView;
		RoundedImageView profilePictureImageView;
		ProgressBar progressBar;
    }
 
    @Override
	public int getItemCount() {
        return mWinnerList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mWinnerList.get(position);
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
            view = mInflater.inflate(R.layout.item_winner, null);
            holder.monthTextView = (TextView) view.findViewById(R.id.tv_winner_post_month);
            holder.totalActionTextView = (TextView) view.findViewById(R.id.tv_home_winner_total_action);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_home_winner_username);
            holder.voteTextView = (TextView) view.findViewById(R.id.tv_home_winner_votecount);
            holder.shareTextView = (TextView) view.findViewById(R.id.tv_home_winner_sharecount);
			holder.postImageView = (ImageView) view.findViewById(R.id.iv_home_winner_post);
			holder.profilePictureImageView = (RoundedImageView) view.findViewById(R.id.iv_home_winner_profile);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			// Set the imageview height to be same as width
	        ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
	        imageViewParams.height = AppManager.mScreenWidth;
	        holder.postImageView.setLayoutParams(imageViewParams);
	        
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        UserInfoModel userInfo = mWinnerList.get(position);
        final PostModel post = userInfo.getLastPost();        
        displayPost(holder, post);        
                
        //display prev month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - position - 1);
		SimpleDateFormat dateFormat= new SimpleDateFormat("MMM yyyy", Locale.US);
		holder.monthTextView.setText("Miss " + dateFormat.format(calendar.getTime()));
		
        //display vote count
    	if(post == null){
    		holder.voteTextView.setText("");
    	}else{
    		holder.voteTextView.setText(String.valueOf(userInfo.getVoteCount()));
    	}
    	
    	//display shared count
    	if(post == null){
    		holder.shareTextView.setText("");
    	}else{
    		holder.shareTextView.setText(String.valueOf(userInfo.getShareCount()));
    	}
    	
        final ParseUser postUser = userInfo.getUser();
		if(postUser != null){
			//display username
	        holder.userNameTextView.setText(postUser.getUsername());
	        
	        //display profile image
	        TheMissApplication.getInstance().displayUserProfileImage(postUser, holder.profilePictureImageView);        
		}
                       
        //display total action count
        holder.totalActionTextView.setText(String.valueOf(userInfo.getVoteCount() + userInfo.getShareCount()));
        
        holder.postImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
		});
        
        holder.userNameTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
		});

        holder.profilePictureImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
		});
        return view;
    }

    public void displayPost(final ViewHolder holder, PostModel post){
    	if(post == null){
    		holder.postImageView.setImageBitmap(null);
    		
    	}else{
    	    TheMissApplication.getInstance().displayImage(post.getPhotoFile().getUrl(), 
	    			holder.postImageView, holder.progressBar);
    	}
    }
    
    public void showProfile(int position){
    	ProfileFragment profileFragment = new ProfileFragment();
    	profileFragment.mUser = mWinnerList.get(position).getUser();
		MainActivity mainActivity = (MainActivity) mFragment.getActivity();
		mainActivity.addContent(profileFragment, ProfileFragment.TAG);
    }
}