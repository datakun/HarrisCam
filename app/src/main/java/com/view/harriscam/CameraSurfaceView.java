package com.view.harriscam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import com.image.harriscam.HarrisImageProcess;
import com.image.harriscam.HarrisNative;
import com.main.harriscam.EditActivity;
import com.main.harriscam.R;
import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    // Camera
    public Camera camera;
    // Bitmap & Image
    private byte[][] rawImages; // 0: first, 1: second, 2: third Raw data
    private Bitmap[] bmpImage;
    private boolean isMakingBackground;

    private Camera.Parameters cameraParameters;
    private List< Camera.Size > previewSizeList;
    private List< Camera.Size > pictureSizeList;
    private Camera.Size previewSize;
    // View
    private Context context;
    private SurfaceHolder holder;
    private int totalOfCamera;
    private boolean isFlashlightEnable;
    private int indexOfImages;
    private long lastTime;
    private ProgressDialog progressApplying;
    private ImageButton ibShutter;

    // Sound
    private ToneGenerator tone;

    public CameraSurfaceView( Context context ) {
        super( context );
        init( context );
    }

    public CameraSurfaceView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context );
    }

    public CameraSurfaceView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init( context );
    }

    private void init( Context context ) {
        this.context = context;

        holder = getHolder();
        holder.addCallback( this );

        rawImages = new byte[ HarrisConfig.PHOTO_COUNT ][];
        bmpImage = new Bitmap[ HarrisConfig.PHOTO_COUNT ];

        tone = new ToneGenerator( AudioManager.STREAM_ALARM, 100 );

        if ( progressApplying == null ) {
            progressApplying = new ProgressDialog( context );
            progressApplying.setMessage( getResources().getString( R.string.msg_progressing ) );
            progressApplying.setIndeterminate( true );
            progressApplying.setCancelable( false );
        }
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        openCamera( HarrisConfig.FLAG_CAMERA );

        totalOfCamera = Camera.getNumberOfCameras();
        isFlashlightEnable = cameraParameters.getFlashMode() != null;
        indexOfImages = 0;
    }

    public void openCamera( int camNum ) {
        try {
            camera = Camera.open( camNum );
            camera.setPreviewCallback( previewCallback );
            camera.setDisplayOrientation( rotatePreview( 90 ) );
            camera.setPreviewDisplay( holder );

            cameraParameters = camera.getParameters();
            previewSizeList = cameraParameters.getSupportedPreviewSizes();
            pictureSizeList = cameraParameters.getSupportedPictureSizes();
            HarrisUtil.sortCameraSize( previewSizeList, true );
            HarrisUtil.sortCameraSize( pictureSizeList, true );

            initializeQuality();
        } catch ( IOException e ) {
            HarrisUtil.jlog( e );

            HarrisUtil.toast( context, getResources().getString( R.string.msg_camera_failed ) );
        }
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

        return ( roateAngle - degrees + 360 ) % 360;
    }

    private void initializeQuality() {
        HarrisConfig.PHOTO_QUALITY_LIST.clear();
        float ratioPreview = ( float ) previewSizeList.get( 0 ).width / ( float ) previewSizeList.get( 0 ).height;
        ratioPreview = Float.valueOf( String.format( Locale.US, "%.2f", ratioPreview ) );

        for ( Camera.Size size : previewSizeList ) {
            float ratio = ( float ) size.width / ( float ) size.height;
            ratio = Float.valueOf( String.format( Locale.US, "%.2f", ratio ) );

            if ( ratioPreview * 0.9 > ratio || ratio > ratioPreview * 1.1 )
                continue;

            if ( size.width < HarrisConfig.MIN_PHOTO_WIDTH )
                break;

            HarrisConfig.PHOTO_QUALITY_LIST.add( size );
//            HarrisUtil.jlog( "width : " + size.width + ", height : " + size.height );
        }
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        startCamera();
    }

    private void startCamera() {
        if ( cameraParameters != null && camera != null ) {
            if ( HarrisConfig.PHOTO_QUALITY_LIST.size() > HarrisConfig.QUALITY_INDEX )
                previewSize = HarrisConfig.PHOTO_QUALITY_LIST.get( HarrisConfig.QUALITY_INDEX );
            else
                previewSize = HarrisConfig.PHOTO_QUALITY_LIST.get( 0 );
            cameraParameters.setPreviewSize( previewSize.width, previewSize.height );
            camera.setParameters( cameraParameters );
            camera.startPreview();

            setAutoFocus();

            setFocusable( true );
            setFocusableInTouchMode( true );
        }
    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {
        releaseCamera();
    }

    public void setAutoFocus() {
        if ( camera != null ) {
            camera.autoFocus( afCallback );
        }
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

    public boolean isSwitchCameraEnable() {
        return totalOfCamera > 1 ? true : false;
    }

    public boolean isFlashlightEnable() {
        return isFlashlightEnable;
    }

    private void setFlashlight() {
        if ( HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_FRONT )
            return;

        if ( cameraParameters.getFlashMode() != null ) {
            switch ( HarrisConfig.FLAG_FLASHLIGHT ) {
                case HarrisConfig.OFF:
                    cameraParameters.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );

                    break;
                case HarrisConfig.ON:
                    cameraParameters.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );

                    break;
            }

            camera.setParameters( cameraParameters );
        }
    }

    private void offFlashlight() {
        if ( HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_FRONT )
            return;

        if ( cameraParameters.getFlashMode() != null ) {
            cameraParameters.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
            camera.setParameters( cameraParameters );
        }
    }

    private void playSound() {
        tone.startTone( ToneGenerator.TONE_PROP_BEEP );
    }

    Camera.AutoFocusCallback afCallback = new Camera.AutoFocusCallback() {

        public void onAutoFocus( boolean success, Camera camera ) {
        }
    };

    public void takePhotos() {
        HarrisConfig.DOIN_CAPTURE = true;

        if ( HarrisConfig.FILE_PATH.equals( "" ) ) {
            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
            HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";
        }
    }

    public void setShutterButton( ImageButton ibShutter ) {
        this.ibShutter = ibShutter;
    }

    private void saveImageProcess( byte[] data ) {
        rawImages[ indexOfImages ] = data;
        new SaveBitmapFromYuv().execute( indexOfImages++ );
        playSound();
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame( byte[] data, Camera camera ) {
            if ( HarrisConfig.BD_GALLERY_BACKGROUND == null && isMakingBackground == false ) {
                new BlurImageTask().execute( data );
            }
            if ( HarrisConfig.DOIN_CAPTURE ) {
                if ( HarrisConfig.CAPTURE_INTERVAL > 0 ) {
                    setFlashlight();
                    if ( indexOfImages == 0 ) {
                        saveImageProcess( data );
                        lastTime = System.currentTimeMillis();
                    } else if ( HarrisConfig.CAPTURE_INTERVAL <= System.currentTimeMillis() - lastTime ) {
                        saveImageProcess( data );
                        lastTime = System.currentTimeMillis();
                    }
                } else {
                    if ( indexOfImages <= 2 ) {
                        setFlashlight();
                        try {
                            Thread.sleep( 50 );
                        } catch ( InterruptedException e ) {
                            HarrisUtil.jlog( e );
                        }
                        saveImageProcess( data );
                        HarrisConfig.DOIN_CAPTURE = false;
                        ( ( Activity ) context ).runOnUiThread( new Runnable() {

                            @Override
                            public void run() {
                                ibShutter.setEnabled( true );
                            }
                        } );
                    }
                }
            }

            if ( indexOfImages >= HarrisConfig.PHOTO_COUNT ) {
                offFlashlight();
                indexOfImages = 0;
                HarrisConfig.DOIN_CAPTURE = false;

                ( ( Activity ) context ).runOnUiThread( new Runnable() {

                    @Override
                    public void run() {
                        progressApplying.show();
                    }
                } );

                new ApplyHarrisShutterEffectTask().execute();
            }
        }
    };

    private class BlurImageTask extends AsyncTask< byte[], Void, Void > {

        @Override
        protected Void doInBackground( byte[]... params ) {
            isMakingBackground = true;
            int w = cameraParameters.getPreviewSize().width;
            int h = cameraParameters.getPreviewSize().height;
            int format = cameraParameters.getPreviewFormat();
            YuvImage image = new YuvImage( params[ 0 ], format, w, h, null );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rect area = new Rect( 0, 0, w, h );
            image.compressToJpeg( area, 100, out );

            Bitmap bmpBackground = null;
            try {
                bmpBackground = BitmapFactory.decodeByteArray( out.toByteArray(), 0, out.size() );
                if ( HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_BACK )
                    bmpBackground = HarrisImageProcess.rotateBitmap( bmpBackground, 90 );
                else
                    bmpBackground = HarrisImageProcess.rotateBitmap( bmpBackground, 270 );
            } catch ( OutOfMemoryError e ) {
                e.printStackTrace();
            }

            try {
                Bitmap bmpBlur = Bitmap.createScaledBitmap( bmpBackground, bmpBackground.getWidth() / 8,
                        bmpBackground.getHeight() / 8, false );
                Bitmap bmpTemp = Bitmap.createBitmap( bmpBlur );

                HarrisNative.naBlurBitmap( bmpTemp, bmpBlur, 20 );
                HarrisConfig.BD_GALLERY_BACKGROUND = new BitmapDrawable( getResources(), bmpBlur );

                bmpBackground.recycle();
                isMakingBackground = false;
            } catch ( NullPointerException e ) {
                e.printStackTrace();
                HarrisConfig.BD_GALLERY_BACKGROUND = null;
            }

            return null;
        }
    }

    private class SaveBitmapFromYuv extends AsyncTask< Integer, Void, Void > {

        @Override
        protected Void doInBackground( Integer... params ) {

            int index = params[ 0 ];

            int w = cameraParameters.getPreviewSize().width;
            int h = cameraParameters.getPreviewSize().height;
            int format = cameraParameters.getPreviewFormat();
            YuvImage image = new YuvImage( rawImages[ index ], format, w, h, null );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rect area = new Rect( 0, 0, w, h );
            image.compressToJpeg( area, 100, out );

            String filename = HarrisConfig.FILE_PATH + ( index + 1 ) + ".jpg";
            try {
                bmpImage[ index ] = BitmapFactory.decodeByteArray( out.toByteArray(), 0, out.size() );
                if ( HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_BACK )
                    bmpImage[ index ] = HarrisImageProcess.rotateBitmap( bmpImage[ index ], 90 );
                else
                    bmpImage[ index ] = HarrisImageProcess.rotateBitmap( bmpImage[ index ], 270 );
            } catch ( OutOfMemoryError e ) {
                e.printStackTrace();
            }

            try {
                HarrisUtil.saveBitmapToFileCache( bmpImage[ index ], filename, 100 );
            } catch ( NullPointerException e ) {
                e.printStackTrace();
            }

            offFlashlight();

            return null;
        }
    }

    private class ApplyHarrisShutterEffectTask extends AsyncTask< Void, Void, Void > {

        @Override
        protected Void doInBackground( Void... params ) {
            HarrisConfig.BMP_HARRIS_RESULT = Bitmap.createBitmap( bmpImage[ 0 ] );
            HarrisNative.naApplyHarris( HarrisConfig.BMP_HARRIS_RESULT, bmpImage[ 0 ], bmpImage[ 1 ], bmpImage[ 2 ] );

            int count = 0;
            while ( !( new File( HarrisConfig.FILE_PATH + "1.jpg" ).exists() )
                    || !( new File( HarrisConfig.FILE_PATH + "2.jpg" ).exists() )
                    || !( new File( HarrisConfig.FILE_PATH + "3.jpg" ).exists() ) ) {
                try {
                    Thread.sleep( 100 );
                } catch ( InterruptedException e ) {
                    HarrisUtil.jlog( e );
                }
                if ( count++ >= 50 )
                    break;
            }

            for ( Bitmap bitmap : bmpImage ) {
                bitmap.recycle();
            }

            if ( !HarrisConfig.IS_SAVE_ORIGINAL_IMAGE && !HarrisConfig.IS_SAVE_ORIGINAL_IMAGE_OF_ALL ) {
                ( new File( HarrisConfig.FILE_PATH + "1.jpg" ) ).delete();
                ( new File( HarrisConfig.FILE_PATH + "2.jpg" ) ).delete();
                ( new File( HarrisConfig.FILE_PATH + "3.jpg" ) ).delete();
            } else {
                HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "1.jpg" );

                if ( HarrisConfig.IS_SAVE_ORIGINAL_IMAGE ) {
                    ( new File( HarrisConfig.FILE_PATH + "2.jpg" ) ).delete();
                    ( new File( HarrisConfig.FILE_PATH + "3.jpg" ) ).delete();
                } else if ( HarrisConfig.IS_SAVE_ORIGINAL_IMAGE_OF_ALL ) {
                    HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "2.jpg" );
                    HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "3.jpg" );
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid ) {
            HarrisConfig.FILE_PATH = "";
            progressApplying.dismiss();
//            ibShutter.setEnabled( true );
            HarrisUtil.toast( context, getResources().getString( R.string.msg_success ) );

            context.startActivity( new Intent( context, EditActivity.class ) );
        }
    }
}