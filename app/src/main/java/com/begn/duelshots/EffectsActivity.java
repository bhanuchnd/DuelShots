package com.begn.duelshots;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.begn.duelshots.imageEditor.EditImageActivity;
import com.begn.duelshots.thumnails.ThumbnailCallback;
import com.begn.duelshots.thumnails.ThumbnailItem;
import com.begn.duelshots.thumnails.ThumbnailsAdapter;
import com.begn.duelshots.thumnails.ThumbnailsManager;
import com.theartofdev.edmodo.cropper.CropImageView;


import net.alhazmy13.imagefilter.ImageFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.ViewType;



import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class EffectsActivity extends AppCompatActivity
        implements ThumbnailCallback,OnPhotoEditorListener {



    private ImageView mergedImage, straightenImageView;
    private Activity activity;
    RecyclerView effectsList;
    public static Bitmap mergedBitmap;
    private CropImageView cropImageView;
    private SeekBar seekBar;
    private Matrix mMatrix;
    private static String imagePath;
    private static boolean isEdited = false;
    private static boolean effectAdded = false;
    Bitmap straightenBitmap;
    ImageButton saveButton, cancelButton,
            cropButton,cropCancel,cropSave,
            editImageButton,
            rotate,straightenButton,straightenSaveButton, straightenCancelButton;
    private static final float BLUR_RADIUS = 25f;
    private RelativeLayout effectsLayout, cropLayout,straightenLayout;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_action_rotate) {
            cropImageView.rotateImage(90);
            return true;
        } else if (item.getItemId() == R.id.main_action_flip_horizontally) {
            cropImageView.flipImageHorizontally();
            return true;
        } else if (item.getItemId() == R.id.main_action_flip_vertically) {
            cropImageView.flipImageVertically();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_menu,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = this;
        if(getIntent().getExtras() != null) {
            isEdited = getIntent().getExtras().getBoolean("isEdited");
            imagePath = getIntent().getExtras().getString("editedImage");
        }


        mergedBitmap = MainActivity.mergedBitmap;

//        actionBar = getSupportActionBar();
//        actionBar.hide();

//        mergedBitmap = blurBackground(mergedBitmap);
        effectsLayout = findViewById(R.id.effects_layout);
        cropLayout = findViewById(R.id.crop_layout);
        cropImageView = findViewById(R.id.crop_view);
        cropImageView.setAspectRatio(mergedBitmap.getWidth(),mergedBitmap.getHeight());
        mergedImage = findViewById(R.id.merged_image);
        if(isEdited) {
            mergedImage.setImageURI(Uri.fromFile(new File(imagePath)));
            mergedBitmap = ((BitmapDrawable) mergedImage.getDrawable()).getBitmap();
        } else {
            mergedImage.setImageBitmap(mergedBitmap);
        }
        effectsList = findViewById(R.id.effects_list);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        cropCancel = findViewById(R.id.crop_cancelButton);
        straightenCancelButton = findViewById(R.id.straighten_cancel);
        straightenSaveButton = findViewById(R.id.straighten_saveButton);
        editImageButton = findViewById(R.id.edit_image);
        cropSave = findViewById(R.id.crop_saveButton);
        cropButton = findViewById(R.id.crop_button);
        rotate = findViewById(R.id.rotate);
        seekBar = findViewById(R.id.straighten);
        straightenButton = findViewById(R.id.straighten_button);
        straightenImageView = findViewById(R.id.straighten_image_view);
        straightenLayout = findViewById(R.id.straighten_layout);
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editImageIntent = new Intent(EffectsActivity.this,EditImageActivity.class);
                startActivity(editImageIntent);
            }
        });
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(90);
            }
        });
        straightenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                straightenImageView.setImageBitmap(mergedBitmap);
                cropLayout.setVisibility(View.INVISIBLE);
                effectsLayout.setVisibility(View.INVISIBLE);
                straightenLayout.setVisibility(View.VISIBLE);
            }
        });
        straightenSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mergedImage.setImageMatrix(straightenImageView.getImageMatrix());
                mergedBitmap = straightenBitmap;
                mergedImage.setImageBitmap(mergedBitmap);
                cropLayout.setVisibility(View.INVISIBLE);
                effectsLayout.setVisibility(View.VISIBLE);
                straightenLayout.setVisibility(View.INVISIBLE);
                effectAdded = true;
            }
        });
        straightenCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropLayout.setVisibility(View.INVISIBLE);
                effectsLayout.setVisibility(View.VISIBLE);
                straightenLayout.setVisibility(View.INVISIBLE);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float angle = progress - 45;
                float width = straightenImageView.getDrawable().getIntrinsicWidth();
                float height = straightenImageView.getDrawable().getIntrinsicHeight();
                if(width > height) {
                    width = straightenImageView.getDrawable().getIntrinsicHeight();
                    height = straightenImageView.getDrawable().getIntrinsicWidth();
                }
                float a = (float) Math.atan(height/width);
                float len1 = (width / 2) / (float) Math.cos(a - Math.abs(Math.toRadians(angle)));
                float len2 = (float) Math.sqrt(Math.pow(width/2,2) + Math.pow(height/2,2));
                float scale = len2 / len1;


                Matrix matrix = straightenImageView.getImageMatrix();
                if (mMatrix == null) {
                    mMatrix = new Matrix(matrix);
                }
                matrix = new Matrix(mMatrix);
                float newX = (straightenImageView.getWidth() / 2) * (1 - scale);
                float newY = (straightenImageView.getHeight() / 2) * (1 - scale);
                matrix.postScale(scale, scale);
                matrix.postTranslate(newX, newY);
                matrix.postRotate(angle, straightenImageView.getWidth() / 2, straightenImageView.getHeight() / 2);
                straightenImageView.setImageMatrix(matrix);
                straightenBitmap = Bitmap.createBitmap(mergedBitmap,0,0,(int) width,(int) height,matrix,false);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox(activity);
            }
        });
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                effectsLayout.setVisibility(View.INVISIBLE);
                cropLayout.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(MainActivity.mergedBitmap);
            }
        });
        cropSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergedBitmap = cropImageView.getCroppedImage();
                mergedImage.setImageBitmap(mergedBitmap);
                cropLayout.setVisibility(View.INVISIBLE);
                effectsLayout.setVisibility(View.VISIBLE);
                effectAdded = true;
                bindDataToAdapter();
            }
        });
        cropCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropLayout.setVisibility(View.INVISIBLE);
                effectsLayout.setVisibility(View.VISIBLE);
            }
        });
//        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
//            @Override
//            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
//
//            }
//        });
        initHorizontalList();
    }
    private void showDialogBox(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.title)
                .setPositiveButton(R.string.positive_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetImage(dialog);
                    }
                })
                .setNegativeButton(R.string.negative_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }
    private void resetImage(DialogInterface dialog) {
        if(effectAdded || isEdited) {
            File imageFile = new File(imagePath);
            imageFile.delete();
            bindDataToAdapter();
            effectAdded = false;
            isEdited = false;
            mergedBitmap = MainActivity.mergedBitmap;
            mergedImage.setImageBitmap(mergedBitmap);

        } else {
            dialog.dismiss();
            onBackPressed();
        }

    }
    private Bitmap blurBackground(Bitmap image) {
        Bitmap blurredImage = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, blurredImage);

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(blurredImage);
        int width = Math.round(blurredImage.getWidth() * 0.70f);
        int height = Math.round(blurredImage.getHeight() *  0.9f);
        Log.i("Width" , width + " ");
        Log.i("Height", height + " ");
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mergedBitmap,width,height,false);
        Bitmap result = Bitmap.createBitmap(blurredImage.getWidth(),blurredImage.getHeight(), blurredImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(blurredImage, new Matrix(), null);
        canvas.drawBitmap(scaledBitmap,
                (blurredImage.getWidth() - scaledBitmap.getWidth()) / 2 ,
                (blurredImage.getHeight() - scaledBitmap.getHeight()) / 2, new Paint());
        return result;
    }
    private void saveImage() {

        mergedBitmap = ((BitmapDrawable) mergedImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mergedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] mergedShot = stream.toByteArray();
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE,0);//
                if (pictureFile == null){
                    Log.d("Media File Error", "Error creating media file, check storage permissions: ");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(mergedShot);
                    fos.close();
//                    cancelButton.setVisibility(View.INVISIBLE);
                    cancelButton.setEnabled(false);
                    Toast.makeText(EffectsActivity.this, "Image Stored in" + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    Log.d("File Not Found", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Accessing File", "Error accessing file: " + e.getMessage());
                }
    }
    private static File getOutputMediaFile(int type, int cameraFacing){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"duelShots");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Directory", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp+ ".jpg");
            if(isEdited)
                mediaFile = new File(imagePath);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    private void initHorizontalList() {
        effectsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        layoutManager.setAutoMeasureEnabled(false);
        effectsList.setLayoutManager(layoutManager);
        bindDataToAdapter();
    }

    private void bindDataToAdapter() {
        final Context context = this.getApplication();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {

                Bitmap thumbImage = createThumbnail(mergedBitmap,100,100);
                ThumbnailItem t1 = new ThumbnailItem();
                ThumbnailItem t2 = new ThumbnailItem();
                ThumbnailItem t3 = new ThumbnailItem();
                ThumbnailItem t4 = new ThumbnailItem();
                ThumbnailItem t5 = new ThumbnailItem();
                ThumbnailItem t6 = new ThumbnailItem();
                ThumbnailItem t7 = new ThumbnailItem();
                ThumbnailItem t8 = new ThumbnailItem();
                ThumbnailItem t9 = new ThumbnailItem();
                ThumbnailItem t10 = new ThumbnailItem();
                ThumbnailItem t11 = new ThumbnailItem();
                ThumbnailItem t12 = new ThumbnailItem();
                ThumbnailItem t13 = new ThumbnailItem();
                ThumbnailItem t14 = new ThumbnailItem();
                ThumbnailItem t15 = new ThumbnailItem();


                t1.image = thumbImage;
                t2.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.GRAY);
                t3.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.SOFT_GLOW);
                t4.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.LOMO);
                t5.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.SHARPEN);
                t6.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.HDR);
                t7.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.GOTHAM);
                t8.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.AVERAGE_BLUR);
                t9.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.OLD);
                t10.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.OIL);
                t11.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.LIGHT);
                t12.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.BLOCK);
                t13.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.NEON);
                t14.image = ImageFilter.applyFilter(thumbImage,ImageFilter.Filter.SKETCH);
                t15.image = blurBackground(thumbImage);

                ThumbnailsManager.clearThumbs();
                ThumbnailsManager.addThumb(t1); // Original Image
                t2.filters = ImageFilter.Filter.GRAY;
                t3.filters = ImageFilter.Filter.SOFT_GLOW;
                t4.filters = ImageFilter.Filter.LOMO;
                t5.filters = ImageFilter.Filter.SHARPEN;
                t6.filters = ImageFilter.Filter.HDR;
                t7.filters = ImageFilter.Filter.GOTHAM;
                t8.filters = ImageFilter.Filter.AVERAGE_BLUR;
                t9.filters = ImageFilter.Filter.OLD;
                t10.filters = ImageFilter.Filter.OIL;
                t11.filters = ImageFilter.Filter.LIGHT;
                t12.filters = ImageFilter.Filter.BLOCK;
                t13.filters = ImageFilter.Filter.NEON;
                t14.filters = ImageFilter.Filter.SKETCH;
                t15.filters = ImageFilter.Filter.MOTION_BLUR;

                ThumbnailsManager.addThumb(t2);
                ThumbnailsManager.addThumb(t3);
                ThumbnailsManager.addThumb(t4);
                ThumbnailsManager.addThumb(t5);
                ThumbnailsManager.addThumb(t6);
                ThumbnailsManager.addThumb(t7);
                ThumbnailsManager.addThumb(t8);
                ThumbnailsManager.addThumb(t9);
                ThumbnailsManager.addThumb(t10);
                ThumbnailsManager.addThumb(t11);
                ThumbnailsManager.addThumb(t12);
                ThumbnailsManager.addThumb(t13);
                ThumbnailsManager.addThumb(t14);
                ThumbnailsManager.addThumb(t15);

                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) activity);
                effectsList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();

    }
    public Bitmap createThumbnail(Bitmap bitmap,int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    @Override
    public void onThumbnailClick(ImageFilter.Filter filter) {
        if(filter != null) {
            if(filter.getValue() != 18 ) {
                mergedBitmap = ImageFilter.applyFilter(MainActivity.mergedBitmap,filter);
                mergedImage.setImageBitmap(mergedBitmap);
            } else {
                mergedBitmap = blurBackground(MainActivity.mergedBitmap);
                mergedImage.setImageBitmap(mergedBitmap);
            }

            effectAdded = true;
        }
        else {
            effectAdded = false;
//            mergedBitmap = MainActivity.mergedBitmap;
            mergedImage.setImageBitmap(mergedBitmap);
        }

    }

    @Override
    public void onEditTextChangeListener(View rootView, String text, int colorCode) {

    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {

    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {

    }
}
