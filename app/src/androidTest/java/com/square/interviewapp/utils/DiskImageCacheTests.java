package com.square.interviewapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;

import com.square.interviewapp.R;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DiskImageCacheTests {

    @Test
    public void testDiskImageCache() {
        Context context = ApplicationProvider.getApplicationContext();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.square_horizontal_logo);
        assertNotNull(bitmap);

        DiskImageCache diskImageCache = new DiskImageCache(context);
        try {
            diskImageCache.addBitmapToDiskCache("square_logo", bitmap);
            assertNotNull(diskImageCache.getBitmapFromDiskCache("square_logo"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
