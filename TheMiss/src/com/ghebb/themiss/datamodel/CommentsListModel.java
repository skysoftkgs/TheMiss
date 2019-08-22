package com.ghebb.themiss.datamodel;

import java.util.Date;

import com.parse.ParseUser;

public class CommentsListModel{

	ParseUser mUser;
	String mComment;
	Date mDate;
	
	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}
	
	public ParseUser getUser() {
		return mUser;
	}

	public void setUser(ParseUser user) {
		mUser = user;
	}
}
