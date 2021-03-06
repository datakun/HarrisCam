package com.image.harriscam;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public final class HarrisImageProcess {
    private HarrisImageProcess() {}

    public static Bitmap blurBitmap( Bitmap sentBitmap, int radius ) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy( sentBitmap.getConfig(), true );

        if ( radius < 1 ) {
            return ( null );
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[ w * h ];
        bitmap.getPixels( pix, 0, w, 0, 0, w, h );

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[ wh ];
        int g[] = new int[ wh ];
        int b[] = new int[ wh ];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[ Math.max( w, h ) ];

        int divsum = ( div + 1 ) >> 1;
        divsum *= divsum;
        int dv[] = new int[ 256 * divsum ];
        for ( i = 0; i < 256 * divsum; i++ ) {
            dv[ i ] = ( i / divsum );
        }

        yw = yi = 0;

        int[][] stack = new int[ div ][ 3 ];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for ( y = 0; y < h; y++ ) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for ( i = -radius; i <= radius; i++ ) {
                p = pix[ yi + Math.min( wm, Math.max( i, 0 ) ) ];
                sir = stack[ i + radius ];
                sir[ 0 ] = ( p & 0xff0000 ) >> 16;
                sir[ 1 ] = ( p & 0x00ff00 ) >> 8;
                sir[ 2 ] = ( p & 0x0000ff );
                rbs = r1 - Math.abs( i );
                rsum += sir[ 0 ] * rbs;
                gsum += sir[ 1 ] * rbs;
                bsum += sir[ 2 ] * rbs;
                if ( i > 0 ) {
                    rinsum += sir[ 0 ];
                    ginsum += sir[ 1 ];
                    binsum += sir[ 2 ];
                } else {
                    routsum += sir[ 0 ];
                    goutsum += sir[ 1 ];
                    boutsum += sir[ 2 ];
                }
            }
            stackpointer = radius;

            for ( x = 0; x < w; x++ ) {

                r[ yi ] = dv[ rsum ];
                g[ yi ] = dv[ gsum ];
                b[ yi ] = dv[ bsum ];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[ stackstart % div ];

                routsum -= sir[ 0 ];
                goutsum -= sir[ 1 ];
                boutsum -= sir[ 2 ];

                if ( y == 0 ) {
                    vmin[ x ] = Math.min( x + radius + 1, wm );
                }
                p = pix[ yw + vmin[ x ] ];

                sir[ 0 ] = ( p & 0xff0000 ) >> 16;
                sir[ 1 ] = ( p & 0x00ff00 ) >> 8;
                sir[ 2 ] = ( p & 0x0000ff );

                rinsum += sir[ 0 ];
                ginsum += sir[ 1 ];
                binsum += sir[ 2 ];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = ( stackpointer + 1 ) % div;
                sir = stack[ ( stackpointer ) % div ];

                routsum += sir[ 0 ];
                goutsum += sir[ 1 ];
                boutsum += sir[ 2 ];

                rinsum -= sir[ 0 ];
                ginsum -= sir[ 1 ];
                binsum -= sir[ 2 ];

                yi++;
            }
            yw += w;
        }
        for ( x = 0; x < w; x++ ) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for ( i = -radius; i <= radius; i++ ) {
                yi = Math.max( 0, yp ) + x;

                sir = stack[ i + radius ];

                sir[ 0 ] = r[ yi ];
                sir[ 1 ] = g[ yi ];
                sir[ 2 ] = b[ yi ];

                rbs = r1 - Math.abs( i );

                rsum += r[ yi ] * rbs;
                gsum += g[ yi ] * rbs;
                bsum += b[ yi ] * rbs;

                if ( i > 0 ) {
                    rinsum += sir[ 0 ];
                    ginsum += sir[ 1 ];
                    binsum += sir[ 2 ];
                } else {
                    routsum += sir[ 0 ];
                    goutsum += sir[ 1 ];
                    boutsum += sir[ 2 ];
                }

                if ( i < hm ) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for ( y = 0; y < h; y++ ) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[ yi ] = ( 0xff000000 & pix[ yi ] ) | ( dv[ rsum ] << 16 ) | ( dv[ gsum ] << 8 ) | dv[ bsum ];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[ stackstart % div ];

                routsum -= sir[ 0 ];
                goutsum -= sir[ 1 ];
                boutsum -= sir[ 2 ];

                if ( x == 0 ) {
                    vmin[ y ] = Math.min( y + r1, hm ) * w;
                }
                p = x + vmin[ y ];

                sir[ 0 ] = r[ p ];
                sir[ 1 ] = g[ p ];
                sir[ 2 ] = b[ p ];

                rinsum += sir[ 0 ];
                ginsum += sir[ 1 ];
                binsum += sir[ 2 ];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = ( stackpointer + 1 ) % div;
                sir = stack[ stackpointer ];

                routsum += sir[ 0 ];
                goutsum += sir[ 1 ];
                boutsum += sir[ 2 ];

                rinsum -= sir[ 0 ];
                ginsum -= sir[ 1 ];
                binsum -= sir[ 2 ];

                yi += w;
            }
        }

        bitmap.setPixels( pix, 0, w, 0, 0, w, h );
        sentBitmap.recycle();
        sentBitmap = null;

        return ( bitmap );
    }

    public static Bitmap rotateBitmap( Bitmap bitmap, int angle ) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate( angle );

        Bitmap resizedBitmap = Bitmap.createBitmap( bitmap, 0, 0, width, height, matrix, true );
        bitmap.recycle();

        return resizedBitmap;
    }

    // TODO: rotate image
//    public void rotateImage(Bitmap bmInput) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        matrix.postScale(1, 1);
//
//        try {
//            Bitmap bitTemp = Bitmap.createBitmap(bmInput);
//
//            bmInput = Bitmap.createBitmap(bitTemp, 0, 0, bitTemp.getWidth(), bitTemp.getHeight(), matrix, true);
//            ivCropView.setImageBitmap(bitResultImage);
//
//            if (bitTemp != null) {
//                bitTemp.recycle();
//                bitTemp = null;
//            }
//            System.gc();
//        } catch (OutOfMemoryError e) {
//            showToast("Memory space is full... Try again.");
//
//            MainActivity.deleteAllBitmap();
//
//            setResult(RESULT_FAILED);
//            finish();
//        }
//
//        nImageWidth = bitResultImage.getWidth();
//        nImageHeight = bitResultImage.getHeight();
//
//        if (MainActivity.bScaledSquare == false) {
//            if (bVertical == true) {
//                bVertical = false;
//                adjustViewSize(ivCropView, displayWidth, displayWidth * 3 / 4);
//            } else {
//                bVertical = true;
//                adjustViewSize(ivCropView, displayWidth, displayWidth * 4 / 3);
//            }
//        }
//    }
}
