package com.image.harriscam;

import android.graphics.Bitmap;

public final class HarrisNative {
    private HarrisNative() {
    }

    static {
        System.loadLibrary( "HarrisCam" );
    }

    public static native void naApplyHarris( Bitmap bmpResult,  Bitmap bmpGreen, Bitmap bmpRed, Bitmap bmpBlue );

    public static native void naApplyScreen( Bitmap bmpResult, Bitmap bmpOriginal, Bitmap bmpTemp );

    public static native void naBlurBitmap( Bitmap bmpInput, Bitmap bmpResult, int radius );
}
