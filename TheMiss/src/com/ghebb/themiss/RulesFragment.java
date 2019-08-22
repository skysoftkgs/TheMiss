package com.ghebb.themiss;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gab.themiss.R;

public class RulesFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "RulesFragment";
	MainActivity mActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_rules, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		TextView  prizesTextView = (TextView) view.findViewById(R.id.tv_prizes_prizes);
		prizesTextView.setOnClickListener(this);
		
		TextView  tutorialTextView = (TextView) view.findViewById(R.id.tv_prizes_tutorial);
		tutorialTextView.setOnClickListener(this);

		TextView  privacyTextView = (TextView) view.findViewById(R.id.tv_prizes_privacy);
		privacyTextView.setOnClickListener(this);
		
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
		backButton.setText(getResources().getString(R.string.menu_rules_and_privacy));
		backButton.setVisibility(View.VISIBLE);
		
		ImageButton refreshImageButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		refreshImageButton.setAlpha(50);
		refreshImageButton.setEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
		Fragment newContent = null;
		switch(v.getId()){
			case R.id.tv_prizes_prizes:
				newContent = new PrizesFragment();
				if(newContent != null)
					mActivity.addContent(newContent, PrizesFragment.TAG);
				break;
				
			case R.id.tv_prizes_tutorial:
				newContent = new TutorialFragment();
				if(newContent != null)
					mActivity.addContent(newContent, TutorialFragment.TAG);
				break;
								
			case R.id.tv_prizes_privacy:
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.iubenda.com/privacy-policy/170136"));
				startActivity(intent);
				break;
				
			case R.id.btn_menu_back:
//				mActivity.toggle();
				mActivity.goBack();
				break;
		}
	}
}
