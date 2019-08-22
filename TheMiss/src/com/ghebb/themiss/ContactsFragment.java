package com.ghebb.themiss;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.gab.themiss.R;

public class ContactsFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "ContactsFragment";
	final String contactEmail1 = "info@themiss.tv";
	final String contactEmail2 = "themissofficial@mail.com";
	
	MainActivity mActivity;
	
	EditText mNameEditText;
	EditText mMessageEditText;
	Spinner mPlatformSpinner;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_contacts, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		mPlatformSpinner = (Spinner) view.findViewById(R.id.spinner_contacts_platform);		
		ArrayAdapter<String> adapter_platform = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, 
				Arrays.asList(getResources().getString(R.string.report_problem), 
							getResources().getString(R.string.send_suggestion),
							getResources().getString(R.string.business_press)));
		mPlatformSpinner.setAdapter(adapter_platform);
		mPlatformSpinner.setSelection(0);
			
		mNameEditText = (EditText) view.findViewById(R.id.et_contacts_name);
		mMessageEditText = (EditText) view.findViewById(R.id.et_contacts_message);
		
		Button sendButton = (Button) view.findViewById(R.id.btn_contacts_send);
		sendButton.setOnClickListener(this);
		
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
		backButton.setText(getResources().getString(R.string.contacts));
		backButton.setVisibility(View.VISIBLE);
		
		ImageButton refreshImageButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		refreshImageButton.setAlpha(50);
		refreshImageButton.setEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
		switch(v.getId()){
			case R.id.btn_contacts_send:
				
				if(mMessageEditText.getText() == null || mMessageEditText.getText().length()<=0)
					return;
				
				Intent email = new Intent(Intent.ACTION_SEND);
				email.putExtra(Intent.EXTRA_EMAIL, new String[]{contactEmail1, contactEmail2});		  
				email.putExtra(Intent.EXTRA_SUBJECT, mPlatformSpinner.getSelectedItem().toString());
				email.putExtra(Intent.EXTRA_TEXT, mMessageEditText.getText().toString());
				email.setType("message/rfc822");
				startActivity(Intent.createChooser(email, "Choose an Email client :"));
				break;
								
			case R.id.btn_menu_back:
				mActivity.goBack();
//				mActivity.toggle();
				break;
		}
	}
}
