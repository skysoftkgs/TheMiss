package com.ghebb.themiss.datamodel;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("UserInfo")
public class UserInfoModel extends ParseObject{

	public int getVoteCount() {
		Integer obj = getInt("voteCount");
		return obj.intValue();
	}

	public void setVoteCount(int count) {
		put("voteCount", Integer.valueOf(count));
	}

	public int getShareCount() {
		Integer obj = getInt("shareCount");
		return obj.intValue();
	}

	public void setShareCount(int count) {
		put("shareCount", Integer.valueOf(count));
	}
	
	public PostModel getLastPost() {
		PostModel obj = (PostModel) getParseObject("lastPost");
		return obj;
	}

	public void setLastPost(PostModel lastPost) {
		put("lastPost", lastPost);
	}
	
//	public Date getLastPostDate() {
//		Date obj = (Date) getDate("lastPostDate");
//		return obj;
//	}
//
//	public void setLastPostDate(Date lastPostDate) {
//		put("lastPostDate", lastPostDate);
//	}
	
	public ParseUser getUser() {
		ParseUser obj = (ParseUser) getParseUser("user");
		return obj;
	}

	public void setUser(ParseUser user) {
		put("user", user);
	}
	
	public String getPostMonth() {
		String obj = getString("postMonth");
		return obj;
	}

	public void setPostMonth(String month) {
		put("postMonth", month);
	}
	
	public String getUserName() {
		String obj = getString("userName");
		return obj;
	}

	public void setUserName(String userName) {
		put("userName", userName);
	}
}
