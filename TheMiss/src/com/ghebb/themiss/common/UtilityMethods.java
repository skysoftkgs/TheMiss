package com.ghebb.themiss.common;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ghebb.themiss.datamodel.MissOfMonthListModel;
import com.ghebb.themiss.datamodel.PostModel;
import com.ghebb.themiss.datamodel.UserInfoModel;
import com.ghebb.themiss.datamodel.UsersListModel;
import com.parse.ParseUser;

public class UtilityMethods {
	
	public static boolean checkEditText(Context context, EditText et, String fieldName){
		if(et.getText() == null || et.getText().length()<=0){
			Toast.makeText(context, fieldName + " can't be empty value", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	public final static boolean isValidEmail(CharSequence target) {
	    if (target == null) {
	    	return false;
	    } else {
	        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	    }
	}
	
	public static void hideSoftInput(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
	
	public static void hideKeyboard(Activity activity) {
	     InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

	     // check if no view has focus:
	     View view = activity.getCurrentFocus();
	     if (view != null) {
	         inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	     }
	}
		
	public static Bitmap getCropedBitmap(Bitmap srcBmp, int width){
		
		System.gc();
		Bitmap dstBmp;
		
		try {
        	if (srcBmp.getWidth() >= srcBmp.getHeight()){
		
				  dstBmp = Bitmap.createBitmap(
				     srcBmp, 
				     srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
				     0,
				     srcBmp.getHeight(), 
				     srcBmp.getHeight()
				     );
		
			}else{
		
				  dstBmp = Bitmap.createBitmap(
				     srcBmp,
				     0, 
				     srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
				     srcBmp.getWidth(),
				     srcBmp.getWidth()
				     );
			}
					
			if(width>0){
				Bitmap bm = Bitmap.createScaledBitmap(dstBmp, width, width, false);
				dstBmp.recycle();
				return bm;
			}
//			else if(srcBmp.getWidth()>Constants.UPLOAD_POST_IMAGE_WIDTH){
//				Bitmap bm = Bitmap.createScaledBitmap(dstBmp, Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_WIDTH, false);
//				dstBmp.recycle();
//				return bm;
//			}
			
//			srcBmp.recycle();
			return dstBmp;
		}
		catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static Bitmap rotateBitmap(Bitmap bitmap, String photoPath) {

		ExifInterface exif;
		int orientation;
	    try {
			exif = new ExifInterface(photoPath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}  
	    
	    try{
	        Matrix matrix = new Matrix();
	        switch (orientation) {
	            case ExifInterface.ORIENTATION_NORMAL:
	                return bitmap;
	            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
	                matrix.setScale(-1, 1);
	                break;
	            case ExifInterface.ORIENTATION_ROTATE_180:
	                matrix.setRotate(180);
	                break;
	            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
	                matrix.setRotate(180);
	                matrix.postScale(-1, 1);
	                break;
	            case ExifInterface.ORIENTATION_TRANSPOSE:
	                matrix.setRotate(90);
	                matrix.postScale(-1, 1);
	                break;
	           case ExifInterface.ORIENTATION_ROTATE_90:
	               matrix.setRotate(90);
	               break;
	           case ExifInterface.ORIENTATION_TRANSVERSE:
	               matrix.setRotate(-90);
	               matrix.postScale(-1, 1);
	               break;
	           case ExifInterface.ORIENTATION_ROTATE_270:
	               matrix.setRotate(-90);
	               break;
	           default:
	               return bitmap;
	        }
	        try {
	            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	            bitmap.recycle();
	            return bmRotated;
	        }
	        catch (OutOfMemoryError e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
				 		
	public static void setListViewHeight(ListView listView, int verticalSpacing){
		ListAdapter listAdapter = listView.getAdapter(); 
		if (listAdapter == null)
			return;
		
		int totalHeight = 0; 
        for (int i = 0; i <listAdapter.getCount(); i++) { 
            View listItem = listAdapter.getView(i, null, listView); 
            listItem.measure(0, 0); 
            totalHeight += listItem.getMeasuredHeight(); 
        } 
 
        ViewGroup.LayoutParams params = listView.getLayoutParams(); 
       
        params.height = totalHeight + verticalSpacing * (listAdapter.getCount()-1);
        listView.setLayoutParams(params);
	}
	
	public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
	
	public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
	}
	
	public static Date getFirstDateOfCurrentMonth() {
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
	public static Date getFirstDateOfPrevMonth() {
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DATE, 1);
		return cal.getTime();
	}
	
	public static Date getFirstDateOfNextMonth() {
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DATE, 1);
		return cal.getTime();
	}
		
	public static boolean containsParseUser(List<ParseUser> userList, ParseUser user){
		if(userList == null || user == null) return false;
		
		for(int i = 0;i<userList.size();i++){
			if(userList.get(i).getObjectId().equals(user.getObjectId()))
				return true;
		}
		
		return false;
	}
	
	public static int getRankingofMonth(List<UserInfoModel> list, ParseUser user){
		
		if(list == null || user == null) return -1;
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getUser() == null) continue;
			
			if(user.getObjectId().equals(list.get(i).getUser().getObjectId()))
				return i;
		}
		return -1;
	}
	
	public static int getIndexOfUser(List<UsersListModel> list, ParseUser user){
		
		if(list == null || user == null) return -1;
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getUser() == null) continue;
			
			if(user.getObjectId().equals(list.get(i).getUser().getObjectId()))
				return i;
		}
		return -1;
	}
	
//	public static boolean containsUser(ArrayList<ParseUser> userList, ParseUser user){
//		
//		if(userList == null || user == null) return false;
//		
//		for(int i = 0;i<userList.size();i++){
//			if(userList.get(i).getObjectId().equalsIgnoreCase(user.getObjectId()) == true)
//				return true;
//		}
//		return false;
//	}
	
	//check if line app installed.
	public static boolean appInstalledOrNot(Context context, String uri)
    {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try
        {
               pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
               app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
               app_installed = false;
        }
        return app_installed ;
	}
	
	public static int getIndexOfMissMonth(PostModel post, List<MissOfMonthListModel> list){
		
		if(post == null || list == null) return -1;
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getPost().getUser() == null) continue;
			
			if(post.getUser().getObjectId().equals(list.get(i).getPost().getUser().getObjectId()))
				return i;
		}
		return -1;
	}
	
	public static List<String> removeUser(List<String> list, ParseUser user){
		Iterator<String> itr = list.iterator();
	    String element;
	    while(itr.hasNext()){

	    	element = (String)itr.next();
	      if(element.equals(user.getObjectId()))
	      {
	        itr.remove();
	      }
	    }
	    return list;
	}
	
	public static String setLocalBitmap(Bitmap bitmap, Context context) {
	    String fileName = "myImage";//no .png or .jpg needed
	    try {
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	        FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
	        fo.write(bytes.toByteArray());
	        // remember close file output
	        fo.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	        fileName = null;
	    }
	    return fileName;
	}
	
	public static Bitmap getLocalBitmap(Context context){
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.openFileInput("myImage"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return bitmap;
	}
	
	public static Bitmap rotateBitmapBy90(Bitmap original){		
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap rotated = Bitmap.createBitmap(original, 0, 0, 
		                              original.getWidth(), original.getHeight(), 
		                              matrix, true);
		
		return rotated;
	}
	
	//get facebook cover photo url
	public static String getCoverPhotoUrl(String facebookId, String accessToken){
		String URL = "https://graph.facebook.com/" + facebookId + "?fields=cover&access_token=" + accessToken;

		String finalCoverPhoto = null;

		try {

		    HttpClient hc = new DefaultHttpClient();
		    HttpGet get = new HttpGet(URL);
		    HttpResponse rp = hc.execute(get);

		    if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		        String result = EntityUtils.toString(rp.getEntity());

		        JSONObject JODetails = new JSONObject(result);

		        if (JODetails.has("cover")) {
		            String getInitialCover = JODetails.getString("cover");

		            if (getInitialCover.equals("null")) {
		                finalCoverPhoto = null;
			        } else {
			            JSONObject JOCover = JODetails.optJSONObject("cover");
	
			            if (JOCover.has("source"))  {
			                finalCoverPhoto = JOCover.getString("source");
			            } else {
			                finalCoverPhoto = null;
			            }
			        }
			    } else {
			        finalCoverPhoto = null;
			    }
		        
		    }
		    
		} catch (Exception e) {
		    // TODO: handle exception
			
		}
		
		return finalCoverPhoto;
	}
}
