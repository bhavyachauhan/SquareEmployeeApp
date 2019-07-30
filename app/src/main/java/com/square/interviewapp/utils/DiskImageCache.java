/*
 * Using https://github.com/JakeWharton/DiskLruCache to cache images on disk
 * with help from https://medium.com/mindorks/this-post-is-about-the-implementation-details-of-jake-whartons-famous-disklrucache-9a87d90206fe
 * */

package com.square.interviewapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StatFs;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DiskImageCache {

    private static final String TAG = DiskImageCache.class.getCanonicalName();
    private static final long MIN_DISK_CACHE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_DISK_CACHE = 50 * 1024 * 1024; // 50 MB
    private static final int BUFFER_SIZE = 8 * 1024;

    private DiskLruCache diskCache;
    private static final String CACHE_DIR = "square-images";

    DiskImageCache(Context context) {
        try {
            final File cacheDir = createCacheDir(context);
            long cacheSize = getDiskCacheSize(cacheDir);
            diskCache = DiskLruCache.open(cacheDir, 1, 1, cacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addBitmapToDiskCache(String key, Bitmap bitmap) throws IOException {
        Editor editor = diskCache.edit(key);
        if (editor == null) { // Current editing already in progress for the key
            return;
        }

        if (writeBitmapToBuffer(bitmap, editor.newOutputStream(0))) {
            diskCache.flush();
            editor.commit();
            Log.i(TAG, "Caching image with key " + key + " to disk.");
        } else {
            editor.abort();
        }

    }

    Bitmap getBitmapFromDiskCache(String key) {
        try (DiskLruCache.Snapshot snapshot = diskCache.get(key)) {
            if (snapshot == null) {
                return null;
            }

            InputStream in = snapshot.getInputStream(0);
            if (in == null) {
                return null;
            }

            BufferedInputStream bufferedIn = new BufferedInputStream(in, BUFFER_SIZE);
            Log.i(TAG, "Reading cached image with key " + key + " from disk.");
            return BitmapFactory.decodeStream(bufferedIn);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading snapshot from cache.", e);
            return null;
        }

    }

    private boolean writeBitmapToBuffer(Bitmap bitmap, OutputStream stream) throws IOException {
        try (OutputStream out = new BufferedOutputStream(stream, BUFFER_SIZE)) {
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
    }


    private static long getDiskCacheSize(File file) { //in MB
        StatFs statFs = new StatFs(file.getAbsolutePath());
        long totalSize = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        long target = Math.max(MIN_DISK_CACHE, totalSize / 50); // 2% of available size
        return Math.min(target, MAX_DISK_CACHE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File createCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), CACHE_DIR);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }
}
