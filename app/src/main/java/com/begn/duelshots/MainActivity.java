package com.begn.duelshots;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.support.media.ExifInterface;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.begn.duelshots.utilities.CameraPreview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.Parameters mCameraSettings;
    private static final int CAMERA_FACING_BACK = 0;
    private static final int CAMERA_FACING_FRONT = 1;
    private static int TYPE = 1;
    private static final int PERMISSION_MULTIPLE_REQUEST = 100;
    Bitmap frontImageBitmap;
    Bitmap backImageBitmap;
    PopupWindow selectCamera;
    FrameLayout firstPreview, secondPreview;
    ImageButton captureButton;
    Button frontCam, rearCam;
    public static Bitmap mergedBitmap;


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_MULTIPLE_REQUEST:
                if (grantResults.length > 0 ) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean externalStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraPermission && externalStoragePermission)
                        mainLogic();
                    else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                showMessageOKCancel("You need to allow access permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions();
                                                }
                                            }
                                        });
                            }
                        }

                    }
                }
                break;
        }

    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.AppTheme_NoActionBar);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(checkPermission()) {
            mainLogic();
        } else {
            requestPermissions();
        }


    }
    private boolean checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
    private void mainLogic() {
        captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupView = inflater.inflate(R.layout.switch_camera,null);
                        frontCam = popupView.findViewById(R.id.front);
                        rearCam = popupView.findViewById(R.id.rear);
                        selectCamera = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        selectCamera.showAsDropDown(captureButton,0,0);
                        selectCamera.setOutsideTouchable(true);
                        selectCamera.setFocusable(true);
                        selectCamera.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        frontCam.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TYPE = 1;
                                selectCamera.dismiss();
                                getCamera(TYPE,true);
                                captureImages(TYPE);
                            }
                        });
                        rearCam.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectCamera.dismiss();
                                TYPE = 0;
                                getCamera(TYPE,true);
                                captureImages(TYPE);
                            }
                        });

//
                    }
                }
        );
    }
    private void captureImages(final int type) {
        final LinearLayout timerLayout = findViewById(R.id.timer_layout);
        final TextView countDown = findViewById(R.id.count_down);
        timerLayout.setVisibility(View.VISIBLE);
        captureButton.setEnabled(false);
        captureButton.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(shutterCallback, rawCallback, mPicture);

            }
        },2000);
        countDown.setText("5");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(5000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        countDown.setText(millisUntilFinished /1000 + "");
                    }

                    @Override
                    public void onFinish() {
                        timerLayout.setVisibility(View.INVISIBLE);
                    }
                }.start();

            }
        },0);

    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_MULTIPLE_REQUEST);
    }
    private void getCamera(int type,boolean isFirst) {
        releaseCamera();
        if(type == CAMERA_FACING_FRONT) {
            mCamera = getCameraInstance(CAMERA_FACING_FRONT);
            mCamera.setDisplayOrientation(setCameraDisplayOrientation(this,CAMERA_FACING_FRONT));
            mCameraSettings = mCamera.getParameters();
            mCameraSettings.setRotation(270);
            mPreview = new CameraPreview(this, mCamera,mCameraSettings);
            if(isFirst) {
                firstPreview =  findViewById(R.id.first_preview);
                firstPreview.addView(mPreview);
            } else {
                secondPreview = findViewById(R.id.second_preview);
                secondPreview.addView(mPreview);
            }
        } else {
            mCamera = getCameraInstance(CAMERA_FACING_BACK);
            mCamera.setDisplayOrientation(setCameraDisplayOrientation(this,CAMERA_FACING_BACK));
            mCameraSettings = mCamera.getParameters();
            mCameraSettings.setRotation(90);
            mPreview = new CameraPreview(this, mCamera,mCameraSettings);
            mCameraSettings.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
            mCamera.setParameters(mCameraSettings);
            if(isFirst) {
                firstPreview =  findViewById(R.id.first_preview);
                firstPreview.addView(mPreview);
            } else {
                secondPreview = findViewById(R.id.second_preview);
                secondPreview.addView(mPreview);
            }

        }

    }

    private void switchToBackCamera(final int type) {
        getCamera(type,false);
        TYPE = type;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mPreview.safeToOpenCamera)
                    mCamera.takePicture(null, null, mPicture);
                else
                    Toast.makeText(MainActivity.this,"Failed to open Camera",Toast.LENGTH_SHORT).show();
            }
        },3000);
//

    }
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Log", "onPictureTaken - raw");
        }
    };

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.i("Log", "onShutter'd");
        }
    };
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            boolean isFrontFirst = true;
            if(frontImageBitmap == null)
                isFrontFirst = false;
            byteToBitmap(data, TYPE);
            Log.d("Log", "onPictureTaken - raw");
            if(frontImageBitmap != null && backImageBitmap != null) {

                if(isFrontFirst)
                    mergedBitmap = createSingleImageFromMultipleImages(frontImageBitmap,backImageBitmap);
                else
                    mergedBitmap = createSingleImageFromMultipleImages(backImageBitmap,frontImageBitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mergedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                Intent saveImageImage = new Intent(MainActivity.this,EffectsActivity.class);
                startActivity(saveImageImage);
                releaseCamera();
                finish();
            } else {
                switchToBackCamera((TYPE == 1) ? CAMERA_FACING_BACK : CAMERA_FACING_FRONT);
            }



        }
    };
    private void byteToBitmap(byte[] blob,int type)  {
        Log.d("Model", Build.MODEL + " - " + Build.MODEL.contains("Mi A1"));
        Bitmap   bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(blob,0,blob.length),mPreview.mPictureSize.width,mPreview.mPictureSize.height,false);
        if(type == 1) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && Build.MODEL.contains("Mi A1")) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(new ByteArrayInputStream(blob));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("EXIF value", exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){

                    frontImageBitmap=rotateImage(bitmap, 90);
                }else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                    frontImageBitmap=rotateImage(bitmap, 270);
                }else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                    frontImageBitmap=rotateImage(bitmap, 270);
                } else
                    frontImageBitmap=rotateImage(bitmap, 0);
            } else
                frontImageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(blob,0,blob.length),mPreview.mPictureSize.width,mPreview.mPictureSize.height,false);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && Build.MODEL.contains("Mi A1")) {

                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(new ByteArrayInputStream(blob));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("EXIF value", exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){

                    backImageBitmap=rotateImage(bitmap, 90);
                }else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                    backImageBitmap=rotateImage(bitmap, 270);
                }else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                    backImageBitmap=rotateImage(bitmap, 180);
                } else
                    backImageBitmap=rotateImage(bitmap, 90);
            } else
                backImageBitmap =  Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(blob,0,blob.length),mPreview.mPictureSize.width,mPreview.mPictureSize.height,false);
        }
    }
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Log.d("Picture size ", "Width "+ mPreview.mPictureSize.width + " Height- " + mPreview.mPictureSize.height);
        return Bitmap.createBitmap(source, 0, 0, mPreview.mPictureSize.width ,mPreview.mPictureSize.height, matrix, true);
    }

    private Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage) {
        Bitmap result = Bitmap.createBitmap(mPreview.mPictureSize.width, mPreview.mPictureSize.height * 2, firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, new Matrix(), null);
        canvas.drawBitmap(secondImage, 0, mPreview.mPictureSize.height , null);
        return result;
    }
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mPreview = null;
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    public Camera getCameraInstance(int typeId){
        releaseCamera();
        Camera c = null;
        try {
            c = Camera.open(typeId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
            Log.i("Orientation Front"," "+result);
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
            Log.i("Orientation Back"," "+result);
        }
        return result;
    }
//    private static File getOutputMediaFile(int type, int cameraFacing){
//
//
//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "DuelShots");
//        if (! mediaStorageDir.exists()){
//            if (! mediaStorageDir.mkdirs()){
//                Log.d("Directory", "failed to create directory");
//                return null;
//            }
//        }
//
//
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile;
//        if (type == MEDIA_TYPE_IMAGE){
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_Merged"+ ".jpg");
//        } else if(type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_"+ timeStamp + ".mp4");
//        } else {
//            return null;
//        }
//
//        return mediaFile;
//    }

}
