package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.gab.themiss.R;
import com.ghebb.themiss.HomeFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class HomeGridMissOfMonthCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    List<UserInfoModel> mMissOfMonthList = new ArrayList<UserInfoModel>();
    int mImageThumbSize;
    
    public HomeGridMissOfMonthCellAdapter(HomeFragment fragment,
            List<UserInfoModel> missOfMonthList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mMissOfMonthList = missOfMonthList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mImageThumbSize = (AppManager.mScreenWidth-4)/3;
    }
 
    public static class ViewHolder {
		ImageView postImageView;
		ProgressBar progressBar;
    }
 
    @Override
	public int getItemCount() {
        return mMissOfMonthList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mMissOfMonthList.get(position);
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
            view = mInflater.inflate(R.layout.item_home_postgrid, null);
  			holder.postImageView = (ImageView) view.findViewById(R.id.iv_content);
  			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
  			
  			ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
  	        imageViewParams.width = mImageThumbSize;
  	        imageViewParams.height = mImageThumbSize;
  	        holder.postImageView.setLayoutParams(imageViewParams);
  	        
            view.setTag(holder);
            
        } else {
            holder = (ViewHolder) view.getTag();
        }

        PostModel post = mMissOfMonthList.get(position).getLastPost();
        displayPost(holder, post);        
        	
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ProfileFragment profileFragment = new ProfileFragment();
				profileFragment.mUser = mMissOfMonthList.get(position).getUser();
				MainActivity mainActivity = (MainActivity) mFragment.getActivity();
				mainActivity.addContent(profileFragment, ProfileFragment.TAG);
			}
        	
        });
	        
        if(position == mMissOfMonthList.size() - 1){
        	mFragment.fetchMissOfMonth();
        }
        
        return view;
    }
    
    public void displayPost(final ViewHolder holder, PostModel post){
    	
    	if(post == null){
    		holder.postImageView.setImageBitmap(null);
    		
    	}else{
    		String thumbnailUrl;
        	if(post.getThumbnailImageFile() != null){
            	thumbnailUrl = post.getThumbnailImageFile().getUrl();
            }else{
            	thumbnailUrl= post.getPhotoFile().getUrl();
            }
        	
        	// display post image
            ImageLoader.getInstance().displayImage(thumbnailUrl, holder.postImageView, TheMissApplication.getInstance().options, new SimpleImageLoadingListener() {
    			 @Override
    			 public void onLoadingStarted(String imageUri, View view) {
    				 holder.progressBar.setProgress(0);
    				 holder.progressBar.setVisibility(View.VISIBLE);
    			 }

    			 @Override
    			 public void onLoadingFailed(String imageUri, View view,
    					 FailReason failReason) {
    				 holder.progressBar.setVisibility(View.GONE);
    			 }

    			 @Override
    			 public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    				 holder.progressBar.setVisibility(View.GONE);
    			 }
    		 }, new ImageLoadingProgressListener() {
    			 @Override
    			 public void onProgressUpdate(String imageUri, View view, int current,
    					 int total) {
    				 holder.progressBar.setProgress(Math.round(100.0f * current / total));
    			 }
    		});
    	}
    }
}