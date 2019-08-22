package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
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
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.plattysoft.ui.ListAsGridBaseAdapter;
 
public class HomeMissOfMonthListCellAdapter extends ListAsGridBaseAdapter {
 
    // Declare Variables
    HomeFragment mFragment;
    LayoutInflater mInflater;
    public List<UserInfoModel> mMissOfMonthList = new ArrayList<UserInfoModel>();
    ParseUser mCurrentUser;

    public HomeMissOfMonthListCellAdapter(HomeFragment fragment,
            List<UserInfoModel> missOfMonthList) {
    	super(fragment.getActivity());
        mFragment = fragment;
        mMissOfMonthList = missOfMonthList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
        mCurrentUser = ParseUser.getCurrentUser();
    }
 
    public static class ViewHolder {
    	TextView totalActionTextView;
    	TextView rankingTextView;
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
            view = mInflater.inflate(R.layout.item_home_postlist_missofmonth, null);
            holder.totalActionTextView = (TextView) view.findViewById(R.id.tv_home_postlist_total_action);
            holder.rankingTextView = (TextView) view.findViewById(R.id.tv_home_postlist_ranking);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_home_postlist_username);
            holder.voteTextView = (TextView) view.findViewById(R.id.tv_home_postlist_votecount);
            holder.shareTextView = (TextView) view.findViewById(R.id.tv_home_postlist_sharecount);
			holder.postImageView = (ImageView) view.findViewById(R.id.iv_content);
			holder.profilePictureImageView = (RoundedImageView) view.findViewById(R.id.iv_home_postlist_photo);
			holder.voteImageButton = (ImageButton) view.findViewById(R.id.ib_home_vote);
			holder.shareImageButton = (ImageButton) view.findViewById(R.id.ib_home_share);
			holder.actionImageButton = (ImageButton) view.findViewById(R.id.ib_home_photo_action);
			holder.adView = (AdView) view.findViewById(R.id.adView);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			// Set the imageview height to be same as width
	        ViewGroup.LayoutParams imageViewParams = holder.postImageView.getLayoutParams();
	        imageViewParams.height = AppManager.mScreenWidth;
	        holder.postImageView.setLayoutParams(imageViewParams);
	        
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final PostModel post = mMissOfMonthList.get(position).getLastPost();        
        displayPost(holder, post);        
        
        //diplay ranking NO
        holder.rankingTextView.setText(String.valueOf(position+1));
        if(position<9){
        	holder.rankingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFragment.getResources().getDimension(R.dimen.miss_month_ranking_large_font));
        }else if(position>=9 && position <99){
        	holder.rankingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFragment.getResources().getDimension(R.dimen.miss_month_ranking_medium_font));
        }else{
        	holder.rankingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFragment.getResources().getDimension(R.dimen.miss_month_ranking_small_font));
        }
                
        final ParseUser postUser = mMissOfMonthList.get(position).getUser();
		if(postUser != null){
			//display username
	        holder.userNameTextView.setText(postUser.getUsername());
	        
	        //display profile image
	        TheMissApplication.getInstance().displayUserProfileImage(postUser, holder.profilePictureImageView);        
		}
                       
        //display total action count
        holder.totalActionTextView.setText(String.valueOf(mMissOfMonthList.get(position).getVoteCount()));
        
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
				activity.shareImage(post, post.getPhotoFile().getUrl(), postUser, mFragment);
			}       	
        });
        
        holder.voteImageButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity mActivity = (MainActivity)mFragment.getActivity();
				mActivity.votePhoto(mMissOfMonthList.get(position), holder.totalActionTextView, post, holder.voteTextView, holder.voteImageButton);
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
				if(postUser != null && mCurrentUser != null && postUser.getObjectId().equalsIgnoreCase(mCurrentUser.getObjectId()))
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
															if(arg0 == null){
																//update last post for month ranking
																setLastPost(ParseUser.getCurrentUser(), position);
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
        
        if(position == mMissOfMonthList.size() - 1){
        	mFragment.fetchMissOfMonth();
        }
        
        return view;
    }

    public void displayPost(final ViewHolder holder, PostModel post){
    	if(post == null){
    		holder.postImageView.setImageBitmap(null);
    		
    	}else{
    	    TheMissApplication.getInstance().displayImage(post.getPhotoFile().getUrl(), 
	    			holder.postImageView, holder.progressBar);
    	}
		//display vote count
    	if(post == null){
    		holder.voteTextView.setText("");
    	}else{
    		holder.voteTextView.setText(String.valueOf(post.getVoteCount()));
    	}
    	
    	//display shared count
    	if(post == null){
    		holder.shareTextView.setText("");
    	}else{
    		holder.shareTextView.setText(String.valueOf(post.getShareCount()));
    	}
    	
    	//display voted status
    	if(post == null){
    		holder.voteImageButton.setSelected(false);
    		
    	}else{
    		if(post.getVoteUserList().contains(mCurrentUser.getObjectId())){
        		holder.voteImageButton.setSelected(true);
    		}else{
    			holder.voteImageButton.setSelected(false);
    		}
    	}
    }
    
    public void setLastPost(final ParseUser user, final int position){
		ParseQuery<UserInfoModel> query = ParseQuery.getQuery("UserInfo");
		query.whereEqualTo("user", user);
		query.getFirstInBackground(new GetCallback<UserInfoModel>() {

			@Override
			public void done(final UserInfoModel userInfo, ParseException err) {
				
				ParseQuery<PostModel> query = new ParseQuery<PostModel>("Post");
				query.whereEqualTo("user", user);
				query.orderByDescending("createdAt");
				query.getFirstInBackground(new GetCallback<PostModel>(){

					@Override
					public void done(final PostModel post, ParseException arg1) {
						// TODO Auto-generated method stub
						
						if(post == null){
							userInfo.deleteEventually(new DeleteCallback() {
								
								@Override
								public void done(ParseException arg0) {
									// TODO Auto-generated method stub
									AppManager.mMissOfMonthList.remove(position);
									notifyDataSetChanged();
									TheMissApplication.getInstance().hideProgressDialog();
									
								}
							});
							return;
						}
						
						//update with last post
						userInfo.setLastPost(post);
						userInfo.saveEventually(new SaveCallback() {
							
							@Override
							public void done(ParseException arg0) {
								// TODO Auto-generated method stub
								AppManager.mMissOfMonthList.get(position).setLastPost(post);
								notifyDataSetChanged();
								TheMissApplication.getInstance().hideProgressDialog();
							}
						});
					}
				});
			}
		});		
	}
    
    public void showProfile(int position){
    	ProfileFragment profileFragment = new ProfileFragment();
    	profileFragment.mUser = mMissOfMonthList.get(position).getUser();
//		Bundle bundle = new Bundle();
//		bundle.putSerializable("Post", mMissOfMonthList.get(position));
//		profileFragment.setArguments(bundle);
		MainActivity mainActivity = (MainActivity) mFragment.getActivity();
		mainActivity.addContent(profileFragment, ProfileFragment.TAG);
    }
}