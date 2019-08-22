package com.ghebb.themiss.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.FAQFragment;
import com.ghebb.themiss.datamodel.FAQListModel;
 
public class FAQListCellAdapter extends BaseAdapter {
 
    // Declare Variables
	FAQFragment mFragment;
    LayoutInflater mInflater;

    List<String> mFAQTitleList;
    List<String> mFAQDetailList;
    
    List<FAQListModel> mFAQDisplayList = new ArrayList<FAQListModel>();
     
    public FAQListCellAdapter(FAQFragment fragment) {
        mFragment = fragment;
        mInflater = LayoutInflater.from(mFragment.getActivity());
       
        mFAQTitleList = Arrays.asList(mFragment.getResources().getString(R.string.faq_topic1),
            	mFragment.getResources().getString(R.string.faq_topic2),
            	mFragment.getResources().getString(R.string.faq_topic3),
            	mFragment.getResources().getString(R.string.faq_topic4),
            	mFragment.getResources().getString(R.string.faq_topic5),
            	mFragment.getResources().getString(R.string.faq_topic6),
            	mFragment.getResources().getString(R.string.faq_topic7));
            		
       mFAQDetailList = Arrays.asList(mFragment.getResources().getString(R.string.faq_content1),
            	mFragment.getResources().getString(R.string.faq_content2),
            	mFragment.getResources().getString(R.string.faq_content3),
            	mFragment.getResources().getString(R.string.faq_content4),
            	mFragment.getResources().getString(R.string.faq_content5),
            	mFragment.getResources().getString(R.string.faq_content6),
            	mFragment.getResources().getString(R.string.faq_content7));
            
        for(int i=0;i<mFAQTitleList.size();i++){
        	FAQListModel model = new FAQListModel();
        	model.setContent(mFAQTitleList.get(i));
        	model.setSelected(false);
        	mFAQDisplayList.add(model);
        }
    }
 
    public class ViewHolder {
    	TextView contentTextView;
    }
 
    @Override
    public int getCount() {
        return mFAQDisplayList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mFAQDisplayList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_faq, null);
            holder.contentTextView = (TextView) view.findViewById(R.id.tv_faq_item);
            					
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final FAQListModel item = mFAQDisplayList.get(position);
        
        if(item.getSelected() == true)
        	holder.contentTextView.setBackgroundResource(R.drawable.import_menubg);
        else
        	holder.contentTextView.setBackgroundColor(Color.rgb(224, 224, 224));
        
        holder.contentTextView.setText(item.getContent());
        
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				for(int i=0;i<mFAQDisplayList.size();i++){
		        	FAQListModel model = mFAQDisplayList.get(i);
		        	model.setContent(mFAQTitleList.get(i));
		        	model.setSelected(false);
		        }
				item.setContent(mFAQTitleList.get(position) + "\n\n" + mFAQDetailList.get(position));
				item.setSelected(true);
				
				FAQListCellAdapter.this.notifyDataSetChanged();
			}
        	
        });
        
        return view;
    }
}