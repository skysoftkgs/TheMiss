package com.ghebb.themiss;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.gab.themiss.R;
import com.ghebb.themiss.custom.CameraPreview;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters params= mCamera.getParameters();
        Camera.Size size = getBestPreviewSize(450, 450, params);
        params.setPreviewSize(size.width, size.height);
        params.set("jpeg-quality", 100);
        params.set("rotation", 90);
        params.set("orientation", "portrait");
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setPictureFormat(PixelFormat.JPEG);
                mCamera.setParameters(params);
        
        // set Camera parameters
        mCamera.setParameters(params);
       
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        ImageButton captureImageButton = (ImageButton) findViewById(R.id.imageButton_capture);
        captureImageButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                }
            }
        );
    }
    
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (pictureFile == null){
//                Log.d(TAG, "Error creating media file, check storage permissions: " +
//                    e.getMessage());
//                return;
//            }
//
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
//            }
        }
    };
    
    Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result=null;
        float dr = Float.MAX_VALUE;
        float ratio = (float)width/(float)height;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            float r = (float)size.width/(float)size.height;
            if( Math.abs(r - ratio) < dr && size.width <= width && size.height <= height ) {
                dr = Math.abs(r - ratio);
                result = size;
            }
        }

        return result;
    }
}
