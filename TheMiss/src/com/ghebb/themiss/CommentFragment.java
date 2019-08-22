package com.ghebb.themiss;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.CommentsListCellAdapter;
import com.ghebb.themiss.common.AppManager;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.NotificationService;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.datamodel.CommentsListModel;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.landing.MainLoginActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CommentFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "CommentFragment";
	
	ListView mCommentListView;
	EditText mCommentEditText;
	ImageView mPostImageView;
	ImageButton mRefreshButton;
	ImageButton mCommentButton;
	ImageButton mVoteButton;
	ImageButton mShareButton;
	ImageButton mSendButton;
	ProgressBar mRefreshingProgressBar;
	TextView mCommentCountTextView;
	TextView mShareCountTextView;
	TextView mVoteCountTextView;
	RelativeLayout mInputBarLayout;
	
	MainActivity mActivity;
	public PostModel mPost;
	public ParseUser mUser;
	ArrayList<CommentsListModel> mCommentsList;
	boolean mRefreshingComment;
	boolean mSavingComment;
	
	int mVotesCount;
	int mShareCount;
	
	private final Observer shareNotification = new Observer()
	{
		public void update(Observable observable, Object data)
		{
			shareAction();
		}
	};
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_comment, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		mCommentListView = (ListView) view.findViewById(R.id.lv_comment);
		View header = View.inflate(mActivity, R.layout.header_comment, null);
		mCommentListView.addHeaderView(header);
		
		String postImageUrl;
		boolean voteStatus = false;
		String month = null;
		
		if(mPost == null){			//display from profile
			month = getArguments().getString("time");
			postImageUrl = getArguments().getString("postImageUrl");
			mVotesCount = Integer.parseInt(getArguments().getString("voteCount"));
			mShareCount = Integer.parseInt(getArguments().getString("shareCount"));
			mPost = (PostModel) getArguments().getSerializable("post");
			voteStatus = getArguments().getBoolean("voteStatus");
			
		}else{						//display from notification
			Date postDate = mPost.getCreatedAt();
		    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
			month = formatter.format(postDate);
				
			postImageUrl = mPost.getPhotoFile().getUrl();
			mVotesCount = mPost.getVoteCount();
			mShareCount = mPost.getShareCount();
			
			//display voted status
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
			ParseUser currentUser = ParseUser.getCurrentUser();
			if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)== true && currentUser != null){
				if(mPost.getVoteUserList().contains(currentUser.getObjectId())){
					voteStatus = true;
				}else{
					voteStatus = false;
				}
			}
		}
		
		
		TextView userNameTextView = (TextView) header.findViewById(R.id.tv_comment_username);
		userNameTextView.setText(mUser.getUsername());
		
		
		ImageView profileImageView = (ImageView) header.findViewById(R.id.iv_comment_profile_photo);
		TheMissApplication.getInstance().displayUserProfileImage(mUser, profileImageView);
		
		mPostImageView = (ImageView) header.findViewById(R.id.iv_content);
		ViewGroup.LayoutParams imageViewParams = mPostImageView.getLayoutParams();
        imageViewParams.height = AppManager.mScreenWidth;
        mPostImageView.setLayoutParams(imageViewParams);
        final ProgressBar progressBar = (ProgressBar) header.findViewById(R.id.progress);

        // display post image
        TheMissApplication.getInstance().displayImage(postImageUrl, mPostImageView, progressBar);
        
		TextView monthTextView = (TextView) header.findViewById(R.id.tv_comment_month);
		monthTextView.setText(month);
		
		mVoteCountTextView = (TextView) header.findViewById(R.id.tv_comment_votecount);
		mVoteCountTextView.setText(String.valueOf(mVotesCount));
		
		mCommentButton = (ImageButton) header.findViewById(R.id.ib_comment_comment);
		
		mVoteButton = (ImageButton) header.findViewById(R.id.ib_home_vote);
		mVoteButton.setOnClickListener(this);
		
		//display voted status
		if(voteStatus == true)
			mVoteButton.setSelected(true);
		else
			mVoteButton.setSelected(false);
				
		mShareButton = (ImageButton) header.findViewById(R.id.ib_home_share);
		mShareButton.setOnClickListener(this);
		
		mCommentCountTextView = (TextView) header.findViewById(R.id.tv_comment_commentcount);
		mShareCountTextView = (TextView) view.findViewById(R.id.tv_comment_sharecount);
		mShareCountTextView.setText(String.valueOf(mShareCount));
		
		final ParseUser currentUser = ParseUser.getCurrentUser();
		mCommentEditText = (EditText) view.findViewById(R.id.et_comment_input);
		mCommentEditText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable et) {
				// TODO Auto-generated method stub
				if(et.toString().length()==0){
					if(currentUser.getString("language") != null && currentUser.getString("language").equalsIgnoreCase("italian"))
						mSendButton.setImageResource(R.drawable.comment_send_deactive_it_btn);
					else
						mSendButton.setImageResource(R.drawable.comment_send_deactive_btn);
				}else
				{
					if(currentUser.getString("language") != null && currentUser.getString("language").equalsIgnoreCase("italian"))
						mSendButton.setImageResource(R.drawable.comment_send_active_it_btn);
					else
						mSendButton.setImageResource(R.drawable.comment_send_active_btn);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
		});
				
		mSendButton = (ImageButton) view.findViewById(R.id.ib_comment_send);
		if(currentUser.getString("language") != null && currentUser.getString("language").equalsIgnoreCase("italian")){
			mSendButton.setImageResource(R.drawable.comment_send_deactive_it_btn);
		}else{
			mSendButton.setImageResource(R.drawable.comment_send_deactive_btn);
		}
		mSendButton.setOnClickListener(this);
		
		//if not logged in, hide inputbar
		mInputBarLayout = (RelativeLayout) view.findViewById(R.id.layout_comment_inputbar);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==false){
			mInputBarLayout.setVisibility(View.GONE);
		}
		
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);	
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		
		//refresh at startup
		refreshComment();
	
		NotificationService.getInstance().addObserver(Constants.NOTIFICATION_SHARE_COMMENT_SUCCESS, shareNotification);
		view.setOnClickListener(null);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if(mActivity.currentFragment != null && !this.getClass().equals(mActivity.currentFragment)) return;
		
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.INVISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.INVISIBLE);
		
		Button backButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
		backButton.setOnClickListener(this);
		backButton.setText(getResources().getString(R.string.comment_title));
		backButton.setVisibility(View.VISIBLE);
		
		mRefreshButton.setOnClickListener(this);
		
		if(mRefreshingComment == true){
			mRefreshButton.setVisibility(View.INVISIBLE);
			mRefreshingProgressBar.setVisibility(View.VISIBLE);
		}else{
			mRefreshButton.setVisibility(View.VISIBLE);
			mRefreshingProgressBar.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		NotificationService.getInstance().removeObserver(Constants.NOTIFICATION_SHARE_COMMENT_SUCCESS, shareNotification);
		super.onDestroyView();
	}
	
	@SuppressLint("SdCardPath")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.ib_comment_send:
				saveComment();
				break;
				
			case R.id.ib_menu_refresh:
				if(mRefreshingComment == false){
					refreshComment();
				}
				break;
				
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
				
			case R.id.ib_home_vote:
				mActivity.votePhoto(null, null, mPost, mVoteCountTextView, mVoteButton);
				break;
				
			case R.id.ib_home_share:				
				if(AppManager.isLoggedIn(mActivity) == false){
					Intent intent = new Intent(mActivity, MainLoginActivity.class);
					startActivity(intent);
					return;
				}
				
				mActivity.shareImage(mPost, mPost.getPhotoFile().getUrl(), mUser, this);
				
				break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == Constants.REQUEST_SHARE_ACTION){
//			if(resultCode == Activity.RESULT_OK){
				shareAction();
//			}
		}
	}
	
	public void shareAction(){
		if(AppManager.mIsAlreadyShared == false){
			int shareCount = Integer.parseInt(mShareCountTextView.getText().toString());
			mShareCountTextView.setText(String.valueOf(++shareCount));
			mPost.put("shareCount", shareCount);
	//		mPost.put("totalActionCount", mPost.getTotalActionCount() + 1);
			mPost.saveEventually();
			
			mActivity.saveShareCount(ParseUser.getCurrentUser(), mUser);
			
			mActivity.sendNotification(mPost, Constants.NOTIFICATION_KIND_SHARE);
			AppManager.mIsAlreadyShared = true;
		}
	}
	
	public void saveComment(){
		String strComment = mCommentEditText.getText().toString();
		if(strComment == null || strComment.length()<=0) return;
		if(mSavingComment == true) return;
		
		UtilityMethods.hideSoftInput(mActivity, mCommentEditText);
		
		final ParseUser currentUser = ParseUser.getCurrentUser();
		
		mSavingComment = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		mCommentEditText.setText("");
		
		ParseObject comment = new ParseObject("Comment");
		comment.put("comment", strComment);
		comment.put("commenter", currentUser);
		comment.put("post", mPost);
		comment.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException arg0) {
				// TODO Auto-generated method stub
				if(arg0 == null && getActivity() != null){
					Toast.makeText(mActivity, getResources().getString(R.string.comment_saved_successfully), Toast.LENGTH_SHORT).show();
					refreshComment();
					mActivity.sendNotification(mPost, Constants.NOTIFICATION_KIND_COMMENT);
				}
			}
			
		});
	}
	
	public void refreshComment(){
		if(mRefreshingComment == true) return;
		
		mRefreshingComment = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
		query.whereEqualTo("post", mPost);
		query.include("commenter");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.orderByAscending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> list, ParseException err) {
				// TODO Auto-generated method stub
				if(err == null && getActivity() != null){
					mCommentsList = new ArrayList<CommentsListModel>();
					for(int i=0;i<list.size();i++){
						CommentsListModel model = new CommentsListModel();
						model.setComment(list.get(i).getString("comment"));
						model.setUser((ParseUser)list.get(i).get("commenter"));
						model.setDate(list.get(i).getCreatedAt());
						mCommentsList.add(model);
					}
					mCommentCountTextView.setText(String.valueOf(mCommentsList.size()));
					CommentsListCellAdapter adapter = new CommentsListCellAdapter(CommentFragment.this, mCommentsList);
					mCommentListView.setAdapter(adapter);
					
					//display comment status
			        ParseUser currentUser = ParseUser.getCurrentUser();
					if(AppManager.isLoggedIn(getActivity())== true && currentUser != null){
						 if(mPost.getCommentUserList().contains(currentUser.getObjectId())){
				        	mCommentButton.setSelected(true);
						}else{
							mCommentButton.setSelected(false);
						}
					}
					
					mSavingComment = false;
					mRefreshingComment = false;
					mRefreshingProgressBar.setVisibility(View.INVISIBLE);
					mRefreshButton.setVisibility(View.VISIBLE);
				}
			}
			
		});
	}
	
	public void setCommentListViewHeight(BaseAdapter adapter){
		int totalHeight = 0; 
        for (int i = 0; i < adapter.getCount(); i++) { 
            View listItem = adapter.getView(i, null, mCommentListView); 
            listItem.measure(0, 0); 
            totalHeight += listItem.getMeasuredHeight(); 
        } 
 
        ViewGroup.LayoutParams params = mCommentListView.getLayoutParams(); 
       
        params.height = totalHeight + (mCommentListView.getDividerHeight() * (adapter.getCount() - 1)); ; 
        mCommentListView.setLayoutParams(params); 
	}
	
//	public void voteAction(){
//		
//		if(AppMananger.isLoggedIn(mActivity) == false){
//			Intent intent = new Intent(mActivity, MainLoginActivity.class);
//			startActivity(intent);
//			return;
//		}
//		
//		ParseUser currentUser = ParseUser.getCurrentUser();
//	        
//		if(mVoteButton.isSelected()==false){
//			mVoteButton.setSelected(true);
//			mVoteCountTextView.setText(String.valueOf(++mVotesCount));
//			
//			mPost.addVoteUser(currentUser.getObjectId());
//			mPost.setTotalActionCount(mPost.getTotalActionCount() + 1);
//			mPost.saveEventually();
//
//			mActivity.sendNotification(mPost, Constants.NOTIFICATION_KIND_VOTE);
//			
//		}else{
//			if(mVotesCount<=0) return;
//			
//			voteUsersList.remove(currentUser.getObjectId());
//			mPost.put("voteUsers", voteUsersList);
//			mPost.put("totalActionCount", mPost.getTotalActionCount() - 1);
//			mPost.saveEventually();
//			mVoteButton.setSelected(false);
//			mVoteCountTextView.setText(String.valueOf(--mVotesCount));
//			
//		}
//	}
}
