package com.square.interviewapp.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4ClassRunner.class)
public class ImageLoaderTests {

    private String imageUrl = "https://s3.amazonaws.com/sq-mobile-interview/photos/16c00560-6dd3-4af4-97a6-d4754e7f2394/small.jpg";


    //Assuming url is correct and internet is connected
    @Test
    public void testImageLoader() throws InterruptedException {
        Context appContext = ApplicationProvider.getApplicationContext();
        final ImageView imageView = new ImageView(appContext);

        final Object syncObject = new Object();

        ImageLoader imageLoader = ImageLoader.get(appContext);
        imageLoader.clearMemoryCache();
        imageLoader.loadImage(imageUrl, imageView, new ImageLoader.OnCompleteListener() {
            @Override
            public void onSuccess() {
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onFailure() {
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }
        });

        if (imageView.getDrawable() == null) {
            synchronized (syncObject) {
                syncObject.wait(5000);
            }
        }

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        assertNotNull(drawable);
    }

    @Test
    public void testImageLoaderIncorrectUrl() throws InterruptedException {
        Context appContext = ApplicationProvider.getApplicationContext();
        final ImageView imageView = new ImageView(appContext);
        final Object lockObject = new Object();

        ImageLoader imageLoader = ImageLoader.get(appContext);
        imageLoader.clearMemoryCache();
        imageLoader.loadImage("", imageView, new ImageLoader.OnCompleteListener() {
            @Override
            public void onSuccess() {
                fail();
                synchronized (lockObject) {
                    lockObject.notify();
                }
            }

            @Override
            public void onFailure() {
                synchronized (lockObject) {
                    lockObject.notify();
                }
            }
        });

        synchronized (lockObject) {
            lockObject.wait(5000);
        }
    }


    @Test
    public void testLRUMemoryCache() {
        Context appContext = ApplicationProvider.getApplicationContext();
        ImageView imageView = new ImageView(appContext);

        ImageLoader imageLoader = ImageLoader.get(appContext);
        imageLoader.loadImage(imageUrl, imageView);
        assertNotNull(imageLoader.getBitmapFromMemoryCache(imageUrl));
    }

    @Test
    public void testDiskCache() {
        Context appContext = ApplicationProvider.getApplicationContext();
        ImageView imageView = new ImageView(appContext);

        ImageLoader imageLoader = ImageLoader.get(appContext);
        imageLoader.loadImage(imageUrl, imageView);
        assertNotNull(imageLoader.getBitmapFromDiskCache(imageUrl));
    }

}
