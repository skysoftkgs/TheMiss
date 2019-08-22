package com.ghebb.themiss.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.CommentFragment;
import com.ghebb.themiss.HomeFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
 
public class ProfilePostsListCellAdapter extends BaseAdapter {
 
	private static final int TYPE_MONTH = 0;
    private static final int TYPE_POST = 1;
    private static final int TYPE_MAX_COUNT = TYPE_POST + 1;
    
    // Declare Variables
    ProfileFragment mFragment;
    LayoutInflater mInflater;
    List<PostModel> mPostsList = new ArrayList<PostModel>();
    ParseUser mUser;
    int mCurrentMonth;
    
    public ProfilePostsListCellAdapter(ProfileFragment fragment,
            List<PostModel> postsList, ParseUser user) {
        mFragment = fragment;
        mPostsList = postsList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mUser = user;
        
        if(mUser.getString("gender")!= null && mUser.getString("gender").equalsIgnoreCase("female")){	//if gender is female, show month list
	        Calendar cal = Calendar.getInstance();
	        mCurrentMonth = cal.get(Calendar.MONTH);
        }else{
        	mCurrentMonth = 0;
        }
    }
 
    public class ViewHolder {
    	TextView monthTextView;
    	TextView timeTextView;
    	TextView userNameTextView;
		TextView voteTextView;
		TextView shareTextView;
		TextView commentTextView;
		ImageView postImageView;
		ImageView profilePictureImageView;
		ImageButton voteImageButton;
		ImageButton shareImageButton;
		ImageButton commentImageButton;
		ImageButton actionImageButton;
		RelativeLayout commentLayout;
		ProgressBar progressBar;
		AdView adView;
    }
     
    @Override
    public int getItemViewType(int position) {
    	if(position >= mPostsList.size()){
    		return TYPE_MONTH;
    	}else{
    		return TYPE_POST;
    	}
    }
    
    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }
    
    @Override
    public int getCount() {
        return mPostsList.size() + mCurrentMonth;
    }
 
    @Override
    public Object getItem(int position) {
    	if(position >= mPostsList.size()){
    		return mCurrentMonth - (position - mPostsList.size());
    	}else{
    		return mPostsList.get(position);
    	}
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint("SimpleDateFormat")
	public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        
        if(view == null){
        	holder = new ViewHolder();
        	int type = getItemViewType(position);
         	if(type == TYPE_MONTH){                   
	            view = mInflater.inflate(R.layout.item_month, null);
	            holder.monthTextView = (TextView) view.findViewById(R.id.tv_month);
	            
         	}else{
         		view = mInflater.inflate(R.layout.item_profile_self_postlist, null);
 	            holder.timeTextView = (TextView) view.findViewById(R.id.tv_profile_postlist_month);
 	            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_profile_postlist_username);
 	            holder.voteTextView = (TextView) view.findViewById(R.id.tv_home_postlist_votecount);
 	            holder.shareTextView = (TextView) view.findViewById(R.id.tv_home_postlist_sharecount);
 	            holder.commentTextView = (TextView) view.findViewById(R.id.tv_profile_comment_count);
 				holder.postImageView = (ImageView) view.findViewById(R.id.iv_content);
 				holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_profile_postlist_photo);
 				holder.voteImageButton = (ImageButton) view.findViewById(R.id.ib_home_vote);
 				holder.shareImageButton = (ImageButton) view.findViewById(R.id.ib_home_share);
 				holder.commentImageButton = (ImageButton) view.findViewById(R.id.ib_profile_comment);
 				holder.actionImageButton = (ImageButton) view.findViewById(R.id.ib_profile_photo_action);
 				holder.commentLayout = (RelativeLayout) view.findViewById(R.id.layout_profile_item_comment);
 				holder.adView = (AdView) view.findViewById(R.id.adView);
 				holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
 				
 				// Set the imageview height to be same as width
 		        ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
 		        imageViewParams.height = AppManager.mScreenWidth;
 		        holder.postImageView.setLayoutParams(imageViewParams);
         	}
         	view.setTag(holder);
         	
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        if(position >= mPostsList.size()){

            final int month = mCurrentMonth -(position - mPostsList.size());
            
        	view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(month <= mFragment.mMonth){
						mFragment.displaySelfPostsByDate(Calendar.getInstance().get(Calendar.YEAR), month-1, 1);
					}else{
						mFragment.displaySelfPostsByDate(Calendar.getInstance().get(Calendar.YEAR), month, 1);
					}
				}
			});
        	
        	if(month <= mFragment.mMonth){
        		holder.monthTextView.setText(UtilityMethods.getMonth(month - 1));
            }else{
            	holder.monthTextView.setText(UtilityMethods.getMonth(month));
            }

        	
        }else{
	        
	        final PostModel post = mPostsList.get(position);
	               
	        // Set the results into TextViews
	        Date postDate = post.getCreatedAt();
	        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
			holder.timeTextView.setText(formatter.format(postDate));
			        
			TheMissApplication.getInstance().displayImage(post.getPhotoFile().getUrl(), 
					holder.postImageView, holder.progressBar);
			
			//display vote count
	        holder.voteTextView.setText(String.valueOf(post.getVoteCount()));
	        
	        //display shared count
	        holder.shareTextView.setText(String.valueOf(post.getShareCount()));
	        final ParseUser currentUser = ParseUser.getCurrentUser();
	        
	        if(AppManager.isFemale(mUser) == false){
	        	holder.commentLayout.setVisibility(View.GONE);
	        	
	        	//display username
	        	if(post.getUser() != null){
		            holder.userNameTextView.setText(post.getUser().getUsername());
		            //display profile image.
		            TheMissApplication.getInstance().displayUserProfileImage(post.getUser(), holder.profilePictureImageView);
	        	}
	        	
	        }else{
	        	//display username
	            holder.userNameTextView.setText(mUser.getUsername());
	            
	            //display profile image.
	            TheMissApplication.getInstance().displayUserProfileImage(mUser, holder.profilePictureImageView);
	            
		        //display comment count
		        holder.commentTextView.setText(String.valueOf(post.getCommentUserList().size()));
		        
		        //display comment status
				if(AppManager.isLoggedIn(mFragment.getActivity()) && currentUser != null){
					 if(post.getCommentUserList().contains(currentUser.getObjectId())){
			        	holder.commentImageButton.setSelected(true);
					}else{
						holder.commentImageButton.setSelected(false);
					}
				}
	        }
	        
	        //display voted status
			if(AppManager.isLoggedIn(mFragment.getActivity()) && mUser != null){
				if(AppManager.isFemale(mUser) == false){
					if(post.getVoteUserList().contains(mUser.getObjectId())){
			        	holder.voteImageButton.setSelected(true);
					}else{
						holder.voteImageButton.setSelected(false);
					}
				}else{
					if(post.getVoteUserList().contains(currentUser.getObjectId())){
			        	holder.voteImageButton.setSelected(true);
					}else{
						holder.voteImageButton.setSelected(false);
					}
				}
			}
	               
	        holder.shareImageButton.setOnClickListener(new OnClickListener(){
	
				@SuppressLint("SdCardPath")
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					
					//if it is not current month post, return
	//				if(year != mFragment.mYear || month != mFragment.mMonth) return;
								
					if(AppManager.isLoggedIn(mFragment.getActivity()) == false){
						Intent intent = new Intent(mFragment.getActivity(), MainLoginActivity.class);
						mFragment.startActivity(intent);
						return;
					}
					
					ProfileFragment.mSelectedPostObject = post;
					ProfileFragment.mShareCountTextView = holder.shareTextView;
					try {
						post.getUser().fetchIfNeeded();
						MainActivity activity = (MainActivity) mFragment.getActivity();
						activity.shareImage(post, post.getPhotoFile().getUrl(), post.getUser(), mFragment);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
				}       	
	        });
	        
	        holder.voteImageButton.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//if it is not current month post, return
	//				if(year != mFragment.mYear || month != mFragment.mMonth) return;
					
					MainActivity mActivity = (MainActivity)mFragment.getActivity();
					mActivity.votePhoto(null, null, post, holder.voteTextView, holder.voteImageButton);
				}
	        	
	        });
	        
	        holder.commentImageButton.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					MainActivity activity = (MainActivity) mFragment.getActivity();
					CommentFragment fragment = new CommentFragment();
					if(fragment != null){
						Bundle bundle = new Bundle();
						bundle.putString("time", holder.timeTextView.getText().toString());
						bundle.putString("postImageUrl", post.getPhotoFile().getUrl());
						bundle.putBoolean("voteStatus", holder.voteImageButton.isSelected());
						bundle.putString("voteCount", holder.voteTextView.getText().toString());
						bundle.putString("shareCount", holder.shareTextView.getText().toString());
						bundle.putSerializable("post", post);
						fragment.setArguments(bundle);
						fragment.mUser = mUser;
						activity.addContent(fragment, CommentFragment.TAG);
					}
				}
	        	
	        });
	        
	        holder.actionImageButton.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mUser == null || currentUser == null) return;
	
					AlertDialog.Builder buildSingle = new AlertDialog.Builder(mFragment.getActivity());
					final ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(mFragment.getActivity(),
							R.layout.item_dialog_row);
					if(mUser.getObjectId().equalsIgnoreCase(currentUser.getObjectId())){
						aAdapter.add(mFragment.getResources().getString(R.string.delete));
					}else{
						aAdapter.add(mFragment.getResources().getString(R.string.report_inappropriate));
					}
					
					buildSingle.setNegativeButton(mFragment.getResources().getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							});
					buildSingle.setAdapter(aAdapter,
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String chosen = aAdapter.getItem(which)
											.toString();
									if (chosen.equals(mFragment.getResources().getString(R.string.report_inappropriate))) {
										mFragment.mRefreshingProgressBar.setVisibility(View.VISIBLE);
										mFragment.mRefreshButton.setVisibility(View.INVISIBLE);
										
										ParseObject flaggedObject = new ParseObject("FlagedPicture");
										flaggedObject.put("post", post);
										flaggedObject.put("user", mUser);
										flaggedObject.saveInBackground(new SaveCallback(){
	
											@Override
											public void done(ParseException arg0) {
												// TODO Auto-generated method stub
												if(arg0 == null){
													mFragment.mRefreshingProgressBar.setVisibility(View.INVISIBLE);
													mFragment.mRefreshButton.setVisibility(View.VISIBLE);
													Toast.makeText(mFragment.getActivity(), mFragment.getResources().getString(R.string.report_success_message), Toast.LENGTH_SHORT).show();
													
													MainActivity mActivity = (MainActivity) mFragment.getActivity();
													mActivity.sendFlagNotification();
												}
											}
											
										});
							            
									} else if (chosen.equals(mFragment.getResources().getString(R.string.delete))) {
										AlertDialog.Builder alertDialog = new AlertDialog.Builder(mFragment.getActivity());
										
										alertDialog.setTitle(mFragment.getResources().getString(R.string.confirm_deleting));
										alertDialog.setMessage(mFragment.getResources().getString(R.string.are_you_sure_delete));
	
										// Setting Positive "Yes" Btn
										alertDialog.setPositiveButton(mFragment.getResources().getString(R.string.yes),
										        new DialogInterface.OnClickListener() {
										            public void onClick(DialogInterface dialog, int which) {
										            	
														TheMissApplication.getInstance().showProgressFullScreenDialog(mFragment.getActivity());
														if(AppManager.isFemale(currentUser)){
																															
															post.deleteInBackground(new DeleteCallback(){

																@Override
																public void done(ParseException arg0) {
																	// TODO Auto-generated method stub
																	if(arg0 == null){
																		mPostsList.remove(post);
																		notifyDataSetChanged();
																		HomeFragment.mRequiredRefresh = true;
																		
																		TheMissApplication.getInstance().hideProgressDialog();
																		
																		//update last post for month ranking
																		MainActivity activity = (MainActivity) mFragment.getActivity();
																		activity.setLastPost(ParseUser.getCurrentUser());
																	}
																}
																
															});
																
															
														}else{
															List<String> dislikeUsers = post.getList("dislikeUsers");
															if(dislikeUsers == null)
																dislikeUsers = new ArrayList<String>();
															dislikeUsers.add(currentUser.getObjectId());
															post.put("dislikeUsers", dislikeUsers);
															post.saveInBackground(new SaveCallback(){
	
																@Override
																public void done(
																	ParseException arg0) {
																	// TODO Auto-generated method stub
																	if(arg0 == null){
																		mFragment.fetchVotedPosts();
																	}
																}
																
															});
														}									                
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
								}
							});
					
					AlertDialog a = buildSingle.create();
					a.show();
					Button bq = a.getButton(DialogInterface.BUTTON_NEGATIVE);
					bq.setTextColor(Color.GRAY);
				}});
	        
	        //display AD banner
	        if(position % 5 == 4){
	        	holder.adView.loadAd(new AdRequest());
	        	holder.adView.setVisibility(View.VISIBLE);
	        }else{
	        	holder.adView.setVisibility(View.GONE);
	        }
        }
        
        if(position == mPostsList.size() - 1){
        	if(AppManager.isFemale(mUser) == true){
        		mFragment.refreshSelfPosts();
        	}else{
        		mFragment.fetchVotedPosts();
        	}
        }
        
        return view;
    }
}