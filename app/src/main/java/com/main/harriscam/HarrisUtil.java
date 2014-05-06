package com.main.harriscam;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kimdata on 2014-05-04.
 */
public class HarrisUtil {
    private static final String TAG = "junu";

    static Comparator< Camera.Size > _ascSize = new Comparator< Camera.Size >() {
        @Override
        public int compare( Camera.Size arg0, Camera.Size arg1 ) {
            return ( arg1.width + arg1.height ) < ( arg0.width + arg0.height ) ? 1 : -1;
        }
    };
    static Comparator< Camera.Size > _descSize = new Comparator< Camera.Size >() {
        @Override
        public int compare( Camera.Size arg0, Camera.Size arg1 ) {
            return ( arg1.width + arg1.height ) > ( arg0.width + arg0.height ) ? 1 : -1;
        }
    };
    static Comparator< Integer > _ascInteger = new Comparator< Integer >() {
        @Override
        public int compare( Integer arg0, Integer arg1 ) {
            return arg1 < arg0 ? 1 : -1;
        }
    };
    static Comparator< Integer > _descInteger = new Comparator< Integer >() {
        @Override
        public int compare( Integer arg0, Integer arg1 ) {
            return arg1 > arg0 ? 1 : -1;
        }
    };

    public static void sortCameraSize( List< Camera.Size > list, boolean desc ) {
        if ( desc ) {
            Collections.sort( list, _descSize );
        } else {
            Collections.sort( list, _ascSize );
        }
    }

    public static void sortInteger( List< Integer > list, boolean desc ) {
        if ( desc ) {
            Collections.sort( list, _descInteger );
        } else {
            Collections.sort( list, _ascInteger );
        }
    }

    public static void jlog( String s ) {
        Log.i( TAG, s );
    }

    public static void jlog( Object o ) {
        Log.i( TAG, o.toString() );
    }

    public static void toast( Context ctx, String s ) {
        Toast.makeText( ctx, s, Toast.LENGTH_SHORT ).show();
    }

    public static void SaveBitmapToFileCache( Bitmap bitmap, String strFilePath, int quality ) {
        File fileCacheItem = new File( strFilePath );
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream( fileCacheItem );

            bitmap.compress( Bitmap.CompressFormat.JPEG, quality, out );
        } catch ( Exception e ) {
            Log.i( TAG, e.toString() );
        } finally {
            try {
                out.close();
            } catch ( IOException e ) {
                Log.i( TAG, e.toString() );
            }
        }
    }

    public static String makeDir( String str ) {
        String s = Environment.getExternalStorageDirectory().getAbsolutePath() + str;
        File dir = new File( s );
        dir.mkdirs();

        return s;
    }

    public static int dp2px( int dp, Resources res ) {
        final float scale = res.getDisplayMetrics().density;

        return ( int ) Math.ceil( dp * scale );
    }

    public static void singleBroadcast( Context context, String filepath ) {
        final int DIR_FORMAT = 0x3001; // directory
        Uri MediaUri = MediaStore.Files.getContentUri( "external" );
        ContentValues values = new ContentValues();
        values.put( MediaStore.MediaColumns.DATA, filepath );
        values.put( "format", DIR_FORMAT );
        values.put( MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000 );
        context.getContentResolver().insert( MediaUri, values );
    }
}
