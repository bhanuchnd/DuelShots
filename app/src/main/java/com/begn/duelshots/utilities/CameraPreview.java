package com.begn.duelshots.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    Camera mCamera;
    SurfaceHolder mHolder;
    private List<Camera.Size> mSupportedPreviewSizes;
    public Camera.Size mPreviewSize;
    public Camera.Size mPictureSize;
    public boolean safeToOpenCamera = false;
    private List<Camera.Size> mSupportedPictureSizes;
    private Camera.Parameters mCameraSettings;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);
        }
        if(mSupportedPictureSizes != null) {
            mPictureSize = getOptimalPreviewSize(mCamera.getParameters().getSupportedPictureSizes(),width,height);
        }
    }

    public CameraPreview(Context context, Camera camera, Camera.Parameters cameraSettings) {
        super(context);
        mCamera = camera;
        mCameraSettings = cameraSettings;
        setFocusable(true);
        setFocusableInTouchMode(true);
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCameraSettings.setPreviewSize(mPreviewSize.width,mPreviewSize.height);
            mCameraSettings.setPictureSize(mPictureSize.width,mPictureSize.height);
            mCameraSettings.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            setCamFocusMode();
            mCamera.startPreview();
            safeToOpenCamera = true;
        } catch (IOException e) {
            Log.d("Setting Error", "Error setting camera preview: " + e.getMessage());
        }
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    private void setCamFocusMode(){
        if(null == mCamera) {
            return;
        }
        /* Set Auto focus */
        List<String> focusModes = mCameraSettings.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            mCameraSettings.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            mCameraSettings.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else {
            Log.d("Front","Front Cam");
        }

        mCamera.setParameters(mCameraSettings);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }
        Log.d("Surface","Changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
