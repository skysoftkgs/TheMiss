package com.ghebb.themiss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.gab.themiss.R;
import com.ghebb.themiss.common.Constants;
import com.parse.ParseUser;

public class InviteFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "InviteFragment";
	
	RelativeLayout mFacebookLayout;
	RelativeLayout mWhatsappLayout;
	RelativeLayout mContactLayout;
	RelativeLayout mInviteWithLayout;
	TextView mFacebookUserNameTextView;
	Button mBackButton;
	ImageButton mMenuRefreshButton;
	ProgressBar mRefreshingProgressBar;
	
	ParseUser mCurrentUser;
	MainActivity mActivity;
	
	private final int PICK_FROM_CONTACT = 1001;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_invite, container, false);
		mActivity = (MainActivity)getActivity();
		mActivity.currentFragment = this.getClass();
		mCurrentUser = ParseUser.getCurrentUser();
		
		mFacebookLayout = (RelativeLayout) view.findViewById(R.id.layout_invite_facebook);
		mFacebookLayout.setOnClickListener(this);
		
		mWhatsappLayout = (RelativeLayout) view.findViewById(R.id.layout_invite_whatsapp);
		mWhatsappLayout.setOnClickListener(this);
		
		mContactLayout = (RelativeLayout) view.findViewById(R.id.layout_invite_contact);
		mContactLayout.setOnClickListener(this);
		
		mInviteWithLayout = (RelativeLayout) view.findViewById(R.id.layout_invite_with);
		mInviteWithLayout.setOnClickListener(this);
		
		mFacebookUserNameTextView = (TextView) view.findViewById(R.id.tv_invite_facebook_username);
		if(mCurrentUser.getString("facebookID") != null && Session.getActiveSession() != null && Session.getActiveSession().isOpened()){
			mFacebookUserNameTextView.setText(getResources().getString(R.string.connected_as) + " " + mCurrentUser.getUsername());
		}
		
		mRefreshingProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_refresh);	
		mMenuRefreshButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		
		view.setOnClickListener(null);
		return view;
	}
	
	@SuppressWarnings("deprecation")
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
		backButton.setText(getResources().getString(R.string.invite_title));
		backButton.setVisibility(View.VISIBLE);
		
		mMenuRefreshButton.setAlpha(50);
		mMenuRefreshButton.setEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.layout_invite_facebook:
				Session session = Session.getActiveSession();
				if(mCurrentUser.getString("facebookID") != null && session != null && session.isOpened() == true)
					inviteToFriends();
				else{
					Intent intent = new Intent(mActivity, SettingsActivity.class);
					intent.putExtra("ScrollToEnd", true);
					startActivity(intent);
				}
				break;
					
			case R.id.layout_invite_whatsapp:
				invite("whatsapp", getResources().getString(R.string.invite_message));
				break;
				
			case R.id.layout_invite_contact:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, PICK_FROM_CONTACT);
				break;
				
			case R.id.layout_invite_with:
				inviteToAll(getResources().getString(R.string.invite_message));
				break;
				
			case R.id.btn_menu_back:
				mActivity.goBack();
				break;
		}
	}

	public void inviteToFriends(){
		Bundle parameters = new Bundle();
        parameters.putString("message", "Send Request");

        WebDialog.Builder builder = new WebDialog.Builder(mActivity, Session.getActiveSession(),
            "apprequests", parameters);

	    builder.setOnCompleteListener(new WebDialog.OnCompleteListener() {
	        @Override
	        public void onComplete(Bundle values, FacebookException error) {
	            if (error != null){
	                if (error instanceof FacebookOperationCanceledException){
	                    Toast.makeText(mActivity, getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
	                }
	                else{
	                    Toast.makeText(mActivity,"Network Error",Toast.LENGTH_SHORT).show();
	                }
	            }
	            else{
	
	                final String requestId = values.getString("request");
	                if (requestId != null) {
	                    Toast.makeText(mActivity,"Request sent",Toast.LENGTH_SHORT).show();
	                }
	                else {
	                    Toast.makeText(mActivity, getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
	                }
	            }
	        }
	    });
	
	    WebDialog webDialog = builder.build();
	    webDialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
			case PICK_FROM_CONTACT:
				if(resultCode == Activity.RESULT_OK){
					
					if(data != null){
						Uri uri = data.getData();
						
						if(uri != null){
							Cursor c = null;
							try{
								c= mActivity.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
										ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);
								
								if(c != null && c.moveToFirst()){
									String number = c.getString(0);
//									int type = c.getInt(1);
									sendSMS(number);
								}
							}finally{
								if(c!=null){
									c.close();
								}
							}
						}
					}
				}
		}
	}

	public void sendSMS(String number){
		try{
			String messageBody = getResources().getString(R.string.invite_message);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("sms:" + number));
			intent.putExtra("sms_body", messageBody);
			startActivity(intent);
		}catch(Exception e)
		{
			Toast.makeText(mActivity, "You can't send sms message now.", Toast.LENGTH_LONG).show();
		}
	}
	
    public void invite(String nameApp, String message) {

		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
		try {
		    f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		    fo.close();
		} catch (IOException e) {                       
		        e.printStackTrace();
		}
		
    	try
    	{
    		List<Intent> targetedShareIntents = new ArrayList<Intent>();
    		Intent share = new Intent(android.content.Intent.ACTION_SEND);
    	    share.setType("image/*");
    	    List<ResolveInfo> resInfo = mActivity.getPackageManager().queryIntentActivities(share, 0);
    	    if (!resInfo.isEmpty()){
	    	    for (ResolveInfo info : resInfo) {
	    	        Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
	    	        targetedShare.setType("image/*"); // put here your mime type
	    	        if (info.activityInfo.packageName.toLowerCase().contains(nameApp) || info.activityInfo.name.toLowerCase().contains(nameApp)) {
	    	            targetedShare.putExtra(Intent.EXTRA_SUBJECT, "Sample Photo");
	    	            targetedShare.putExtra(Intent.EXTRA_TEXT, message);
	    	            targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
	    	            targetedShare.setPackage(info.activityInfo.packageName);
	    	            targetedShareIntents.add(targetedShare);
	    	        }
	    	    }
	    	    Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Invite friends");
	    	    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
	    	    startActivityForResult(chooserIntent, Constants.REQUEST_INVITE_ACTION);
    	    }
    	}catch(Exception e){
    	      Log.v("VM","Exception while sending image on" + nameApp + " "+  e.getMessage());
    	      TheMissApplication.getInstance().showErrorDialog(mActivity, "Not installed "+nameApp);
    	}		
    }
    
    public void inviteToAll(String message) {
    	Bitmap icon =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
		try {
		    f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		    fo.close();
		} catch (IOException e) {                       
		        e.printStackTrace();
		}
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
		share.putExtra(Intent.EXTRA_SUBJECT, "TheMiss subject");
		share.putExtra(Intent.EXTRA_TEXT, message);
		startActivityForResult(Intent.createChooser(share, "Invite friends"), Constants.REQUEST_INVITE_ACTION);
    }
}
