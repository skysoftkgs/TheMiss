package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.gab.themiss.R;
import com.ghebb.themiss.InstagramPhotoPickerActivity;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
 
public class SelectPhotoCellAdapter extends BaseAdapter {
 
    // Declare Variables
    InstagramPhotoPickerActivity mActivity;
    LayoutInflater mInflater;
    List<String> mPhotosList = new ArrayList<String>();
    
    public SelectPhotoCellAdapter(InstagramPhotoPickerActivity activity,
            List<String> photosList) {
        mActivity = activity;
        mPhotosList = photosList;
        mInflater = LayoutInflater.from(mActivity);
    }
 
    public class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
    }
 
    @Override
    public int getCount() {
        return mPhotosList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mPhotosList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_home_postgrid, null);
  			holder.imageView = (ImageView) view.findViewById(R.id.iv_content);
  			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
  			
  			// Set the imageview height to be same as width
  	        ViewGroup.LayoutParams imageViewParams = holder.imageView.getLayoutParams();
  	        int imageWidth = (AppManager.mScreenWidth - 4)/3;
  	        imageViewParams.width = imageWidth;
  	        imageViewParams.height = imageWidth;
  	        holder.imageView.setLayoutParams(imageViewParams);
  			view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ImageLoader.getInstance().displayImage(mPhotosList.get(position), holder.imageView, TheMissApplication.getInstance().options, new SimpleImageLoadingListener() {
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
        
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if(mPhotosList.get(position) != null){
					Intent returnIntent = new Intent();
					returnIntent.putExtra("url", mPhotosList.get(position));
					mActivity.setResult(Activity.RESULT_OK, returnIntent);
					mActivity.finish();}
			}
        	
        });
        return view;
    }

}