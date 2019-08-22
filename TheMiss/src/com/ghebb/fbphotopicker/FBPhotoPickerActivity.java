package com.ghebb.fbphotopicker;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.gab.themiss.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;


@SuppressLint("NewApi")
public class FBPhotoPickerActivity extends Activity {

    public static final String PHOTO_ID = "photoId";
    public static final String PHOTO_URL = "photoUrl";

    public static String TAG = FBPhotoPickerActivity.class.getSimpleName();

    private Context mContext;
    private Boolean mPhotoGridVisible = false;

    private ListView mAlbumsList;
    private GridView mPhotosGrid;
    private Button mBack;
    private List<FBPhoto> mPhotos;
    private LinearLayout mProgressOverlay;
    private FBPhotoArrayAdapter mPhotoAdapter;
    
    private int mScreenWidth;
    private String mAlbumId;
    private int mCurrentPage = 1;
    private boolean mLoadingMore;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_fb_photo_picker);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            if (getActionBar() != null)
                getActionBar().setTitle(R.string.activity_title);

        mContext = this;
        
        DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;
				
        mAlbumsList = (ListView) findViewById(R.id.listView_albums);
        mPhotosGrid = (GridView) findViewById(R.id.gridView_photos);
        mPhotosGrid.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        mBack = (Button) findViewById(R.id.btn_back);
        mBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goBack();
			}
		});
        mProgressOverlay = (LinearLayout) findViewById(R.id.progress_overlay);
        mProgressOverlay.setVisibility(View.VISIBLE);

        Bundle params = new Bundle();
        params.putString(Constants.FB_FIELDS_PARAM, Constants.FB_ALBUM_FIELDS);
        new Request(Session.getActiveSession(), "me", params, HttpMethod.GET, new Callback() {

            @Override
            public void onCompleted(Response response) {

                if (response.getGraphObject() != null) {
                    JSONObject json = response.getGraphObject().getInnerJSONObject();

                    final List<FBAlbum> fbAlbums = new ArrayList<FBAlbum>();

                    try {
                        JSONArray jsonFBAlbums = json.getJSONObject("albums").getJSONArray("data");

                        for (int i = 0; i < jsonFBAlbums.length(); i++) {

                            JSONObject jsonAlbum = jsonFBAlbums.getJSONObject(i);

                            if (!jsonAlbum.has("photos"))
                                continue;

                            FBAlbum fbAlbum = new FBAlbum();
                            fbAlbum.setId(jsonAlbum.getString("id"));
                            fbAlbum.setName(jsonAlbum.getString("name"));
                            String coverImageId = jsonAlbum.getString("cover_photo");
                            fbAlbum.setCount(jsonAlbum.getInt("count"));

                            JSONArray jsonPhotos = jsonAlbum.getJSONObject("photos").getJSONArray(
                                    "data");

                            for (int j = 0; j < jsonPhotos.length(); j++) {
                                JSONObject jsonFBPhoto = jsonPhotos.getJSONObject(j);
                                FBPhoto fbPhoto = new FBPhoto();
                                fbPhoto.setId(jsonFBPhoto.getString("id"));
                                fbPhoto.setUrl(jsonFBPhoto.getString("picture"));
                                fbPhoto.setSource(jsonFBPhoto.getString("source"));
                                if (fbPhoto.getId().equals(coverImageId)) {
                                    fbAlbum.setCoverPhoto(fbPhoto.getUrl());
                                }
                                fbAlbum.getPhotos().add(fbPhoto);
                            }

                            fbAlbums.add(fbAlbum);
                        }

                        mAlbumsList.setAdapter(new FBAlbumArrayAdapter(mContext, 0, fbAlbums));
                        mAlbumsList.setOnItemClickListener(new OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                            	mCurrentPage = 1;
                            	mAlbumId = fbAlbums.get(position).getId();
                            	mPhotos = new ArrayList<FBPhoto>();
                                mPhotos.addAll(fbAlbums.get(position).getPhotos());
                                mPhotoAdapter = new FBPhotoArrayAdapter(mContext, 0, mPhotos, (mScreenWidth - 4)/3); 
                                mPhotosGrid.setAdapter(mPhotoAdapter);
                                // TODO: check for API Level before animating
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
                                    mAlbumsList.animate().x(-mAlbumsList.getWidth());
                                else
                                    mAlbumsList.setVisibility(View.GONE);
                                mPhotoGridVisible = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                    if (getActionBar() != null)
                                        getActionBar().setTitle(fbAlbums.get(position).getName());
                            }

                        });

                        mPhotosGrid.setOnItemClickListener(new OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                                Intent resultData = new Intent();

                                resultData.putExtra(PHOTO_ID, mPhotos.get(position).getId());
                                resultData.putExtra(PHOTO_URL, mPhotos.get(position).getSource());
                                setResult(Activity.RESULT_OK, resultData);
                                finish();
                            }

                        });
                        
                        mPhotosGrid.setOnScrollListener(new OnScrollListener() {

                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {

                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem,
                                    int visibleItemCount, int totalItemCount) {
                                int lastInScreen = firstVisibleItem + visibleItemCount;
                                if ((lastInScreen == mCurrentPage * 25) && !(mLoadingMore)) {

                                    if (lastInScreen > 0) {
                                        // FETCH THE NEXT BATCH OF FEEDS
                                        loadMorePhots();
                                    }

                                }
                            }
                        });
                        
                        mProgressOverlay.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        mProgressOverlay.setVisibility(View.GONE);
                    }
                }

            }
        }).executeAsync();

        super.onCreate(savedInstanceState);
    }

    public void loadMorePhots(){
    	mLoadingMore = true;
    	mProgressOverlay.setVisibility(View.VISIBLE);
    	Bundle params = new Bundle();
    	params.putInt("offset", mCurrentPage * 25);
    	params.putInt("limit", 25);
    	String graphPath = mAlbumId + "/photos";
    	new Request(
    		    Session.getActiveSession(),
    		    graphPath,
    		    params,
    		    HttpMethod.GET,
    		    new Request.Callback() {
    		        public void onCompleted(Response response) {
    		        	mLoadingMore = false;
    		        	mCurrentPage++;
    		        	if (response.getGraphObject() != null) {
    		        		
    	                    JSONObject json = response.getGraphObject().getInnerJSONObject();
    	                    JSONArray jsonPhotos;
							try {
								jsonPhotos = json.getJSONArray(
								        "data");
								 for (int j = 0; j < jsonPhotos.length(); j++) {
	                                JSONObject jsonFBPhoto = jsonPhotos.getJSONObject(j);
	                                FBPhoto fbPhoto = new FBPhoto();
	                                fbPhoto.setId(jsonFBPhoto.getString("id"));
	                                fbPhoto.setUrl(jsonFBPhoto.getString("picture"));
	                                fbPhoto.setSource(jsonFBPhoto.getString("source"));
	                               
	                                mPhotos.add(fbPhoto);
	                            }
								 
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							mPhotoAdapter.notifyDataSetChanged();
    	           
    		        	}
    		        	
    		        	mProgressOverlay.setVisibility(View.GONE);
    		        }
    		    }
    		).executeAsync();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("NewApi")
    private void goBack() {
        if (mPhotoGridVisible) {
            mPhotosGrid.setAdapter(null);
            // TODO: check for API Level before animating
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                mAlbumsList.animate().x(0);
            else
                mAlbumsList.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                if (getActionBar() != null)
                    getActionBar().setTitle(R.string.activity_title);

            mPhotoGridVisible = false;
            
        } else {
            finish();
        }
    }
}
