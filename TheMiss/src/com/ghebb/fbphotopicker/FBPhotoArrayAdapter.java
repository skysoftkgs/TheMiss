package com.ghebb.fbphotopicker;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.gab.themiss.R;
import com.ghebb.themiss.TheMissApplication;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class FBPhotoArrayAdapter extends ArrayAdapter<FBPhoto> {
    private LayoutInflater mInflater;
    private int mImageWidth;

    public FBPhotoArrayAdapter(Context context, int textViewResourceId, List<FBPhoto> objects, int imageWidth) {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        mImageWidth = imageWidth;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PhotoHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_photo_grid_item, parent, false);
            holder = new PhotoHolder();
            holder.photo = (ImageView) convertView.findViewById(R.id.imageView_photo);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
            
            // Set the imageview height to be same as width
            ViewGroup.LayoutParams imageViewParams = holder.photo.getLayoutParams();
            imageViewParams.width = mImageWidth;
            imageViewParams.height = mImageWidth;
            holder.photo.setLayoutParams(imageViewParams);
            
            convertView.setTag(holder);
            
        } else {
            holder = (PhotoHolder) convertView.getTag();
        }
        
        FBPhoto photo = getItem(position);
        
        // display post image
        ImageLoader.getInstance().displayImage(photo.getSource(), holder.photo, TheMissApplication.getInstance().options, new SimpleImageLoadingListener() {
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
        return convertView;
    }

    /**
     * Holder pattern to make ListView more efficient.
     * 
     * @author dkadlecek
     * 
     */
    static class PhotoHolder {
        ImageView photo;
        ProgressBar progressBar;
    }
}
