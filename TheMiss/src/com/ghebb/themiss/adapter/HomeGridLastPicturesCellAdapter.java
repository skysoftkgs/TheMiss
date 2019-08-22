package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

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
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class HomeGridLastPicturesCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    List<PostModel> mPostsList = new ArrayList<PostModel>();
    int mImageThumbSize;
    
    public HomeGridLastPicturesCellAdapter(HomeFragment fragment,
            List<PostModel> postsList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mPostsList = postsList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mImageThumbSize = (AppManager.mScreenWidth-4)/3;
    }
 
    public static class ViewHolder {
		ImageView postImageView;
		ProgressBar progressBar;
    }
 
    @Override
	public int getItemCount() {
        return mPostsList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mPostsList.get(position);
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

        String thumbnailUrl;
        if(mPostsList.get(position).getThumbnailImageFile() != null){
        	thumbnailUrl = mPostsList.get(position).getThumbnailImageFile().getUrl();
        }else{
        	thumbnailUrl= mPostsList.get(position).getPhotoFile().getUrl();
        }
        	
        // display post image
        TheMissApplication.getInstance().displayImage(thumbnailUrl, holder.postImageView, holder.progressBar);
        
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ProfileFragment profileFragment = new ProfileFragment();
				profileFragment.mUser = mPostsList.get(position).getUser();
				MainActivity mainActivity = (MainActivity) mFragment.getActivity();
				mainActivity.addContent(profileFragment, ProfileFragment.TAG);
			}
        	
        });
	        
        if(position == mPostsList.size() - 1){
        	mFragment.fetchLastPictures();
        }
        
        return view;
    }
}