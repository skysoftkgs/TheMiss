package com.ghebb.themiss.datamodel;

public class MissOfMonthListModel{

	PostModel mPost;
	int mTotalVoteCount;
	
	public int getTotalVoteCount() {
		return mTotalVoteCount;
	}

	public void setTotalVoteCount(int count) {
		mTotalVoteCount = count;
	}

	public PostModel getPost() {
		return mPost;
	}

	public void setPost(PostModel post) {
		mPost = post;
	}
}
