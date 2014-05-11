package com.view.harriscam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
    // Camera
    public Camera camera;
    // Bitmap & Image
    public byte[][] rawImages; // 0: first, 1: second, 2: third Raw data
    Camera.Parameters cameraParameters;
    List< Camera.Size > previewSizeList;
    List< Camera.Size > pictureSizeList;
    Camera.Size previewSize;
    // View
    Context context;
    SurfaceHolder holder;
    private int totalOfCamera;
    private boolean isFrontCamera;
    private int flagOfFlashlight;
    private int indexOfImages;
    private int intervalTime;
    private long lastTime;
    private ProgressDialog progressApplying;
    private ImageButton ibShutter;

    // Sound
    private ToneGenerator tone;

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

    private void init( Context context, AttributeSet attrs, int defStyle ) {
        this.context = context;

        holder = getHolder();
        holder.addCallback( this );

        rawImages = new byte[ HarrisConfig.PHOTO_COUNT ][];

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
        openCamera( Camera.CameraInfo.CAMERA_FACING_BACK );

        totalOfCamera = Camera.getNumberOfCameras();
        isFrontCamera = false;
        flagOfFlashlight = 0;
        indexOfImages = 0;
    }

    public void openCamera( int camNum ) {
        try {
            camera = Camera.open( camNum );
            camera.setPreviewCallback( this );
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

        int result = ( roateAngle - degrees + 360 ) % 360;
        return result;
    }

    private void initializeQuality() {
        HarrisConfig.PHOTO_QUALITY.clear();
        float ratioPreview = ( float ) previewSizeList.get( 0 ).width / ( float ) previewSizeList.get( 0 ).height;
        ratioPreview = Float.valueOf( String.format( "%.2f", ratioPreview ) );

        for ( Camera.Size size : previewSizeList ) {
            float ratio = ( float ) size.width / ( float ) size.height;
            ratio = Float.valueOf( String.format( "%.2f", ratio ) );

            if ( ratioPreview * 0.9 > ratio || ratio > ratioPreview * 1.1 )
                continue;

            if ( size.width < HarrisConfig.MIN_PHOTO_WIDTH )
                break;

            HarrisConfig.PHOTO_QUALITY.add( size );
        }
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        startCamera();
    }

    private void startCamera() {
        if ( cameraParameters != null && camera != null ) {
            previewSize = HarrisConfig.PHOTO_QUALITY.get( HarrisConfig.QUALITY_INDEX );
            cameraParameters.setPreviewSize( previewSize.width, previewSize.height );
            camera.setParameters( cameraParameters );
            camera.startPreview();
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
        HarrisConfig.DOIN_CAPTURE = true;
        intervalTime = HarrisConfig.CAPTURE_INTERVAL;
        lastTime = System.currentTimeMillis();

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
        HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";
    }

    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {
        if ( HarrisConfig.BD_GALLERY_BACKGROUND == null ) {
            new BlurImageTask().execute( data );
        }
        if ( HarrisConfig.DOIN_CAPTURE ) {
            if ( indexOfImages <= 0 ) {
                rawImages[ indexOfImages++ ] = data;
                playSound();
            } else if ( intervalTime <= System.currentTimeMillis() - lastTime ) {
                rawImages[ indexOfImages++ ] = data;
                playSound();
                lastTime = System.currentTimeMillis();
            }

            if ( indexOfImages >= HarrisConfig.PHOTO_COUNT ) {
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
    }

    public void setShutterButton( ImageButton ibShutter ) {
        this.ibShutter = ibShutter;
    }

    private class BlurImageTask extends AsyncTask< byte[], Void, Void > {

        @Override
        protected Void doInBackground( byte[]... params ) {
            int w = cameraParameters.getPreviewSize().width;
            int h = cameraParameters.getPreviewSize().height;
            int format = cameraParameters.getPreviewFormat();
            YuvImage image = new YuvImage( params[ 0 ], format, w, h, null );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rect area = new Rect( 0, 0, w, h );
            image.compressToJpeg( area, 100, out );
            Bitmap bmpBackground = BitmapFactory.decodeByteArray( out.toByteArray(), 0, out.size() );
            bmpBackground = HarrisImageProcess.rotateBitmap( bmpBackground, 90 );
            Bitmap bmpBlur = Bitmap.createScaledBitmap( bmpBackground, bmpBackground.getWidth() / 8,
                    bmpBackground.getHeight() / 8, false );

            bmpBlur = HarrisImageProcess.blurBitmap( bmpBlur, 30 );
            HarrisConfig.BD_GALLERY_BACKGROUND = new BitmapDrawable( getResources(), bmpBlur );

            bmpBackground.recycle();

            return null;
        }
    }

    private class ApplyHarrisShutterEffectTask extends AsyncTask< Void, Void, Void > {

        @Override
        protected Void doInBackground( Void... params ) {
            Bitmap bmpImage[] = new Bitmap[ HarrisConfig.PHOTO_COUNT ];

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
                bmpImage[ i - 1 ] = HarrisImageProcess.rotateBitmap( bmpImage[ i - 1 ], 90 );
                HarrisUtil.SaveBitmapToFileCache( bmpImage[ i - 1 ], filename, 100 );
                i++;
            }

            // TODO: Analyze process time
            // TODO: fast blur image
            long time = System.currentTimeMillis();

            synchronized ( bmpImage[ 0 ] ) {
                HarrisConfig.BMP_HARRIS_RESULT = Bitmap.createBitmap( bmpImage[ 0 ] );
                HarrisNative.naApplyHarris( HarrisConfig.BMP_HARRIS_RESULT, bmpImage[ 1 ], bmpImage[ 2 ] );
            }

            HarrisUtil.jlog( "time : " + ( System.currentTimeMillis() - time ) );

            while ( !( new File( HarrisConfig.FILE_PATH + "1.jpg" ).exists() )
                    || !( new File( HarrisConfig.FILE_PATH + "2.jpg" ).exists() )
                    || !( new File( HarrisConfig.FILE_PATH + "3.jpg" ).exists() ) ) {
            }

            for ( Bitmap bitmap : bmpImage ) {
                bitmap.recycle();
            }

            HarrisUtil.SaveBitmapToFileCache( HarrisConfig.BMP_HARRIS_RESULT, HarrisConfig.FILE_PATH + "fx.jpg", 100 );
            HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "fx.jpg" );

            if ( !HarrisConfig.IS_SAVE_ORIGINAL_IMAGE ) {
                ( new File( HarrisConfig.FILE_PATH + "1.jpg" ) ).delete();
                ( new File( HarrisConfig.FILE_PATH + "2.jpg" ) ).delete();
                ( new File( HarrisConfig.FILE_PATH + "3.jpg" ) ).delete();
            } else {
                HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "1.jpg" );
                HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "2.jpg" );
                HarrisUtil.singleBroadcast( context, HarrisConfig.FILE_PATH + "3.jpg" );
            }

            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid ) {
            progressApplying.dismiss();
            ibShutter.setEnabled( true );
            HarrisUtil.toast( context, getResources().getString( R.string.msg_success ) );
        }
    }
}