package com.example.android.inventory.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by takahashiteruyuki on 2017/09/03.
 */

public class ImageHelper {
    public static final byte[] convertBitmapToBlob(Bitmap bitmapImage) {
        byte[] byteArray;
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream);
            byteArray = byteOutputStream.toByteArray();
        } catch (Exception e) {
            Log.e("EditorActivity", "convertBitmapToBlob exception: " + e);
            return null;
        }
        return byteArray;
    }
    public static final Bitmap convertBlobToBitmap(byte[] imageByteArray) {
        if (imageByteArray == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
    }
}
