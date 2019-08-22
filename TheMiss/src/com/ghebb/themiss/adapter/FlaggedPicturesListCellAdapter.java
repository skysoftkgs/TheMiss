package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.AdminSettingsFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.datamodel.PostModel;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
 
public class FlaggedPicturesListCellAdapter extends BaseAdapter {
 
    // Declare Variables
    AdminSettingsFragment mFragment;
    LayoutInflater mInflater;
    List<ParseObject> mFlaggedPicturesList = new ArrayList<ParseObject>();

    public FlaggedPicturesListCellAdapter(AdminSettingsFragment fragment,
            List<ParseObject> flaggedList) {
        mFragment = fragment;
        mFlaggedPicturesList = flaggedList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public class ViewHolder {
    	TextView userNameTextView;
		ImageView postImageView;
		ImageView profilePictureImageView;
		Button deleteButton;
		ProgressBar progressBar;
	}
 
    @Override
    public int getCount() {
        return mFlaggedPicturesList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mFlaggedPicturesList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
	public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_settings_flaggedlist, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_settings_flaggedlist_username);
            holder.postImageView = (ImageView) view.findViewById(R.id.iv_content);
			holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_settings_flaggedlist_photo);
			holder.deleteButton = (Button) view.findViewById(R.id.btn_settings_delete);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			
			// Set the imageview height to be same as width
	        ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
	        imageViewParams.height = AppManager.mScreenWidth;
	        holder.postImageView.setLayoutParams(imageViewParams);
			view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseObject flaggedPicture = mFlaggedPicturesList.get(position);
        final PostModel post = (PostModel) flaggedPicture.get("post");
                       
        if(post != null && post.getPhotoFile() != null && post.getPhotoFile().getUrl() != null){
        	TheMissApplication.getInstance().displayImage(post.getPhotoFile().getUrl(), holder.postImageView, holder.progressBar);
        }
        
        ParseUser postUser = (ParseUser) flaggedPicture.get("user");
       
        if(postUser != null){
        	 //display username
         	holder.userNameTextView.setText(postUser.getUsername());
        
	        //display profile image.
         	TheMissApplication.getInstance().displayUserProfileImage(postUser, holder.profilePictureImageView);
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
				            	
				            	TheMissApplication.getInstance().showProgressFullScreenDialog(mFragment.getActivity());
				            	
								post.deleteInBackground(new DeleteCallback(){

									@Override
									public void done(ParseException arg0) {
										// TODO Auto-generated method stub
										TheMissApplication.getInstance().hideProgressDialog();
										if(arg0 == null){
											mFlaggedPicturesList.remove(flaggedPicture);
											FlaggedPicturesListCellAdapter.this.notifyDataSetChanged();
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