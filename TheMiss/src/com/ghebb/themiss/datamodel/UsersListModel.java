package com.ghebb.themiss.datamodel;

import com.parse.ParseUser;

public class UsersListModel{

	ParseUser mUser;
	int mTotalActionCount;
	
	public int getTotalActionCount() {
		return mTotalActionCount;
	}

	public void setTotalActionCount(int count) {
		mTotalActionCount = count;
	}

	public ParseUser getUser() {
		return mUser;
	}

	public void setUser(ParseUser user) {
		mUser = user;
	}
}
