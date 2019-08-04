package com.square.interviewapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.webkit.URLUtil;
import android.widget.ImageView;

import androidx.annotation.VisibleForTesting;

import com.square.interviewapp.retrofit.SquareAPIService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


//Reference: https://developer.android.com/topic/performance/graphics/cache-bitmap

@SuppressWarnings("DefaultAnnotationParam")
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getCanonicalName();

    private LruCache<String, Bitmap> memoryCache;
    private DiskImageCache diskImageCache;

    private static ImageLoader imageLoader;
    private final static String SHARED_PREF = "square-images";

    private final SharedPreferences sharedPreferences;
    private final WeakReference<Context> context;

    public static ImageLoader get(Context context) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader(context);
        }
        return imageLoader;
    }

    public void loadImage(final String url, final ImageView imageView, final int defaultImage) {
        loadImage(url, imageView, null, defaultImage);
    }

    void loadImage(final String url, final ImageView imageView) {
        loadImage(url, imageView, null);
    }

    public void loadImage(final String url, final ImageView imageView, final OnCompleteListener listener) {
        loadImage(url, imageView, listener, android.R.drawable.sym_def_app_icon);
    }

    @SuppressWarnings("WeakerAccess")
    public void loadImage(final String url, final ImageView imageView, final OnCompleteListener listener, final int defaultImage) {
        Bitmap bitmap = getBitmapFromMemoryCache(url);
        if (bitmap == null) {
            bitmap = getBitmapFromDiskCache(url);
            if (bitmap != null) {
                addBitmapToMemoryCache(url, bitmap);
            }
        }

        if (bitmap != null) {
            final Bitmap finalBitmap = bitmap;
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(finalBitmap);
                }
            });
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        }

        if (context.get() != null) {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(defaultImage);
                }
            });

            new ImageLoaderTask(imageView, listener).execute(url);
        }
    }

    private ImageLoader(Context context) {
        this.context = new WeakReference<>(context);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); //in KB
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024; //in KB
            }
        };

        sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);

        diskImageCache = new DiskImageCache(context);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void clearMemoryCache() {
        memoryCache.evictAll();
    }

    @SuppressLint("ApplySharedPref")
    private void addBitmapToDiskCache(String key, Bitmap bitmap) {
        String diskCacheKey = UUID.randomUUID().toString();
        if (sharedPreferences.contains(key)) {
            diskCacheKey = sharedPreferences.getString(key, diskCacheKey);
        } else {
            sharedPreferences.edit().putString(key, diskCacheKey).commit();
        }

        try {
            diskImageCache.addBitmapToDiskCache(diskCacheKey, bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Failed to cache image to disk.", e);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    Bitmap getBitmapFromDiskCache(String key) {
        String diskCacheKey = sharedPreferences.getString(key, null);
        if (diskCacheKey == null) {
            return null;
        }
        return diskImageCache.getBitmapFromDiskCache(diskCacheKey);
    }

    @SuppressLint("StaticFieldLeak")
    class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

        WeakReference<ImageView> imageViewWeakReference;
        OnCompleteListener listener;

        ImageLoaderTask(final ImageView imageView, final OnCompleteListener listener) {
            this.imageViewWeakReference = new WeakReference<>(imageView);
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(String... strs) {
            final String imageUrl = strs[0];
            if (!URLUtil.isValidUrl(imageUrl)) {
                if (listener != null) {
                    listener.onFailure();
                }
                return null;
            }

            OkHttpClient httpClient = SquareAPIService.getRetrofitService(context.get()).getOkHttpClientWithNetworkConnectionInterceptor(30);
            Request request = new Request.Builder()
                    .url(imageUrl).build();

            try {
                Response response = httpClient.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    final Bitmap image = BitmapFactory.decodeStream(response.body().byteStream());
                    if (listener != null) {
                        listener.onSuccess();
                    }

                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {
                            addBitmapToMemoryCache(imageUrl, image);
                            addBitmapToDiskCache(imageUrl, image);
                        }
                    });
                    return image;
                } else if (listener != null) {
                    listener.onFailure();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onFailure();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference.get() != null && bitmap != null) {
                imageViewWeakReference.get().setImageBitmap(bitmap);
            }
        }
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure();
    }


}
