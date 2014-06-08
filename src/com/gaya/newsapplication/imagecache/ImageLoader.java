package com.gaya.newsapplication.imagecache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gaya.newsapplication.R;
import com.gaya.newsapplication.util.AppUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

/**
 * Loads images from the web or cache.
 * 
 * @author Gaya
 * 
 */
public class ImageLoader {

    private static final String    TAG             = ImageLoader.class.getName();
    private MemoryCache            memoryCache     = new MemoryCache();
    private FileCache              fileCache       = null;
    private Map<ImageView, String> imageViews      = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService        executorService = null;

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    final int defaultImageId = R.drawable.default_image;

    public void displayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (null != bitmap) {
            imageView.setImageBitmap(bitmap);

        } else {
            // add image to retry queue
            queueImage(url, imageView);
            imageView.setImageResource(defaultImageId);
        }
    }

    /**
     * Maintains a queue to fetch images which cannot be fatched previously
     * 
     * @param url
     *            Image URL
     * @param imageView
     *            View the image to be get set
     */
    private void queueImage(String url, ImageView imageView) {
        ImageToLoad p = new ImageToLoad(url, imageView);
        executorService.submit(new ImagesLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);
        // get image from SD cache.
        Bitmap bitmap = decodeFile(f);

        // if image is not available in the cache, get image from web
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(AppUtils.TIMEOUT);
            conn.setReadTimeout(AppUtils.TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);

        } catch (Exception ex) {
            Log.e(TAG, "Exception occurred");
        }

        return bitmap;
    }

    /**
     * Decodes image and scales it to reduce memory consumption
     * 
     * @param file
     *            The image file
     * @return Bitmap image
     */
    private Bitmap decodeFile(File file) {
        Bitmap bitmap = null;
        try {
            // Decode image size
            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options1);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = options1.outWidth, height_tmp = options1.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            // decode with inSampleSize
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options2);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File cannot be found");
        }
        return bitmap;
    }

    // Task for the queue
    /**
     * 
     * @author Gaya
     * 
     */
    private class ImageToLoad {
        public String    url;
        public ImageView imageView;

        public ImageToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class ImagesLoader implements Runnable {
        ImageToLoad imageToLoad;

        ImagesLoader(ImageToLoad imageToLoad) {
            this.imageToLoad = imageToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(imageToLoad))
                return;
            Bitmap bmp = getBitmap(imageToLoad.url);
            memoryCache.put(imageToLoad.url, bmp);
            if (imageViewReused(imageToLoad)) {
                return;
            }
            BitmapDisplayer bd = new BitmapDisplayer(bmp, imageToLoad);
            Activity a = (Activity) imageToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    /**
     * Check the state of the ImageView
     * 
     * @param imageToLoad
     * @return
     */
    boolean imageViewReused(ImageToLoad imageToLoad) {
        boolean imageViewReused = false;
        String tag = imageViews.get(imageToLoad.imageView);
        if (tag == null || !tag.equals(imageToLoad.url)) {
            imageViewReused = true;
        }
        return imageViewReused;
    }

    /**
     * This class is used to display bitmap in UI thread
     * 
     * @author Gaya
     * 
     */
    class BitmapDisplayer implements Runnable {
        Bitmap      bitmap;
        ImageToLoad imageToLoad;

        public BitmapDisplayer(Bitmap bitmap, ImageToLoad imageToLoad) {
            this.bitmap = bitmap;
            this.imageToLoad = imageToLoad;
        }

        public void run() {
            if (imageViewReused(imageToLoad)) {
                return;
            }
            if (bitmap != null) {
                imageToLoad.imageView.setImageBitmap(bitmap);
            } else {
                imageToLoad.imageView.setImageResource(defaultImageId);
            }
        }
    }

}