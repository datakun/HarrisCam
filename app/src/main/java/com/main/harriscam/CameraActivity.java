package com.main.harriscam;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.LinearLayout;

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
    // TODO: Programmatically add CameraSurfaceView
    private LinearLayout llCameraContainer;
    private CameraSurfaceView cameraSurfaceView;
    private ModeSelectMenuView modeSelectMenuView;
    private OptionSelectMenuView optionSelectMenuView;
    private PhotoSelectMenuView photoSelectMenuView;
    private FrameLayout flGalleryModeBackground;
    private ImageButton ibShutter;
    private ImageView ivHarrisResult;
    private ImageButton ibSubmitEffect;
    private DrawGuidelineView drawLineView;
    private boolean isInitialized;

    // Control to tracking pointer
    private int startTrackPointX;
    private int stopTrackPointX;
    private int startTrackPointY;
    private boolean isVisibleModeMenu;
    // TODO: Option 메뉴 항상 보이기
//    private boolean isVisibleOptionsMenu;
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
                    if ( photoSelectMenuView.isSelectedAnyPhoto() ) {
                        askCancelApplyEffect();
                    } else {
                        setCameraPreview();
                        optionSelectMenuView.showMenu();
                    }

                    break;
                case R.id.ibGalleryMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.GALLERY;

                    // TODO: Programmatically add CameraSurfaceView
                    llCameraContainer.removeAllViews();
                    cameraSurfaceView.releaseCamera();
                    cameraSurfaceView = null;
                    optionSelectMenuView.hideMenu();

                    if ( HarrisConfig.BD_GALLERY_BACKGROUND == null ) {
                        flGalleryModeBackground.setBackgroundDrawable( new ColorDrawable( Color.BLACK ) );
                    } else {
                        flGalleryModeBackground.setBackgroundDrawable( HarrisConfig.BD_GALLERY_BACKGROUND );
                    }
                    flGalleryModeBackground.setVisibility( View.VISIBLE );
                    modeSelectMenuView.hideMenu();
                    showSubmitButton();
                    ibSubmitEffect.setVisibility( View.VISIBLE );
                    ibShutter.setVisibility( View.GONE );

                    showPhotoMenu();

                    Handler h = new Handler();
                    h.postDelayed( new Runnable() {
                        @Override
                        public void run() {
                            hidePhotoMenu();
                        }
                    }, 1000 );

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

                            if ( HarrisConfig.FLAG_CAMERA == Camera.CameraInfo.CAMERA_FACING_BACK ) {
                                optionSelectMenuView.setFlashlightEnable( true );
                            } else {
                                optionSelectMenuView.setFlashlightEnable( false );
                            }

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

    private View.OnClickListener listenerClickSubmit = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            if ( photoSelectMenuView.isSelectedAllPhoto() ) {
                HarrisConfig.BMP_HARRIS_RESULT = ( ( BitmapDrawable ) ivHarrisResult.getDrawable() ).getBitmap();

                Date now = new Date();
                SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
                HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";

                HarrisUtil.saveBitmapToFileCache( HarrisConfig.BMP_HARRIS_RESULT, HarrisConfig.FILE_PATH + "fx.jpg", 100 );
                HarrisUtil.singleBroadcast( CameraActivity.this, HarrisConfig.FILE_PATH + "fx.jpg" );

                modeSelectMenuView.setEnableMenu( true );
                modeSelectMenuView.setEnableCameraMode( false );

                setCameraPreview();

                HarrisUtil.toast( CameraActivity.this, getString( R.string.msg_saved_image ) );
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_camera );

        isInitialized = false;

        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ( isInitialized == false ) {
            isInitialized = true;

            if ( HarrisConfig.FLAG_MODE == HarrisConfig.CAMERA ) {
                // cameraSurfaceView = ( CameraSurfaceView ) findViewById( R.id.cameraSurfaceView );
                // TODO: Programmatically add CameraSurfaceView
//                cameraSurfaceView = new CameraSurfaceView( CameraActivity.this );
//                llCameraContainer.addView( cameraSurfaceView );
//                cameraSurfaceView.setShutterButton( ibShutter );
                setCameraPreview();
                cameraSurfaceView.setShutterButton( ibShutter );

                showModeMenu();

                final Handler h = new Handler();
                h.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        hideModeMenu();
                    }
                }, 1000 );
            }

            loadPreference();

            initializeEnvironment();
        }
    }

    @Override
    protected void onPause() {
        isInitialized = false;

        if ( HarrisConfig.FLAG_MODE == HarrisConfig.CAMERA ) {
            if ( cameraSurfaceView != null ) {
                llCameraContainer.removeAllViews();
                cameraSurfaceView.releaseCamera();
                cameraSurfaceView = null;
            }
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( data == null ) {
            return;
        }

        InputStream is = HarrisUtil.getISFromURI( this, data.getData() );
        switch ( requestCode ) {
            case HarrisConfig.REQUEST_FIRST:
                photoSelectMenuView.applyFirstPhoto( is );

                break;
            case HarrisConfig.REQUEST_SECOND:
                photoSelectMenuView.applySecondPhoto( is );

                break;
            case HarrisConfig.REQUEST_THIRD:
                photoSelectMenuView.applyThirdPhoto( is );

                break;
        }

        HarrisUtil.logMemInfo( getBaseContext() );

        try {
            is.close();
        } catch ( IOException e ) {
            HarrisUtil.jlog( e );
        }

        photoSelectMenuView.checkEnableApplyEffect();
    }

    private void initializeView() {
        llCameraContainer = ( LinearLayout ) findViewById( R.id.llCameraContainer );
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

        // Save original photo
        HarrisConfig.IS_SAVE_ORIGINAL_IMAGE = sharedPref.getBoolean( getString( R.string.pref_id_save_original ), true );

        // Save original photos of all
        HarrisConfig.IS_SAVE_ORIGINAL_IMAGE_OF_ALL = sharedPref.getBoolean( getString( R.string.pref_id_save_original_of_all ), true );

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
        // TODO: Option 메뉴 항상 보이기
//        isVisibleOptionsMenu = false;
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
            if ( modeSelectMenuView.isVisibleMenu()/* || optionSelectMenuView.isVisibleMenu() */
                    || photoSelectMenuView.isVisibleMenu() ) {

                if ( photoSelectMenuView.isVisibleMenu() ) {
                } else if ( modeSelectMenuView.isVisibleMenu() ) {
                }

                modeSelectMenuView.hideMenu();
                // TODO: Option 메뉴 항상 보이기
//                optionSelectMenuView.hideMenu();
                photoSelectMenuView.hideMenu();

                showSubmitButton();
                showShutterButton();

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

    private void setCameraPreview() {
        // TODO: Programmatically add CameraSurfaceView
        cameraSurfaceView = new CameraSurfaceView( CameraActivity.this );
        llCameraContainer.addView( cameraSurfaceView );

        HarrisConfig.FLAG_MODE = HarrisConfig.CAMERA;
        if ( HarrisConfig.BD_GALLERY_BACKGROUND != null ) {
            HarrisConfig.BD_GALLERY_BACKGROUND.getBitmap().recycle();
            HarrisConfig.BD_GALLERY_BACKGROUND = null;
        }
        photoSelectMenuView.clearImageViewDrawable();
        flGalleryModeBackground.setVisibility( View.GONE );
        modeSelectMenuView.hideMenu();
        showShutterButton();
        ibShutter.setVisibility( View.VISIBLE );
        ibSubmitEffect.setVisibility( View.GONE );
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        int x = ( int ) event.getX();
        int y = ( int ) event.getY();

        isVisibleModeMenu = modeSelectMenuView.isVisibleMenu();
        // TODO: Option 메뉴 항상 보이기
//        isVisibleOptionsMenu = optionSelectMenuView.isVisibleMenu();
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

                if ( cameraSurfaceView != null ) {
                    cameraSurfaceView.setAutoFocus();
                }

                break;
        }

        return true;
    }

    private void animatePointMove( int x ) {
        switch ( HarrisConfig.FLAG_MODE ) {
            case HarrisConfig.CAMERA:
                // TODO: Option 메뉴 항상 보이기
//                if ( isMovingLeftToRight( x ) ) {
//                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
//                        showingModeMenu( x - startTrackPointX );
//                    } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
//                        hidingOptionMenu( x - startTrackPointX );
//                    }
//                } else if ( isMovingRightToLeft( x ) ) {
//                    if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
//                        showingOptionMenu( startTrackPointX - x );
//                    } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
//                        hidingModeMenu( startTrackPointX - x );
//                    }
//                }
                if ( isMovingLeftToRight( x ) ) {
                    if ( isVisibleModeMenu == false ) {
                        showingModeMenu( x - startTrackPointX );
                    }
                } else if ( isMovingRightToLeft( x ) ) {
                    if ( isVisibleModeMenu == true ) {
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
                // TODO: Option 메뉴 항상 보이기
//                if ( isVisibleModeMenu == false && isVisibleOptionsMenu == false ) {
//                    if ( isOnFlingLeftToRight() ) {
//                        showModeMenu();
//                    } else {
//                        hideModeMenu();
//
//                        if ( isOnFlingRightToLeft() ) {
//                            showOptionMenu();
//                        } else {
//                            hideOptionMenu();
//                        }
//                    }
//                } else if ( isVisibleModeMenu == false && isVisibleOptionsMenu == true ) {
//                    if ( isOnFlingLeftToRight() ) {
//                        hideOptionMenu();
//                    } else {
//                        showOptionMenu();
//                    }
//                } else if ( isVisibleModeMenu == true && isVisibleOptionsMenu == false ) {
//                    if ( isOnFlingRightToLeft() ) {
//                        hideModeMenu();
//                    } else {
//                        showModeMenu();
//                    }
//                }
                if ( isVisibleModeMenu == false ) {
                    if ( isOnFlingLeftToRight() ) {
                        showModeMenu();
                    } else {
                        hideModeMenu();
                    }
                } else {
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

    // TODO: Option 메뉴 항상 보이기
//    private void hideOptionMenu() {
//        optionSelectMenuView.hideMenu();
//        showShutterButton();
//        isVisibleOptionsMenu = false;
//    }

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

    // TODO: Option 메뉴 항상 보이기
//    private void showOptionMenu() {
//        optionSelectMenuView.setFlashlightEnable( cameraSurfaceView.isFlashlightEnable() );
//        optionSelectMenuView.setCameraSwitcherEnable( cameraSurfaceView.isSwitchCameraEnable() );
//        optionSelectMenuView.showMenu();
//        hideShutterButton();
//        isVisibleOptionsMenu = true;
//    }

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

    // TODO: Option 메뉴 항상 보이기
//    private void showingOptionMenu( float distance ) {
//        optionSelectMenuView.showingMenu( distance );
//        hidingShutterButton( distance );
//    }

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

    // TODO: Option 메뉴 항상 보이기
//    private void hidingOptionMenu( float distance ) {
//        optionSelectMenuView.hidingMenu( distance );
//        showingShutterButton( distance );
//    }

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

    private void askCancelApplyEffect() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( getString( R.string.app_name ) );
        builder.setMessage( getString( R.string.msg_ask_cancel ) );
        builder.setNegativeButton( getString( R.string.no ), new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
                listenerClickMode.onClick( findViewById( R.id.ibGalleryMode ) );
            }
        } );
        builder.setPositiveButton( getString( R.string.yes ), new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
                setCameraPreview();
            }
        } );

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}