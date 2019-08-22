package com.ghebb.themiss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.PrizesListCellAdapter;
import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.UtilityMethods;
import com.ghebb.themiss.imageutil.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PrizesFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "PrizesFragment";
	private final int PICK_FROM_CAMERA = 1001;
	private final int PICK_FROM_FILE = 1002;
	
	MainActivity mActivity;

	ListView mListView;
	Button mAddPhotoButton;
	public ImageButton mRefreshButton;
	public ProgressBar mRefreshingProgressBar;
	boolean mRefreshing;
	String mPhotoOptionChosen;
	ImageLoader mImageLoader;
	public ProgressDialog mProgressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_prizes, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		mImageLoader = new ImageLoader(mActivity);
		View footer = View.inflate(mActivity, R.layout.footer_prizes, null);
		mListView = (ListView) view.findViewById(R.id.lv_prizes);
		mListView.addFooterView(footer);
		mListView.setOnScrollListener(new PauseOnScrollListener(com.nostra13.universalimageloader.core.ImageLoader.getInstance(), true, true));
		
		TextView  tutorialTextView = (TextView) footer.findViewById(R.id.tv_prizes_tutorial);
		tutorialTextView.setOnClickListener(this);
		
		TextView  rulesTextView = (TextView) footer.findViewById(R.id.tv_prizes_rules);
		rulesTextView.setOnClickListener(this);
		
		mAddPhotoButton = (Button) view.findViewById(R.id.btn_prizes_add_photo);
		mAddPhotoButton.setOnClickListener(this);
		
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);
		mRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		
		refreshPrizes();
		
		view.setOnClickListener(null);
		return view;
	}
		
	@Override
	public void onResume() {
		super.onResume();
		
		if(mActivity.currentFragment != null && !this.getClass().equals(mActivity.currentFragment)) return;
		
		ImageButton menuNavigationButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_nav);
		menuNavigationButton.setVisibility(View.INVISIBLE);
		
		ImageView logoImageView = (ImageView) getActivity().findViewById(R.id.iv_menu_themiss);
		logoImageView.setVisibility(View.INVISIBLE);
		
		Button backButton = (Button) getActivity().findViewById(R.id.btn_menu_back);
		backButton.setOnClickListener(this);
		backButton.setText(getResources().getString(R.string.prizes));
		backButton.setVisibility(View.VISIBLE);
		
		if(mRefreshing == true){
			mRefreshButton.setVisibility(View.INVISIBLE);
			mRefreshingProgressBar.setVisibility(View.VISIBLE);
		}else{
			mRefreshButton.setVisibility(View.VISIBLE);
			mRefreshingProgressBar.setVisibility(View.INVISIBLE);
		}
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if(currentUser != null && currentUser.getBoolean("admin")==true){
			mAddPhotoButton.setVisibility(View.VISIBLE);
		}else{
			mAddPhotoButton.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
		Fragment newContent = null;
		switch(v.getId()){
			case R.id.tv_prizes_tutorial:
				newContent = new TutorialFragment();
				if(newContent != null)
					mActivity.addContent(newContent, TutorialFragment.TAG);
				break;
								
			case R.id.tv_prizes_rules:
				newContent = new RulesFragment();
				mActivity.addContent(newContent, RulesFragment.TAG);
				break;
				
			case R.id.btn_prizes_add_photo:
				selectImageFromCameraOrGallery();
				break;
				
			case R.id.btn_menu_back:
				mActivity.goBack();
//				mActivity.toggle();
				break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 
	    super.onActivityResult(requestCode, resultCode, data);
	    System.out.println(" ****** R code"+requestCode+" res code"+resultCode+"data"+data);
	    
	    Bitmap bm = null;
	    System.gc();
	    
	    switch (requestCode) {
		case PICK_FROM_CAMERA:
			if(resultCode == Activity.RESULT_OK)
		    {
				File f = new File(Environment.getExternalStorageDirectory().toString());
				for(File temp : f.listFiles()){
					if(temp.getName().equals("temp.jpg")){
						f = temp;
						break;
					}
				}
                
				try{
					bm = mImageLoader.decodeFile(f, Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_HEIGHT);
					if(bm != null){
						uploadImage(UtilityMethods.rotateBitmap(bm, f.getAbsolutePath()),60);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
		    }
			break;
		case PICK_FROM_FILE:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = mActivity.getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String photoPath = cursor.getString(columnIndex);
				
				bm = mImageLoader.decodeFile(new File(photoPath), Constants.UPLOAD_POST_IMAGE_WIDTH, Constants.UPLOAD_POST_IMAGE_HEIGHT);
				if(bm != null){
					uploadImage(UtilityMethods.rotateBitmap(bm, photoPath),60);
				}
				cursor.close();				
			}
			break;
	    }
	}
			
	public void refreshPrizes(){
		if(mRefreshing == true) return;
		
		mRefreshing = true;
		mRefreshingProgressBar.setVisibility(View.VISIBLE);
		mRefreshButton.setVisibility(View.INVISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Prizes");
		query.setLimit(Constants.PARSE_QUERY_MAX_LIMIT_COUNT);
		query.orderByAscending("createdAt");
		
		query.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> list, ParseException arg1) {
				// TODO Auto-generated method stub
				if(arg1 == null && getActivity() != null){
					PrizesListCellAdapter adapter= new PrizesListCellAdapter(PrizesFragment.this, list);
					mListView.setAdapter(adapter);
					
					mRefreshing = false;
					mRefreshingProgressBar.setVisibility(View.INVISIBLE);
					mRefreshButton.setVisibility(View.VISIBLE);
					if(mProgressDialog!=null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
				}
				
			}			
		});
	}
	
	public void selectImageFromCameraOrGallery(){
		AlertDialog.Builder buildSingle = new AlertDialog.Builder(mActivity);
		buildSingle.setTitle(getResources().getString(R.string.upload_photo));
		final ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.select_dialog_item);
		aAdapter.add(getResources().getString(R.string.take_picture));
		aAdapter.add(getResources().getString(R.string.gallery));

		buildSingle.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		buildSingle.setAdapter(aAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mPhotoOptionChosen = aAdapter.getItem(which)
								.toString();
						System.gc();
						if (mPhotoOptionChosen.equals(getResources().getString(R.string.take_picture))) {
							
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");							
				            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				            PrizesFragment.this.startActivityForResult(intent, PICK_FROM_CAMERA);
				            
						} else if (mPhotoOptionChosen.equals(getResources().getString(R.string.gallery))) {
							Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							intent.setType("image/*");
//							intent.setAction(Intent.ACTION_GET_CONTENT);
							PrizesFragment.this.startActivityForResult(Intent.createChooser(
									intent, "Select File"),
									PICK_FROM_FILE);// one can be replced
													// with any action code
						}
					}
				});
		buildSingle.show();
	}
	
	public void uploadImage(Bitmap bm, int compressRate){
		if(bm == null) return;
		
		mProgressDialog = ProgressDialog.show(mActivity, "", getResources().getString(R.string.uploading), true);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] byteArray = null;
		try {
			Bitmap cropBmp = UtilityMethods.getCropedBitmap(bm, 0);
			if(cropBmp == null) return;
			
			cropBmp.compress(Bitmap.CompressFormat.JPEG, compressRate, stream);
			byteArray = stream.toByteArray();
			stream.flush();
			stream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ParseObject prize = new ParseObject("Prizes");
		ParseFile imageFile = new ParseFile("image.jpg", byteArray);
		prize.put("image", imageFile);
		prize.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				// TODO Auto-generated method stub
				try{
					if (e == null && getActivity() != null) {
						Toast.makeText(mActivity,
								getResources().getString(R.string.image_uploaded_success),
								Toast.LENGTH_SHORT).show();
						
						refreshPrizes();
						
					} else {
					 	Toast.makeText(mActivity,
								"Error saving: " + e.getMessage(),
								 Toast.LENGTH_LONG).show();
					}
					
				}catch(Exception excetption){
					excetption.printStackTrace();
				}
				
				if(mProgressDialog != null)
					mProgressDialog.dismiss();
			}
		});
		
	}
}
