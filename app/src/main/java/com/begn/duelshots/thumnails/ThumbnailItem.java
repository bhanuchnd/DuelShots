package com.begn.duelshots.thumnails;

import android.graphics.Bitmap;



import net.alhazmy13.imagefilter.ImageFilter;

public class ThumbnailItem {
    public Bitmap image;
    public ImageFilter.Filter filters;
    public boolean customFilter;
    public ThumbnailItem() {
        this.image = null;
        this.filters = null;
        this.customFilter = false;
    }
}
