package com.ghebb.themiss.imageutil;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gab.themiss.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;


public class ImageLoader {
 
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    // Handler to display images in UI thread
    Handler handler = new Handler();
    
    int imageWidth;
    int imageHeight;
    
    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }
 
    final int stub_id = R.drawable.white_bg;
 
    public void DisplayImage(String url, ImageView imageView, int width, int height, ProgressBar progressBar) {
    	
//    	if(width != 0 && height != 0){
//    		imageWidth = width;
//    		imageHeight = height;
//    	}else{
//    		imageWidth = 100;
//    		imageHeight = 100;
//    	}
    	
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null){
        	imageView.setImageBitmap(bitmap);
        	if(progressBar != null)
        		progressBar.setVisibility(View.INVISIBLE);
        }
            
        else {
            queuePhoto(url, imageView, progressBar);
//            imageView.setImageResource(stub_id);
            if(progressBar != null)
        		progressBar.setVisibility(View.VISIBLE);
        }
    }
 
    private void queuePhoto(String url, ImageView imageView, ProgressBar progressBar) {
        PhotoToLoad p = new PhotoToLoad(url, imageView, progressBar);
        executorService.submit(new PhotosLoader(p));
    }
 
    public Bitmap getBitmap(String url, int reqWidth, int reqHeight) {
        File f = fileCache.getFile(url);
 
        Bitmap b = decodeFile(f, reqWidth, reqHeight);
        if (b != null)
            return b;
 
        // Download Images from the Internet
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f, reqWidth, reqHeight);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }
 
    // Decodes image and scales it to reduce memory consumption
    public Bitmap decodeFile(File f, int reqWidth, int reqHeight) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
 
            // Find the correct scale value. It should be the power of 2.
//            final int REQUIRED_SIZE = 400;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            
            if(reqWidth > 0 && reqHeight >0){
	            while (true) {
	                if (width_tmp / 2 < reqWidth
	                        || height_tmp / 2 < reqHeight)
	                    break;
	                width_tmp /= 2;
	                height_tmp /= 2;
	                scale *= 2;
	            }
            }
            
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o2.inInputShareable = true;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            o2.inJustDecodeBounds = false;
            
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
        	e.printStackTrace();
        }
        return null;
    }
 
    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public ProgressBar progressBar;
        
        public PhotoToLoad(String u, ImageView i, ProgressBar p) {
            url = u;
            imageView = i;
            progressBar = p;
        }
    }
 
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
 
        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }
 
        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url, imageWidth, imageHeight);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
 
    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
 
    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
 
        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }
 
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null){
                photoToLoad.imageView.setImageBitmap(bitmap);
                if(photoToLoad.progressBar != null) photoToLoad.progressBar.setVisibility(View.INVISIBLE);
            }else
//                photoToLoad.imageView.setImageResource(stub_id);
            	if(photoToLoad.progressBar != null) photoToLoad.progressBar.setVisibility(View.VISIBLE);
        }
    }
 
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
 
}