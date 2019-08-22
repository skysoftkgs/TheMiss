package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.HomeFragment;
import com.ghebb.themiss.MainActivity;
import com.ghebb.themiss.ProfileFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.custom.RoundedImageView;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class HomeLastPicturesListCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    public List<PostModel> mPostsList = new ArrayList<PostModel>();
    ParseUser mCurrentUser;
       
    public HomeLastPicturesListCellAdapter(HomeFragment fragment,
            List<PostModel> postsList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mPostsList = postsList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mCurrentUser = ParseUser.getCurrentUser();
    }
 
    public class ViewHolder {
    	TextView timeTextView;
    	TextView userNameTextView;
		TextView voteTextView;
		TextView shareTextView;
		ImageView postImageView;
		RoundedImageView profilePictureImageView;
		ImageButton voteImageButton;
		ImageButton shareImageButton;
		ImageButton actionImageButton;
		ProgressBar progressBar;
		AdView adView;
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
            view = mInflater.inflate(R.layout.item_home_postlist_lastpictures, null);
            holder.timeTextView = (TextView) view.findViewById(R.id.tv_home_postlist_time);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_home_postlist_username);
            holder.voteTextView = (TextView) view.findViewById(R.id.tv_home_postlist_votecount);
            holder.shareTextView = (TextView) view.findViewById(R.id.tv_home_postlist_sharecount);
			holder.postImageView = (ImageView) view.findViewById(R.id.iv_content);
			holder.profilePictureImageView = (RoundedImageView) view.findViewById(R.id.iv_home_postlist_photo);
			holder.voteImageButton = (ImageButton) view.findViewById(R.id.ib_home_vote);
			holder.shareImageButton = (ImageButton) view.findViewById(R.id.ib_home_share);
			holder.actionImageButton = (ImageButton) view.findViewById(R.id.ib_home_photo_action);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			holder.adView = (AdView) view.findViewById(R.id.adView);
			ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
		    imageViewParams.height = AppManager.mScreenWidth;
		    holder.postImageView.setLayoutParams(imageViewParams);
		    
			view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final PostModel post = mPostsList.get(position);
        	    
        // display past time since post
        Date today = new Date();
		long diff = today.getTime() - post.getCreatedAt().getTime();
		long seconds = diff / 1000;
		long minutes = (seconds / 60) % 60;
		long hours = (seconds / 3600) % 24;
		long days = (seconds /3600) / 24;
        holder.timeTextView.setText(String.format("%d%s %dh %dm", days, mFragment.getResources().getString(R.string.date), hours, minutes));

    	// display post image
        if(mPostsList.get(position).getPhotoFile() != null)
	        TheMissApplication.getInstance().displayImage(mPostsList.get(position).getPhotoFile().getUrl(), 
	        		holder.postImageView, holder.progressBar);
                
		final ParseUser postUser = post.getUser();
		if(postUser != null){
			//display username
	        holder.userNameTextView.setText(postUser.getUsername());
	        //display profile image
	        TheMissApplication.getInstance().displayUserProfileImage(postUser, holder.profilePictureImageView);
		}
		  
        //display vote count
        holder.voteTextView.setText(String.valueOf(post.getVoteCount()));
        
        //display voted status
        if(post.getVoteUserList().contains(mCurrentUser.getObjectId())){
        	holder.voteImageButton.setSelected(true);
		}else{
			holder.voteImageButton.setSelected(false);
		}
        
        //display share count
        holder.shareTextView.setText(String.valueOf(post.getShareCount()));
                
        holder.shareImageButton.setOnClickListener(new OnClickListener(){

			@SuppressLint("SdCardPath")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(AppManager.isLoggedIn(mFragment.getActivity()) == false){
					Intent intent = new Intent(mFragment.getActivity(), MainLoginActivity.class);
					mFragment.startActivity(intent);
					return;
				}
				
				HomeFragment.mSelectedPostObject = post;
				HomeFragment.mShareCountTextView = holder.shareTextView;
				
				MainActivity activity = (MainActivity) mFragment.getActivity();
				activity.shareImage(post, post.getPhotoFile().getUrl(), post.getUser(), mFragment);
			}       	
        });
        
        holder.voteImageButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity mActivity = (MainActivity)mFragment.getActivity();
				mActivity.votePhoto(null, null, post, holder.voteTextView, holder.voteImageButton);
			}
        	
        });
        
        holder.postImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
        	
        });
        
        holder.profilePictureImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
        	
        });
        
        holder.userNameTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showProfile(position);
			}
        	
        });
        
        holder.actionImageButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				if(Utils.checkLoggedInUser(mFragment.getActivity()) == false){
//					Intent intent = new Intent(mFragment.getActivity(), LoginActivity.class);
//					mFragment.startActivity(intent);
//					return;
//				}
				
				AlertDialog.Builder buildSingle = new AlertDialog.Builder(mFragment.getActivity());
				final ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(mFragment.getActivity(),
						R.layout.item_dialog_row);
				if(postUser != null && mCurrentUser != null && postUser.getObjectId().equalsIgnoreCase(mCurrentUser.getObjectId()) == false)
					aAdapter.add(mFragment.getResources().getString(R.string.report_inappropriate));
				else
					aAdapter.add(mFragment.getResources().getString(R.string.delete));
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
									flaggedObject.put("user", postUser);
									flaggedObject.put("new", true);
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
													post.deleteInBackground(new DeleteCallback(){

														@Override
														public void done(ParseException arg0) {
															// TODO Auto-generated method stub
															TheMissApplication.getInstance().hideProgressDialog();
															
															if(arg0 == null){
																AppManager.mLastPicturesList.remove(post);
																notifyDataSetChanged();
																
																//update last post for month ranking
																MainActivity activity = (MainActivity) mFragment.getActivity();
																activity.setLastPost(ParseUser.getCurrentUser());
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
	        
        if(position == mPostsList.size() - 1){
        	mFragment.fetchLastPictures();
        }
        
        return view;
    }

    public void showProfile(int position){
    	ProfileFragment profileFragment = new ProfileFragment();
    	profileFragment.mUser = mPostsList.get(position).getUser();
		MainActivity mainActivity = (MainActivity) mFragment.getActivity();
		mainActivity.addContent(profileFragment, ProfileFragment.TAG);
    }	
}