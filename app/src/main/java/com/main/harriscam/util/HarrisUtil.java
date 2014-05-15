package com.main.harriscam.util;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kimdata on 2014-05-04.
 */
public final class HarrisUtil {
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

    private HarrisUtil() {
    }

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

    public static void saveBitmapToFileCache( Bitmap bitmap, String strFilePath, int quality ) {
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

    public static String getMicroSDCardDirectory() {
        List< String > mMounts = readMountsFile();

        for ( int i = 0; i < mMounts.size(); i++ ) {
            String mount = mMounts.get( i );

            File root = new File( mount );
            if ( !root.exists() || !root.isDirectory() ) {
                mMounts.remove( i-- );
                continue;
            }

            if ( !isAvailableFileSystem( mount ) ) {
                mMounts.remove( i-- );
                continue;
            }

            if ( !checkMicroSDCard( mount ) ) {
                mMounts.remove( i-- );
            }
        }

        if ( mMounts.size() == 1 ) {
            return mMounts.get( 0 );
        }

        return "";
    }

    private static List< String > readMountsFile() {
        List< String > mMounts = new ArrayList< String >();

        try {
            Scanner scanner = new Scanner( new File( "/proc/mounts" ) );

            while ( scanner.hasNext() ) {
                String line = scanner.nextLine();

                if ( line.startsWith( "/dev/block/vold/" ) ) {
                    String[] lineElements = line.split( "[ \t]+" );
                    String element = lineElements[ 1 ];

                    mMounts.add( element );
                }
            }
        } catch ( Exception e ) {
            HarrisUtil.jlog( e );
        }

        return mMounts;
    }

    private static boolean checkMicroSDCard( String fileSystemName ) {
        StatFs statFs = new StatFs( fileSystemName );

        long totalSize = ( long ) statFs.getBlockSize() * statFs.getBlockCount();

        if ( totalSize < 1024 * 1024 * 1024 * 1024 ) {
            return false;
        }

        return true;
    }

    private static boolean isAvailableFileSystem( String fileSystemName ) {
        final String[] unAvailableFileSystemList = { "/dev", "/mnt/asec", "/mnt/obb", "/system", "/data", "/cache", "/efs", "/firmware" };

        for ( String name : unAvailableFileSystemList ) {
            if ( fileSystemName.contains( name ) == true ) {
                return false;
            }
        }

        if ( Environment.getExternalStorageDirectory().getAbsolutePath().equals( fileSystemName ) == true ) {
            return false;
        }

        return true;
    }

    public static void unbindViewDrawable( View view ) {
        Drawable d = view.getBackground();
        if ( d != null ) {
            try {
                d.setCallback( null );
            } catch ( Exception ignore ) {
            }
        }

        try {
            if ( view instanceof ImageView ) {
                ImageView imageView = ( ImageView ) view;
                d = imageView.getDrawable();
                if ( d != null ) {
                    d.setCallback( null );
                }

                if ( d instanceof BitmapDrawable ) {
                    Bitmap bm = ( ( BitmapDrawable ) d ).getBitmap();
                    bm.recycle();
                }

                imageView.setImageDrawable( null );
            }
        } catch ( Exception ignore ) {
        }

        try {
            view.setBackgroundDrawable( null );
        } catch ( Exception ignore ) {
        }
    }

    public static InputStream getISFromURI( Context context, Uri contentURI ) {
        ContentResolver res = context.getContentResolver();
        Uri uri = Uri.parse( contentURI.toString() );
        InputStream is = null;
        try {
            is = res.openInputStream( uri );
        } catch ( FileNotFoundException e ) {
            jlog( e );
        }

        return is;
    }

    public static byte[] bitmapToByteArray( Bitmap $bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        $bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream );
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }

    public static Bitmap scaleToFillBitmap( Bitmap dst, InputStream is ) {
        Bitmap src = BitmapFactory.decodeStream( is );

        float scaled = 1.0f;
        if ( (float) dst.getWidth() / (float) src.getWidth() < (float) dst.getHeight() / (float) src.getHeight() ) {
            scaled = (float) dst.getHeight() / (float) src.getHeight();
        } else {
            scaled = (float) dst.getWidth() / (float) src.getWidth();
        }

        Bitmap bmpScaled = Bitmap.createScaledBitmap( src, (int) Math.ceil( src.getWidth() * scaled ),
                (int) Math.ceil( src.getHeight() * scaled ), true );

        int offsetX = 0;
        int offsetY = 0;
        offsetX = bmpScaled.getWidth() - dst.getWidth() != 0 ? ( bmpScaled.getWidth() - dst.getWidth() ) / 2 : 0;
        offsetY = bmpScaled.getHeight() - dst.getHeight() != 0 ? ( bmpScaled.getHeight() - dst.getHeight() ) / 2 : 0;

        return Bitmap.createBitmap( bmpScaled, offsetX, offsetY, dst.getWidth(), dst.getHeight() );
    }

    public static Bitmap scaleToFillBitmap( Bitmap dst, byte[] byImage ) {
        Bitmap src = BitmapFactory.decodeByteArray( byImage, 0, byImage.length );

        float scaled = 1.0f;
        if ( (float) dst.getWidth() / (float) src.getWidth() < (float) dst.getHeight() / (float) src.getHeight() ) {
            scaled = (float) dst.getHeight() / (float) src.getHeight();
        } else {
            scaled = (float) dst.getWidth() / (float) src.getWidth();
        }

        Bitmap bmpScaled = Bitmap.createScaledBitmap( src, (int) Math.ceil( src.getWidth() * scaled ),
                (int) Math.ceil( src.getHeight() * scaled ), true );

        int offsetX = 0;
        int offsetY = 0;
        offsetX = bmpScaled.getWidth() - dst.getWidth() != 0 ? ( bmpScaled.getWidth() - dst.getWidth() ) / 2 : 0;
        offsetY = bmpScaled.getHeight() - dst.getHeight() != 0 ? ( bmpScaled.getHeight() - dst.getHeight() ) / 2 : 0;

        return Bitmap.createBitmap( bmpScaled, offsetX, offsetY, dst.getWidth(), dst.getHeight() );
    }

    public static Bitmap scaleToStretchBitmap( Bitmap dst, InputStream is ) {
        Bitmap src = BitmapFactory.decodeStream( is );

        return Bitmap.createScaledBitmap( src, dst.getWidth(), dst.getHeight(), true );
    }

    public static Bitmap scaleToStretchBitmap( Bitmap dst, byte[] byImage ) {
        Bitmap src = BitmapFactory.decodeByteArray( byImage, 0, byImage.length );

        return Bitmap.createScaledBitmap( src, dst.getWidth(), dst.getHeight(), true );
    }
}
