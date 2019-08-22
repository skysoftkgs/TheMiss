package com.ghebb.fbphotopicker;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gab.themiss.R;
import com.ghebb.themiss.TheMissApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FBAlbumArrayAdapter extends ArrayAdapter<FBAlbum> {

    private Context mContext;
    private LayoutInflater mInflater;
    
    public FBAlbumArrayAdapter(Context context, int textViewResourceId, List<FBAlbum> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_album_list_item, parent, false);
            holder = new AlbumHolder();
            holder.coverPhoto = (ImageView) convertView.findViewById(R.id.imageView_cover_photo);
            holder.name = (TextView) convertView.findViewById(R.id.textView_name);
            holder.photoCount = (TextView) convertView.findViewById(R.id.textView_count);
            convertView.setTag(holder);
        } else {
            holder = (AlbumHolder) convertView.getTag();
        }

        FBAlbum album = getItem(position);
        if (album.getCoverPhoto() != null)
	        // display post image
        	ImageLoader.getInstance().displayImage(album.getCoverPhoto(), holder.coverPhoto, TheMissApplication.getInstance().options, null);

        holder.name.setText(album.getName());

        if (album.getCount() == 1)
            holder.photoCount.setText(album.getCount() + " " + mContext.getString(R.string.photo));
        else
            holder.photoCount.setText(album.getCount() + " " + mContext.getString(R.string.photos));

        return convertView;
    }

    /**
     * Holder pattern to make ListView more efficient.
     * 
     * @author dkadlecek
     * 
     */
    static class AlbumHolder {
        ImageView coverPhoto;
        TextView name;
        TextView photoCount;
    }
}
