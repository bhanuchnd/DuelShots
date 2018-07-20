package com.begn.duelshots.thumnails;

import android.content.Context;
import android.graphics.Bitmap;

import com.begn.duelshots.R;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailsManager {
    private static List<ThumbnailItem> filterThumbs = new ArrayList<ThumbnailItem>(10);
    private static List<ThumbnailItem> processedThumbs = new ArrayList<ThumbnailItem>(10);

    private ThumbnailsManager() {
    }

    public static void addThumb(ThumbnailItem thumbnailItem) {
        filterThumbs.add(thumbnailItem);
    }

    public static List<ThumbnailItem> processThumbs(Context context) {
        for (ThumbnailItem thumb : filterThumbs) {
            // scaling down the image
            float size = context.getResources().getDimension(R.dimen.recycler_size);
            thumb.image = Bitmap.createScaledBitmap(thumb.image, (int) size, (int) size, false);
//            thumb.image = thumb.filter.processFilter(thumb.image);

            processedThumbs.add(thumb);
        }
        return processedThumbs;
    }

    public static void clearThumbs() {
        filterThumbs = new ArrayList<>();
        processedThumbs = new ArrayList<>();
    }
}
