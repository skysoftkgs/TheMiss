package com.ghebb.themiss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.gab.themiss.R;
import com.ghebb.themiss.adapter.FAQListCellAdapter;

public class FAQFragment extends Fragment implements OnClickListener{
	
	public static final String TAG = "FAQFragment";

	MainActivity mActivity;
	
	ListView mFAQListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_faq, container, false);
		mActivity = (MainActivity) getActivity();
		mActivity.currentFragment = this.getClass();
		
		mFAQListView = (ListView) view.findViewById(R.id.lv_faq);
		FAQListCellAdapter adapter = new FAQListCellAdapter(FAQFragment.this);
		mFAQListView.setAdapter(adapter);
		
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
		backButton.setText(getResources().getString(R.string.faq_faq));
		backButton.setVisibility(View.VISIBLE);
		
		ImageButton refreshImageButton = (ImageButton) getActivity().findViewById(R.id.ib_menu_refresh);
		refreshImageButton.setAlpha(50);
		refreshImageButton.setEnabled(false);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
		switch(v.getId()){
			case R.id.btn_menu_back:
				mActivity.goBack();
//				mActivity.toggle();
				break;
		}
	}
}
