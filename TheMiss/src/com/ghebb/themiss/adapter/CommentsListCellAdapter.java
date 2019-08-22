package com.ghebb.themiss.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.CommentFragment;
import com.ghebb.themiss.TheMissApplication;
import com.ghebb.themiss.datamodel.CommentsListModel;
import com.parse.ParseUser;
 
public class CommentsListCellAdapter extends BaseAdapter {
 
    // Declare Variables
    CommentFragment mFragment;
    LayoutInflater mInflater;
    List<CommentsListModel> mCommentsList = new ArrayList<CommentsListModel>();
   
    
    public CommentsListCellAdapter(CommentFragment fragment,
            List<CommentsListModel> commentsList) {
        mFragment = fragment;
        mCommentsList = commentsList;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }
 
    public class ViewHolder {
    	TextView commentTextView;
    	TextView userNameTextView;
    	TextView dateTextView;
		ImageView profilePictureImageView;
    }
 
    @Override
    public int getCount() {
        return mCommentsList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mCommentsList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint("SimpleDateFormat")
	public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_comment, null);
            holder.userNameTextView = (TextView) view.findViewById(R.id.tv_comment_username);
            holder.commentTextView = (TextView) view.findViewById(R.id.tv_comment_content);
            holder.dateTextView = (TextView) view.findViewById(R.id.tv_comment_date);
            holder.profilePictureImageView = (ImageView) view.findViewById(R.id.iv_comment_profile_photo);
					
//          holder.commentTextView.measure(0, 0);
//          ViewGroup.LayoutParams params = holder.commentTextView.getLayoutParams();
//          params.height = holder.commentTextView.getLayout().getHeight();
//          holder.commentTextView.setLayoutParams(params);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final ParseUser user = mCommentsList.get(position).getUser();
              
        //display user name
        holder.userNameTextView.setText(user.getUsername());
        
		//display comment
        holder.commentTextView.setText(mCommentsList.get(position).getComment());
        
        //display comment time.
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM HH:mm");
		holder.dateTextView.setText(formatter.format(mCommentsList.get(position).getDate()));
		
        //display profile image.
		TheMissApplication.getInstance().displayUserProfileImage(user, holder.profilePictureImageView);
		    
        return view;
    }
}