package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.gab.themiss.R;
import com.ghebb.themiss.PrizesFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
 
public class PrizesListCellAdapter extends BaseAdapter {
 
    // Declare Variables
    PrizesFragment mFragment;
    LayoutInflater mInflater;
    List<ParseObject> mPrizesList = new ArrayList<ParseObject>();

    
    public PrizesListCellAdapter(PrizesFragment fragment,
            List<ParseObject> prizesList) {
        mFragment = fragment;
        mPrizesList = prizesList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public class ViewHolder {
    	ImageView prizeImageView;
		Button deleteButton;
		ProgressBar progressBar;
	}
 
    @Override
    public int getCount() {
        return mPrizesList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mPrizesList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
	public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_prizes, null);
            holder.prizeImageView = (ImageView) view.findViewById(R.id.iv_content);
			holder.deleteButton = (Button) view.findViewById(R.id.btn_prizes_delete);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			
			// Set the imageview height to be same as width
	        ViewGroup.LayoutParams imageViewParams = holder.prizeImageView.getLayoutParams();
	        imageViewParams.height = AppManager.mScreenWidth;
	        holder.prizeImageView.setLayoutParams(imageViewParams);
			view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseObject prize = mPrizesList.get(position);
                
        ParseFile imageFile = prize.getParseFile("image");
        if(imageFile != null && imageFile.getUrl() != null){
        	ImageLoader.getInstance().displayImage(imageFile.getUrl(), holder.prizeImageView, TheMissApplication.getInstance().options, new SimpleImageLoadingListener() {
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
        	
        
        ParseUser currentUser = ParseUser.getCurrentUser();
		if(currentUser != null && currentUser.getBoolean("admin")==true){
			holder.deleteButton.setVisibility(View.VISIBLE);
		}else{
			holder.deleteButton.setVisibility(View.GONE);
		}
				
        holder.deleteButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(mFragment.getActivity());
				alertDialog.setTitle(mFragment.getResources().getString(R.string.confirm_deleting));
				alertDialog.setMessage(mFragment.getResources().getString(R.string.are_you_sure_delete));

				// Setting Positive "Yes" Btn
				alertDialog.setPositiveButton(mFragment.getResources().getString(R.string.yes),
				        new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int which) {
				            	
				            	mFragment.mProgressDialog = ProgressDialog.show(mFragment.getActivity(), "", "Deleting...", true);
				            	mFragment.mRefreshingProgressBar.setVisibility(View.VISIBLE);
				            	mFragment.mRefreshButton.setVisibility(View.INVISIBLE);
				            	prize.deleteInBackground(new DeleteCallback(){

									@Override
									public void done(ParseException arg0) {
										// TODO Auto-generated method stub
										if(arg0 == null){
											mFragment.refreshPrizes();
										}
									}
								});
				                
				            }
				        });
				// Setting Negative "NO" Btn
				alertDialog.setNegativeButton("NO",
				        new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int which) {
				              				                
				                dialog.cancel();
				            }
				        });

				// Showing Alert Dialog
				alertDialog.show();
			}
        	
        });
        return view;
    }
}