package com.view.harriscam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.main.harriscam.R;
import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public Camera camera;
    public byte[][] rawImages; // 0: first, 1: second, 2: third Raw data
    Context context;
    SurfaceHolder holder;
    Camera.Parameters cameraParameters;
    List< Camera.Size > previewSizeList;
    List< Camera.Size > pictureSizeList;
    Camera.Size previewSize;
    private int totalOfCamera;
    private boolean isFrontCamera;
    private int flagOfFlashlight;
    private int indexOfImages;
    private boolean isCapture;
    private int intervalTime;
    private long lastTime;
    private AudioManager audioManager;
    private ToneGenerator tone;
    private ProgressDialog progressDialog;

    public CameraSurfaceView( Context context ) {
        super( context );
        init( context, null, 0 );
    }

    public CameraSurfaceView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context, attrs, 0 );
    }

    public CameraSurfaceView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context, attrs, defStyle );
    }

    public static native void naApplyHarris( Bitmap bitG, Bitmap bitR, Bitmap bitB );

    public static native void naApplyScreen( Bitmap bmpResult, Bitmap bmpImage1, Bitmap bmpImage2 );

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;

        holder = getHolder();
        holder.addCallback( this );

        rawImages = new byte[ HarrisConfig.PICTURE_COUNT ][];

        tone = new ToneGenerator( AudioManager.STREAM_ALARM, 100 );

        if ( progressDialog == null ) {
            progressDialog = new ProgressDialog( context );
            progressDialog.setMessage( getResources().getString( R.string.msg_progressing ) );
            progressDialog.setIndeterminate( true );
            progressDialog.setCancelable( false );
        }
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        openCamera( Camera.CameraInfo.CAMERA_FACING_BACK );

        totalOfCamera = Camera.getNumberOfCameras();
        isFrontCamera = false;
        flagOfFlashlight = 0;
        indexOfImages = 0;
    }

    private void openCamera( int camNum ) {
        try {
            camera = Camera.open( camNum );
            camera.setPreviewCallback( this );
            camera.setDisplayOrientation( rotatePreview( 90 ) );
            camera.setPreviewDisplay( holder );
        } catch ( IOException e ) {
            HarrisUtil.jlog( e );
        }

        cameraParameters = camera.getParameters();
        previewSizeList = cameraParameters.getSupportedPreviewSizes();
        pictureSizeList = cameraParameters.getSupportedPictureSizes();

        HarrisUtil.sortCameraSize( previewSizeList, true );
        HarrisUtil.sortCameraSize( pictureSizeList, true );

        initResolution();
    }

    private int rotatePreview( int roateAngle ) {
        int rotation = ( ( Activity ) context ).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch ( rotation ) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;

        }

        int result = ( roateAngle - degrees + 360 ) % 360;
        return result;
    }

    private Bitmap rotateBitmap( Bitmap bitmap, int angle ) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate( angle );

        Bitmap resizedBitmap = Bitmap.createBitmap( bitmap, 0, 0, width, height, matrix, true );
        bitmap.recycle();

        return resizedBitmap;
    }

    private void initResolution() {
        int length = previewSizeList.size();

        HarrisUtil.jlog( previewSizeList.get( 0 ).width + " width1, " + previewSizeList.get( 0 ).height + " height1" );
        HarrisUtil.jlog( previewSizeList.get( length / 2 ).width + " width2, " + previewSizeList.get( length / 2 ).height + " height2" );
        HarrisUtil.jlog( previewSizeList.get( length - 1 ).width + " width3, " + previewSizeList.get( length - 1 ).height + " height3" );
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        startCamera();
    }

    private void startCamera() {
        if ( cameraParameters != null && camera != null ) {
            previewSize = previewSizeList.get( 0 );
            cameraParameters.setPreviewSize( previewSize.width, previewSize.height );
            camera.setParameters( cameraParameters );
            camera.startPreview();

            HarrisUtil.jlog( previewSizeList.get( 0 ).width + ", " + previewSizeList.get( 0 ).height );
        }
    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {
        releaseCamera();
    }

    public void releaseCamera() {
        if ( camera != null ) {
            try {
                camera.setPreviewCallback( null );
                camera.release();
                camera = null;
            } catch ( Exception e ) {
                HarrisUtil.jlog( e );
            }
        }
    }

    private void playSound() {
        tone.startTone( ToneGenerator.TONE_PROP_BEEP );
    }

    public void takePhotos() {
        isCapture = true;
        intervalTime = HarrisConfig.INTERVAL;
        lastTime = System.currentTimeMillis();

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
        HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";
    }

    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {
        if ( isCapture ) {
            ( ( Activity ) context ).runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            } );

            if ( indexOfImages <= 0 ) {
                rawImages[ indexOfImages++ ] = data;
                playSound();
            } else if ( intervalTime <= System.currentTimeMillis() - lastTime ) {
                rawImages[ indexOfImages++ ] = data;
                playSound();
                lastTime = System.currentTimeMillis();
            }

            if ( indexOfImages >= HarrisConfig.PICTURE_COUNT ) {
                indexOfImages = 0;
                isCapture = false;

                Bitmap bmpImage[] = new Bitmap[ HarrisConfig.PICTURE_COUNT ];

                int i = 1;
                for ( byte[] byImage : rawImages ) {
                    int w = cameraParameters.getPreviewSize().width;
                    int h = cameraParameters.getPreviewSize().height;
                    int format = cameraParameters.getPreviewFormat();
                    YuvImage image = new YuvImage( byImage, format, w, h, null );

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Rect area = new Rect( 0, 0, w, h );
                    image.compressToJpeg( area, 100, out );
                    bmpImage[ i - 1 ] = BitmapFactory.decodeByteArray( out.toByteArray(), 0, out.size() );

                    String filename = HarrisConfig.FILE_PATH + ( i ) + ".jpg";
                    Bitmap bitmap = rotateBitmap( bmpImage[ i - 1 ], 90 );
                    HarrisUtil.SaveBitmapToFileCache( bitmap, filename, 100 );
                    i++;
                }

                synchronized ( bmpImage[ 0 ] ) {
                    HarrisConfig.BMP_HARRIS = Bitmap.createBitmap( bmpImage[ 0 ] );
//                    naApplyHarris( HarrisConfig.BMP_HARRIS, bmpImage[1], bmpImage[2] );
//                    NativeHarrisCam.naApplyHarris( HarrisConfig.BMP_HARRIS, bmpImage[1], bmpImage[2] );
                }

                while ( !( new File( HarrisConfig.FILE_PATH + "1.jpg" ).exists() )
                        || !( new File( HarrisConfig.FILE_PATH + "2.jpg" ).exists() )
                        || !( new File( HarrisConfig.FILE_PATH + "3.jpg" ).exists() ) ) {
                }

                for ( Bitmap bitmap : bmpImage ) {
                    bitmap.recycle();
                    bitmap = null;
                }

                if ( HarrisConfig.IS_SAVE_ORIGINAL_IMAGE == false ) {
                    ( new File( HarrisConfig.FILE_PATH + "1.jpg" ) ).delete();
                    ( new File( HarrisConfig.FILE_PATH + "2.jpg" ) ).delete();
                    ( new File( HarrisConfig.FILE_PATH + "3.jpg" ) ).delete();
                } else {
                    HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "1.jpg" );
                    HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "2.jpg" );
                    HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "3.jpg" );
                }

                ( ( Activity ) context ).runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                } );
            }
        }
    }
}