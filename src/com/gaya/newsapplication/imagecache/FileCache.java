package com.gaya.newsapplication.imagecache;

import java.io.File;
import android.content.Context;

/**
 * This class saves and retrieved the cached images into/from an external
 * storage
 * 
 * @author Gaya
 * 
 */
public class FileCache {
    private File cacheDir;

    public FileCache(Context context) {
        // Find the directory to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "TempImages");

        } else {
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    /**
     * Get the image
     * 
     * @param url
     *            URL if the image is taken from web
     * @return the image file
     */
    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}