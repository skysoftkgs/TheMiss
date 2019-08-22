package com.ghebb.themiss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.SelectPhotoCellAdapter;
import com.ghebb.themiss.common.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.parse.ParseUser;

public class InstagramPhotoPickerActivity extends Activity{

	public static final String TAG = "InstagramPhotoPickerActivity";

	final int GRID_VERTICAL_SPACING = 2;
	List<String> mInstagramPhotosList;
	SelectPhotoCellAdapter mAdapter;
	
	ProgressDialog mProgressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_photo);
		
		Button backButton = (Button) this.findViewById(R.id.btn_select_photo_back);
		backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				InstagramPhotoPickerActivity.this.onBackPressed();
			}
			
		});
		mInstagramPhotosList = new ArrayList<String>();
		GridView gridView = (GridView) this.findViewById(R.id.gridView1);
		gridView.setNumColumns(3);
		gridView.setVerticalScrollBarEnabled(true);
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		mAdapter = new SelectPhotoCellAdapter(this, mInstagramPhotosList);
		gridView.setAdapter(mAdapter);
		
		new FetchPhotosActivity().execute();
	}
	
	private class FetchPhotosActivity extends AsyncTask<Void, String, Boolean> {
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(InstagramPhotoPickerActivity.this, "", "Loading...", true);
		}

		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			mAdapter.notifyDataSetChanged();
			
		}

		protected Boolean doInBackground(Void... voids) {
			
			try{
				ParseUser currentUser = ParseUser.getCurrentUser();
				URL url = new URL("https://api.instagram.com/v1/users/"
						+ currentUser.getString("instagramID")
						+ "/media/recent?client_id="
	                    + Constants.INSTAGRAM_CLIENT_ID
	                    + "&count=50");
	
				
				
				do{
					URLConnection tc = url.openConnection();
				    BufferedReader in = new BufferedReader(new InputStreamReader(
				            tc.getInputStream()));
				
				    String line;
				    
				    while ((line = in.readLine()) != null) {
				        JSONObject ob = new JSONObject(line);
				        JSONArray object = ob.getJSONArray("data");
				
				        for (int i = 0; i < object.length(); i++) {
							
				            JSONObject jo = (JSONObject) object.get(i);
				            JSONObject images = (JSONObject) jo.getJSONObject("images");
				            JSONObject standard_resolution = (JSONObject) images.getJSONObject("standard_resolution");
					
				            Log.e("image:", images.toString());
				            Log.i(TAG, "" + standard_resolution.getString("url"));
				            mInstagramPhotosList.add(standard_resolution.getString("url"));
				        }
				        
				        JSONObject pagination = ob.getJSONObject("pagination");
				        if(pagination != null)
				        	url = new URL(pagination.getString("next_url"));
				        else
				        	url = null;
				    }
				}while(url != null);
		        

			} catch (MalformedURLException e) {
			
			    e.printStackTrace();
			} catch (IOException e) {
			
			    e.printStackTrace();
			} catch (JSONException e) {
			
			    e.printStackTrace();
			}
	
			return true;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}
}
