package com.ghebb.themiss.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/*
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given Meal 
 */

@ParseClassName("Post")
public class PostModel extends ParseObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PostModel() {
		// A default constructor is required.
	}

	public int getTotalActionCount() {
		Integer obj = getInt("totalActionCount");
		return obj.intValue();
	}

	public void setTotalActionCount(int count) {
		put("totalActionCount", Integer.valueOf(count));
	}

	public int getShareCount() {
		Integer obj = getInt("shareCount");
		return obj.intValue();
	}

	public void setShareCount(int count) {
		put("shareCount", Integer.valueOf(count));
	}
		
	public int getVoteCountOfSuperUser() {
		Integer obj = getInt("voteCountOfSuperUser");
		return obj.intValue();
	}

	public void setVoteCountOfSuperUser(int count) {
		put("voteCountOfSuperUser", Integer.valueOf(count));
	}
	
	public List<String> getVoteUserList() {
		List<String> voteUserList = getList("voteUsers");
		if(voteUserList == null)
			voteUserList = new ArrayList<String>();
		
		return voteUserList;
	}

	public void setVoteUserList(List<String> voteUsers) {
		put("voteUsers", voteUsers);
	}
	
	public int getVoteCount(){
		return getVoteUserList().size() + getVoteCountOfSuperUser();		
	}
	
	public void addVoteUser(String userId){
		List<String> voteUserList = getVoteUserList();
				
		if(!voteUserList.contains(userId)){
			voteUserList.add(userId);
			setVoteUserList(voteUserList);
		}
	}
	
	public boolean removeVoteUser(ParseUser user){
		List<String> voteUserList = getVoteUserList();
		voteUserList.remove(user.getObjectId());
		setVoteUserList(voteUserList);
		return true;
	}
	
	public List<String> getCommentUserList() {
		List<String> commentUserList = getList("commentUsers");
		if(commentUserList == null)
			commentUserList = new ArrayList<String>();
		
		return commentUserList;
	}
	
	public ParseUser getUser() {
		return getParseUser("user");
	}

	public void setUser(ParseUser user) {
		put("user", user);
	}

	public ParseFile getPhotoFile() {
		return getParseFile("image");
	}

	public void setPhotoFile(ParseFile file) {
		put("image", file);
	}
	
	public ParseFile getThumbnailImageFile() {
		return getParseFile("thumbnail");
	}

	public void setThumbnailImageFile(ParseFile file) {
		put("thumbnail", file);
	}
}
