package com.ghebb.themiss.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AppManager {
	
	public static List<PostModel> mLastPicturesList = new ArrayList<PostModel>();
	public static List<UserInfoModel> mMissOfMonthList = new ArrayList<UserInfoModel>();
	public static List<UserInfoModel> mWinnerList = new ArrayList<UserInfoModel>();
	
	public static boolean mLoggedIn;
	public static int mScreenWidth;
	public static int mScreenHeight;
	public static boolean mHomeCloseHidden;
	public static boolean mFromImportFragment;
	public static boolean mIsAlreadyShared;
	
	static List<ParseQuery<PostModel>> mQueryList = new ArrayList<ParseQuery<PostModel>>();
	
	public static boolean isLoggedIn(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean(Constants.PREF_LOGGEDIN, false)==false){
			return false;
		}
		return true;
	}
	
	public static boolean isFemale(ParseUser user){
		if(user == null) return false;
		
		if(user.getString("gender") != null && user.getString("gender").equalsIgnoreCase("female"))
			return true;
		else 
			return false;
	}
	
	public static boolean isItalian(ParseUser user){
		if(user == null) return false;
		
		if(user.getString("language") != null && user.getString("language").equalsIgnoreCase("italian"))
			return true;
		else 
			return false;
	}
	
	public static boolean isSuperUser(ParseUser user){
		if(user == null) return false;
		
		if(user.getBoolean("isSuperUser") == true)
			return true;
		else 
			return false;
	}
	
	public static void addQuery(ParseQuery<PostModel> query){
		if(mQueryList.contains(query)) return;
		
		mQueryList.add(query);
	}
	
	public static void removeAllQuery(){
		for(ParseQuery<PostModel> query : mQueryList){
			query.cancel();
		}
	}
}
