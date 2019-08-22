package com.ghebb.themiss.datamodel;

import java.util.Date;

import com.parse.ParseUser;

public class NotificationListModel{

	ParseUser mFromUser;
	ParseUser mToUser;
	String mDisplayTime;
	String mKind;
	PostModel mPost;
	
	public String getDisplayTime() {
		return mDisplayTime;
	}

	public void setDisplayTime(Date date) {
		Date today = new Date();
		long diff = today.getTime() - date.getTime();
		long seconds = diff / 1000;
		long minutes = (seconds / 60) % 60;
		long hours = (seconds / 3600) % 24;
		long days = (seconds /3600) / 24;
        mDisplayTime = String.format("%dd %dh %dm", days, hours, minutes);
	}

	public String getKind() {
		return mKind;
	}

	public void setKind(String kind) {
		mKind = kind;
	}
	
	public ParseUser getFromUser() {
		return mFromUser;
	}

	public void setFromUser(ParseUser user) {
		mFromUser = user;
	}
	
	public ParseUser getToUser() {
		return mToUser;
	}

	public void setToUser(ParseUser user) {
		mToUser = user;
	}
	
	public PostModel getPost() {
		return mPost;
	}

	public void setPost(PostModel post) {
		mPost = post;
	}
}
