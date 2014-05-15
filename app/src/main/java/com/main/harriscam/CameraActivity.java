package com.main.harriscam;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;
import com.view.harriscam.CameraSurfaceView;
import com.view.harriscam.DrawGuidelineView;
import com.view.harriscam.ModeSelectMenuView;
import com.view.harriscam.OptionSelectMenuView;
import com.view.harriscam.PhotoSelectMenuView;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {
    // Views
    private CameraSurfaceView cameraSurfaceView;
    private ModeSelectMenuView modeSelectMenuView;
    private OptionSelectMenuView optionSelectMenuView;
    private PhotoSelectMenuView photoSelectMenuView;
    private FrameLayout flGalleryModeBackground;
    private ImageButton ibShutter;
    private ImageView ivHarrisResult;
    private ImageButton ibSubmitEffect;
    private DrawGuidelineView drawLineView;

    // Control to tracking pointer
    private int startTrackPointX;
    private int stopTrackPointX;
    private int startTrackPointY;
    private boolean isVisibleModeMenu;
    private boolean isVisibleOptionsMenu;
    private boolean isVisiblePhotoMenu;
    private boolean isPossibleTracking;
    private boolean isDisabledTracking;

    // Shared Preference
    SharedPreferences sharedPref;
    SharedPreferences.Editor spEditor;

    // Listner
    private View.OnClickListener listenerClickShutter = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            cameraSurfaceView.takePhotos();
            ibShutter.setEnabled( false );
        }
    };

    private View.OnClickListener listenerClickMode = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            modeSelectMenuView.setEnableMenu( true );
            v.setEnabled( false );

            switch ( v.getId() ) {
                case R.id.ibCameraMode:
                    // TODO : gallery
//                    if ( photoSelectMenuView.isSelectedAnyPhoto() )
//                        askCancelApplyEffect();
//                    else {
                        HarrisConfig.FLAG_MODE = HarrisConfig.CAMERA;
                        if ( HarrisConfig.BD_GALLERY_BACKGROUND != null ) {
                            HarrisConfig.BD_GALLERY_BACKGROUND.getBitmap().recycle();
                            HarrisConfig.BD_GALLERY_BACKGROUND = null;
                        }
                        flGalleryModeBackground.setVisibility( View.GONE );
                        modeSelectMenuView.hideMenu();
                        showShutterButton();
                        ibShutter.setVisibility( View.VISIBLE );
                        ibSubmitEffect.setVisibility( View.GONE );
//                    }

                    break;
                case R.id.ibGalleryMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.GALLERY;

                    checkGalleryBackground();

                    flGalleryModeBackground.setBackgroundDrawable( HarrisConfig.BD_GALLERY_BACKGROUND );
                    flGalleryModeBackground.setVisibility( View.VISIBLE );
                    modeSelectMenuView.hideMenu();
                    showSubmitButton();
                    ibSubmitEffect.setVisibility( View.VISIBLE );
                    ibShutter.setVisibility( View.GONE );

                    break;
                case R.id.ibSettings:
                    modeSelectMenuView.hideMenu();
                    startActivity( new Intent( CameraActivity.this, SettingsActivity.class ) );

                    break;
            }
        }
    };

    private View.OnTouchListener listenerTouchOption = new View.OnTouchListener() {
        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            switch ( event.getAction() ) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor( getResources().getColor( R.color.AlphaGray ) );

                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor( getResources().getColor( android.R.color.transparent ) );

                    switch ( v.getId() ) {
                        case R.id.ibFlashlight:
                            HarrisConfig.FLAG_FLASHLIGHT = HarrisConfig.FLAG_FLASHLIGHT == HarrisConfig.ON ? HarrisConfig.OFF :
                                    HarrisConfig.ON;

                            break;
                        case R.id.ibGuideline:
                            HarrisConfig.FLAG_GUIDELINE = HarrisConfig.FLAG_GUIDELINE == HarrisConfig.ON ? HarrisConfig.OFF :
                                    HarrisConfig.ON;
                            drawLineView.invalidate();

                            break;
                        case R.id.ibCameraSwitcher:
                            HarrisConfig.FLAG_CAMERA = HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_BACK ?
                                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
                            cameraSurfaceView.setVisibility( View.GONE );
                            cameraSurfaceView.setVisibility( View.VISIBLE );

                            break;
                        case R.id.ibIntervalWatch:
                            HarrisConfig.CAPTURE_INTERVAL = ( HarrisConfig.CAPTURE_INTERVAL + HarrisConfig.INTERVAL_OFFSET ) %
                                    ( 4 * HarrisConfig.INTERVAL_OFFSET );

                            break;
                    }

                    optionSelectMenuView.updateView();
                    savePreference();

                    break;
            }

            return false;
        }
    };

    // TODO : gallery
    private View.OnClickListener listenerClickSubmit = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
//            if ( photoSelectMenuView.isSelectedAllPhoto() ) {
//                HarrisConfig.BMP_HARRIS_RESULT = ( ( BitmapDrawable ) ivHarrisResult.getDrawable() ).getBitmap();
//
//                Date now = new Date();
//                SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
//                HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";
//
//                HarrisUtil.saveBitmapToFileCache( HarrisConfig.BMP_HARRIS_RESULT, HarrisConfig.FILE_PATH + "fx.jpg", 100 );
//                HarrisUtil.singleBroadcast( CameraActivity.this, HarrisConfig.FILE_PATH + "fx.jpg" );
//
//                HarrisUtil.toast( CameraActivity.this, getString( R.string.msg_success ) );
//            }
        }
    };

    private void checkGalleryBackground() {
        int count = 0;
        while ( HarrisConfig.BD_GALLERY_BACKGROUND == null ) {
            try {
                Thread.sleep( 10 );
            } catch ( InterruptedException e ) {
                HarrisUtil.jlog( e );
            }

            if ( count++ > 20 )
                break;
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_camera );

        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPreference();

        initializeEnvironment();
    }

    @Override
    protected void onPause() {
        cameraSurfaceView.releaseCamera();

        super.onPause();
    }

    // TODO : gallery
//    @Override
//    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
//        if ( data == null ) {
//            return;
//        }
//
//        InputStream is = HarrisUtil.getISFromURI( this, data.getData() );
//        switch ( requestCode ) {
//            case HarrisConfig.REQUEST_FIRST:
//                photoSelectMenuView.applyFirstPhoto( is );
//
//                break;
//            case HarrisConfig.REQUEST_SECOND:
//                photoSelectMenuView.applySecondPhoto( is );
//
//                break;
//            case HarrisConfig.REQUEST_THIRD:
//                photoSelectMenuView.applyThirdPhoto( is );
//
//                break;
//        }
//        try {
//            is.close();
//        } catch ( IOException e ) {
//            HarrisUtil.jlog( e );
//        }
//
//        photoSelectMenuView.checkEnableApplyEffect();
//    }

    private void initializeView() {
        cameraSurfaceView = ( CameraSurfaceView ) findViewById( R.id.cameraSurfaceView );
        modeSelectMenuView = ( ModeSelectMenuView ) findViewById( R.id.modeSelectMenu );
        optionSelectMenuView = ( OptionSelectMenuView ) findViewById( R.id.optionSelectMenu );
        photoSelectMenuView = ( PhotoSelectMenuView ) findViewById( R.id.photoSelectMenu );
        flGalleryModeBackground = ( FrameLayout ) findViewById( R.id.flGalleryModeBackground );
        ivHarrisResult = ( ImageView ) findViewById( R.id.ivHarrisResult );
        ibSubmitEffect = ( ImageButton ) findViewById( R.id.ibSubmitEffect );
        drawLineView = ( DrawGuidelineView ) findViewById( R.id.dvLines );
        modeSelectMenuView.setOnMenuClickListener( listenerClickMode );
        optionSelectMenuView.setOnMenuTouchListener( listenerTouchOption );
        ibShutter = ( ImageButton ) findViewById( R.id.ibShutter );
        ibShutter.setOnClickListener( listenerClickShutter );
        cameraSurfaceView.setShutterButton( ibShutter );
        photoSelectMenuView.setResultImageView( ivHarrisResult );
        ibSubmitEffect.setOnClickListener( listenerClickSubmit );

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize( size );
        HarrisConfig.SWIPE_MAX_DISTANCE = size.x / 4;
        HarrisConfig.SWIPE_MIN_DISTANCE = HarrisConfig.SWIPE_MAX_DISTANCE / 2;
        HarrisConfig.SWIPE_POSSIBLE_DISTANCE = HarrisConfig.SWIPE_MIN_DISTANCE / 2;
    }

    private void loadPreference() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences( this );
        spEditor = sharedPref.edit();

        try {
            // Storage location
            HarrisConfig.STORAGE_PATH_LIST.clear();
            HarrisConfig.STORAGE_PATH_LIST.add( HarrisUtil.makeDir( "/DCIM/harriscam" ) );
            if ( !HarrisUtil.getMicroSDCardDirectory().equals( "" ) )
                HarrisConfig.STORAGE_PATH_LIST.add( HarrisUtil.makeDir( HarrisUtil.getMicroSDCardDirectory() + "/DCIM/harriscam" ) );
            int idxOfStorage = Integer.valueOf( sharedPref.getString( getString( R.string.pref_id_storage ), "0" ) );
            HarrisConfig.SAVE_PATH = HarrisConfig.STORAGE_PATH_LIST.get( idxOfStorage );
        } catch ( IndexOutOfBoundsException e ) {
            HarrisUtil.jlog( e );
            HarrisConfig.STORAGE_PATH_LIST.clear();
            HarrisConfig.STORAGE_PATH_LIST.add( HarrisUtil.makeDir( "/DCIM/harriscam" ) );
            HarrisConfig.SAVE_PATH = HarrisConfig.STORAGE_PATH_LIST.get( 0 );
        }

        // Photo quality
        HarrisConfig.QUALITY_INDEX = Integer.valueOf( sharedPref.getString( getString( R.string.pref_id_quality ), "0" ) );

        // Save original photos
        HarrisConfig.IS_SAVE_ORIGINAL_IMAGE = sharedPref.getBoolean( getString( R.string.pref_id_save_original ), true );

        // Flashlight
        HarrisConfig.FLAG_FLASHLIGHT = sharedPref.getInt( getString( R.string.pref_id_flashlight ), 0 );

        // Guideline
        HarrisConfig.FLAG_GUIDELINE = sharedPref.getInt( getString( R.string.pref_id_guideline ), 0 );

        // Camera switcher
        HarrisConfig.FLAG_CAMERA = sharedPref.getInt( getString( R.string.pref_id_camera_switcher ),
                Camera.CameraInfo.CAMERA_FACING_BACK );

        // Capture interval
        HarrisConfig.CAPTURE_INTERVAL = sharedPref.getInt( getString( R.string.pref_id_capture_interval ), 500 );
    }

    private void initializeEnvironment() {
        isVisibleModeMenu = false;
        isVisibleOptionsMenu = false;
        isVisiblePhotoMenu = false;

        HarrisConfig.BMP_HARRIS_RESULT = null;
        HarrisConfig.DOIN_CAPTURE = false;
        HarrisConfig.BD_GALLERY_BACKGROUND = null;
        HarrisConfig.DOIN_FINISH = false;
        HarrisConfig.FILE_PATH = "";

        modeSelectMenuView.setEnableMenu( true );
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                findViewById( R.id.ibCameraMode ).setEnabled( false );
                showShutterButton();

                break;
            case HarrisConfig.GALLERY:
                findViewById( R.id.ibGalleryMode ).setEnabled( false );

                break;
        }
        optionSelectMenuView.updateView();
    }

    private void savePreference() {
        // Flashlight
        spEditor.putInt( getString( R.string.pref_id_flashlight ), HarrisConfig.FLAG_FLASHLIGHT );

        // Guideline
        spEditor.putInt( getString( R.string.pref_id_guideline ), HarrisConfig.FLAG_GUIDELINE );

        // Camera switcher
        spEditor.putInt( getString( R.string.pref_id_camera_switcher ), HarrisConfig.FLAG_CAMERA );

        // Capture interval
        spEditor.putInt( getString( R.string.pref_id_capture_interval ), HarrisConfig.CAPTURE_INTERVAL );

        spEditor.commit();
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            if ( modeSelectMenuView.isVisibleMenu() || optionSelectMenuView.isVisibleMenu()
                    || photoSelectMenuView.isVisibleMenu() ) {
                modeSelectMenuView.hideMenu();
                optionSelectMenuView.hideMenu();
                photoSelectMenuView.hideMenu();

                return false;
            }

            if ( HarrisConfig.DOIN_FINISH == false ) {
                HarrisUtil.toast( this, getString( R.string.msg_ask_quit ) );

                HarrisConfig.DOIN_FINISH = true;

                Handler handler = new HarrisConfig.HandlerAskQuit();
                handler.sendEmptyMessageDelayed( 0, 2000 );

                return false;
            } else {
                android.os.Process.killProcess( android.os.Process.myPid() );
            }
        } else if ( keyCode == KeyEvent.KEYCODE_HOME ) {
            android.os.Process.killProcess( android.os.Process.myPid() );
        }

        return super.onKeyDown( keyCode, event );
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        int x = ( int ) event.getX();
        int y = ( int ) event.getY();

        isVisibleModeMenu = modeSelectMenuView.isVisibleMenu();
        isVisibleOptionsMenu = optionSelectMenuView.isVisibleMenu();
        isVisiblePhotoMenu = photoSelectMenuView.isVisibleMenu();

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                startTrackPointX = x;
                startTrackPointY = y;

                break;
            case MotionEvent.ACTION_MOVE:
                if ( isDisabledTracking ) {
                    return false;
                }

                if ( isPossibleTracking == false ) {
                    if ( isSwipePossible( event ) == false ) {
                        isDisabledTracking = true;
                        return false;
                    } else {
                        return true;
                    }
                }

                animatePointMove( x );

                break;
            case MotionEvent.ACTION_UP:
                isDisabledTracking = false;
                if ( isPossibleTracking ) {
                    isPossibleTracking = false;
                    stopTrackPointX = x;
                    animatePointUp();
                }

                break;
        }

        return true;
    }

    private void animatePointMove( int x ) {
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                if ( isMovingLeftToRight( x ) ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        showingModeMenu( x - startTrackPointX );
                    } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                        hidingOptionMenu( x - startTrackPointX );
                    }
                } else if ( isMovingRightToLeft( x ) ) {
                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                        showingOptionMenu( startTrackPointX - x );
                    } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                        hidingModeMenu( startTrackPointX - x );
                    }
                }

                break;
            case HarrisConfig.GALLERY:
                if ( isMovingLeftToRight( x ) ) {
                    if ( isVisibleModeMenu == false && isVisiblePhotoMenu == false ) {
                        showingModeMenu( x - startTrackPointX );
                    } else if ( isVisibleModeMenu == false && isVisiblePhotoMenu == true ) {
                        hidingPhotoMenu( x - startTrackPointX );
                    }
                } else if ( isMovingRightToLeft( x ) ) {
                    if ( isVisibleModeMenu == false && isVisiblePhotoMenu == false ) {
                        showingPhotoMenu( startTrackPointX - x );
                    } else if ( isVisibleModeMenu == true && isVisiblePhotoMenu == false ) {
                        hidingModeMenu( startTrackPointX - x );
                    }
                }

                break;
        }
    }

    private void animatePointUp() {
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
                    if ( isOnFlingLeftToRight() ) {
                        showModeMenu();
                    } else {
                        hideModeMenu();

                        if ( isOnFlingRightToLeft() ) {
                            showOptionMenu();
                        } else {
                            hideOptionMenu();
                        }
                    }
                } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
                    if ( isOnFlingLeftToRight() ) {
                        hideOptionMenu();
                    } else {
                        showOptionMenu();
                    }
                } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
                    if ( isOnFlingRightToLeft() ) {
                        hideModeMenu();
                    } else {
                        showModeMenu();
                    }
                }

                break;
            case HarrisConfig.GALLERY:
                if ( isVisibleModeMenu == false && isVisiblePhotoMenu == false ) {
                    if ( isOnFlingLeftToRight() ) {
                        showModeMenu();
                    } else {
                        hideModeMenu();

                        if ( isOnFlingRightToLeft() ) {
                            showPhotoMenu();
                        } else {
                            hidePhotoMenu();
                        }
                    }
                } else if ( isVisibleModeMenu == false && isVisiblePhotoMenu == true ) {
                    if ( isOnFlingLeftToRight() ) {
                        hidePhotoMenu();
                    } else {
                        showPhotoMenu();
                    }
                } else if ( isVisibleModeMenu == true && isVisiblePhotoMenu == false ) {
                    if ( isOnFlingRightToLeft() ) {
                        hideModeMenu();
                    } else {
                        showModeMenu();
                    }
                }

                break;
        }
    }

    private boolean isSwipePossible( MotionEvent event ) {
        if ( Math.abs( startTrackPointY - event.getY() ) > HarrisConfig.SWIPE_POSSIBLE_DISTANCE ) {
            isPossibleTracking = false;
            return false;
        }

        if ( Math.abs( startTrackPointX - event.getX() ) > HarrisConfig.SWIPE_POSSIBLE_DISTANCE ) {
            startTrackPointX = ( int ) event.getX();
            isPossibleTracking = true;
            return true;
        }

        return true;
    }

    private boolean isMovingRightToLeft( int x ) {
        return startTrackPointX > x ? true : false;
    }

    private boolean isMovingLeftToRight( int x ) {
        return startTrackPointX < x ? true : false;
    }

    private boolean isOnFlingRightToLeft() {
        return startTrackPointX - stopTrackPointX >= HarrisConfig.SWIPE_MIN_DISTANCE ? true : false;
    }

    private boolean isOnFlingLeftToRight() {
        return stopTrackPointX - startTrackPointX >= HarrisConfig.SWIPE_MIN_DISTANCE ? true : false;
    }

    private void hideModeMenu() {
        modeSelectMenuView.hideMenu();
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                showShutterButton();

                break;
            case HarrisConfig.GALLERY:
                showSubmitButton();

                break;
        }
        isVisibleModeMenu = false;
    }

    private void hideOptionMenu() {
        optionSelectMenuView.hideMenu();
        showShutterButton();
        isVisibleOptionsMenu = false;
    }

    private void hidePhotoMenu() {
        photoSelectMenuView.hideMenu();
        showSubmitButton();
        isVisiblePhotoMenu = false;
    }

    private void hideShutterButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibShutter, "alpha", ibShutter.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibShutter.setEnabled( false );
    }

    private void hideSubmitButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibSubmitEffect, "alpha", ibSubmitEffect.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibSubmitEffect.setEnabled( false );
    }

    private void showModeMenu() {
        modeSelectMenuView.showMenu();
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                hideShutterButton();

                break;
            case HarrisConfig.GALLERY:
                hideSubmitButton();

                break;
        }
        isVisibleModeMenu = true;
    }

    private void showOptionMenu() {
        optionSelectMenuView.setFlashlightEnable( cameraSurfaceView.isFlashlightEnable() );
        optionSelectMenuView.setCameraSwitcherEnable( cameraSurfaceView.isSwitchCameraEnable() );
        optionSelectMenuView.showMenu();
        hideShutterButton();
        isVisibleOptionsMenu = true;
    }

    private void showPhotoMenu() {
        photoSelectMenuView.showMenu();
        hideSubmitButton();
        isVisiblePhotoMenu = true;
    }

    private void showShutterButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibShutter, "alpha", ibShutter.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibShutter.setEnabled( true );
    }

    private void showSubmitButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibSubmitEffect, "alpha", ibSubmitEffect.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibSubmitEffect.setEnabled( true );
    }

    private void showingModeMenu( float distance ) {
        modeSelectMenuView.showingMenu( distance );
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                hidingShutterButton( distance );

                break;
            case HarrisConfig.GALLERY:
                hidingSubmitButton( distance );

                break;
        }
    }

    private void showingOptionMenu( float distance ) {
        optionSelectMenuView.showingMenu( distance );
        hidingShutterButton( distance );
    }

    private void showingPhotoMenu( float distance ) {
        photoSelectMenuView.showingMenu( distance );
        hidingSubmitButton( distance );
    }

    private void showingShutterButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibShutter.setAlpha( ratioDistance );
    }

    private void showingSubmitButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibSubmitEffect.setAlpha( ratioDistance );
    }

    private void hidingModeMenu( float distance ) {
        modeSelectMenuView.hidingMenu( distance );
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                showingShutterButton( distance );

                break;
            case HarrisConfig.GALLERY:
                showingSubmitButton( distance );

                break;
        }
    }

    private void hidingOptionMenu( float distance ) {
        optionSelectMenuView.hidingMenu( distance );
        showingShutterButton( distance );
    }

    private void hidingPhotoMenu( float distance ) {
        photoSelectMenuView.hidingMenu( distance );
        showingSubmitButton( distance );
    }

    private void hidingShutterButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibShutter.setAlpha( 1.0f - ratioDistance );
    }

    private void hidingSubmitButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibSubmitEffect.setAlpha( 1.0f - ratioDistance );
    }

    // TODO : gallery
//    private void askCancelApplyEffect() {
//        AlertDialog.Builder builder = new AlertDialog.Builder( this );
//        builder.setTitle( getString( R.string.app_name ) );
//        builder.setMessage( getString( R.string.msg_ask_cancel ) );
//        builder.setNegativeButton( getString( R.string.no ), new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick( DialogInterface dialog, int which ) {
//                dialog.dismiss();
//                listenerClickMode.onClick( findViewById( R.id.ibGalleryMode ) );
//            }
//        } );
//        builder.setPositiveButton( getString( R.string.yes ), new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick( DialogInterface dialog, int which ) {
//                dialog.dismiss();
//                photoSelectMenuView.clearImageViewDrawable();
//                HarrisConfig.FLAG_MODE = HarrisConfig.CAMERA;
//                if ( HarrisConfig.BD_GALLERY_BACKGROUND != null ) {
//                    HarrisConfig.BD_GALLERY_BACKGROUND.getBitmap().recycle();
//                    HarrisConfig.BD_GALLERY_BACKGROUND = null;
//                }
//                flGalleryModeBackground.setVisibility( View.GONE );
//                modeSelectMenuView.hideMenu();
//                showShutterButton();
//            }
//        } );
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
}