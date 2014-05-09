package com.main.harriscam;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;
import com.view.harriscam.CameraSurfaceView;
import com.view.harriscam.ModeSelectMenuView;
import com.view.harriscam.OptionSelectMenuView;
import com.view.harriscam.PhotoSelectMenuView;

public class CameraActivity extends Activity {
    // Views
    private CameraSurfaceView cameraSurfaceView;
    private ModeSelectMenuView modeSelectMenuView;
    private OptionSelectMenuView optionSelectMenuView;
    private PhotoSelectMenuView photoSelectMenuView;
    private FrameLayout flGalleryModeBackground;
    private ImageButton ibShutter;

    // Control to tracking pointer
    private int startTrackPointX;
    private int stopTrackPointX;
    private int startTrackPointY;
    private boolean isVisibleModeMenu;
    private boolean isVisibleOptionsMenu;
    private boolean isVisiblePhotoMenu;
    private boolean isPossibleTracking;
    private boolean isDisabledTracking;

    // Listner
    private View.OnClickListener listenerShutter = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            cameraSurfaceView.takePhotos();
            ibShutter.setEnabled( false );

            HarrisUtil.toast( getBaseContext(), "Shutter" );
        }
    };
    // Listner
    private View.OnClickListener listenerClickMenu = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            modeSelectMenuView.setEnableMenu( true );
            v.setEnabled( false );

            switch ( v.getId() ) {
                case R.id.ibCameraMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.CAMERA;
                    HarrisConfig.BD_GALLERY_BACKGROUND.getBitmap().recycle();
                    HarrisConfig.BD_GALLERY_BACKGROUND = null;
                    flGalleryModeBackground.setVisibility( View.GONE );
                    modeSelectMenuView.hideMenu();
                    showShutterButton();

                    break;
                case R.id.ibGalleryMode:
                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.GALLERY;

                    checkGalleryBackground();

                    flGalleryModeBackground.setBackgroundDrawable( HarrisConfig.BD_GALLERY_BACKGROUND );
                    flGalleryModeBackground.setVisibility( View.VISIBLE );
                    modeSelectMenuView.hideMenu();

                    break;
                case R.id.ibSettings:
//                    HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.SETTINGS;
                    modeSelectMenuView.hideMenu();
                    startActivity( new Intent( CameraActivity.this, SettingsActivity.class ) );
                    modeSelectMenuView.setEnableMenu( true );
                    findViewById( R.id.ibCameraMode ).setEnabled( false );

                    break;
            }
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

        initializePreference();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeEnvironment();
    }

    @Override
    protected void onPause() {
        cameraSurfaceView.releaseCamera();

        super.onPause();
    }

    private void initializeView() {
        cameraSurfaceView = ( CameraSurfaceView ) findViewById( R.id.cameraSurfaceView );
        modeSelectMenuView = ( ModeSelectMenuView ) findViewById( R.id.modeSelectMenu );
        optionSelectMenuView = ( OptionSelectMenuView ) findViewById( R.id.optionSelectMenu );
        photoSelectMenuView = ( PhotoSelectMenuView ) findViewById( R.id.photoSelectMenu );
        flGalleryModeBackground = ( FrameLayout ) findViewById( R.id.flGalleryModeBackground );
        modeSelectMenuView.setOnMenuClickListener( listenerClickMenu );
        ibShutter = ( ImageButton ) findViewById( R.id.ibShutter );
        ibShutter.setOnClickListener( listenerShutter );
        cameraSurfaceView.setShutterButton( ibShutter );

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize( size );
        HarrisConfig.SWIPE_MAX_DISTANCE = size.x / 4;
        HarrisConfig.SWIPE_MIN_DISTANCE = HarrisConfig.SWIPE_MAX_DISTANCE / 2;
        HarrisConfig.SWIPE_POSSIBLE_DISTANCE = HarrisConfig.SWIPE_MIN_DISTANCE / 2;
    }

    private void initializePreference() {
        // TODO: SharedPreference
        HarrisConfig.FLAG_MODE = HarrisConfig.VIEW_MODE.CAMERA;
        HarrisConfig.CAPTURE_INTERVAL = 500;
        HarrisConfig.FLAG_FLASHLIGHT = HarrisConfig.FLASH_MODE.OFF;
        HarrisConfig.SAVE_PATH = HarrisUtil.makeDir( "/DCIM/harriscam" );
        HarrisConfig.IS_SAVE_ORIGINAL_IMAGE = true;
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
                finish();
            }
        } else if ( keyCode == KeyEvent.KEYCODE_HOME ) {
            finish();
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
            case CAMERA:
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
            case GALLERY:
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
            case CAMERA:
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
            case GALLERY:
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
        if ( HarrisConfig.FLAG_MODE == HarrisConfig.VIEW_MODE.CAMERA )
            showShutterButton();
        isVisibleModeMenu = false;
    }

    private void hideOptionMenu() {
        optionSelectMenuView.hideMenu();
        showShutterButton();
        isVisibleOptionsMenu = false;
    }

    private void hidePhotoMenu() {
        photoSelectMenuView.hideMenu();
        isVisiblePhotoMenu = false;
    }

    private void hideShutterButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibShutter, "alpha", ibShutter.getAlpha(), 0.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibShutter.setEnabled( false );
    }

    private void showModeMenu() {
        modeSelectMenuView.showMenu();
        if ( HarrisConfig.FLAG_MODE == HarrisConfig.VIEW_MODE.CAMERA )
            hideShutterButton();
        isVisibleModeMenu = true;
    }

    private void showOptionMenu() {
        optionSelectMenuView.showMenu();
        hideShutterButton();
        isVisibleOptionsMenu = true;
    }

    private void showPhotoMenu() {
        photoSelectMenuView.showMenu();
        isVisiblePhotoMenu = true;
    }

    private void showShutterButton() {
        ObjectAnimator aniAlpha = ObjectAnimator.ofFloat( ibShutter, "alpha", ibShutter.getAlpha(), 1.0f );
        aniAlpha.setDuration( 300 );
        aniAlpha.setInterpolator( new AccelerateDecelerateInterpolator() );

        aniAlpha.start();

        ibShutter.setEnabled( true );
    }

    private void showingModeMenu( float distance ) {
        modeSelectMenuView.showingMenu( distance );
        if ( HarrisConfig.FLAG_MODE == HarrisConfig.VIEW_MODE.CAMERA )
            hidingShutterButton( distance );
    }

    private void showingOptionMenu( float distance ) {
        optionSelectMenuView.showingMenu( distance );
        hidingShutterButton( distance );
    }

    private void showingPhotoMenu( float distance ) {
        photoSelectMenuView.showingMenu( distance );
    }

    private void showingShutterButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibShutter.setAlpha( ratioDistance );
    }

    private void hidingModeMenu( float distance ) {
        modeSelectMenuView.hidingMenu( distance );
        if ( HarrisConfig.FLAG_MODE == HarrisConfig.VIEW_MODE.CAMERA )
            showingShutterButton( distance );
    }

    private void hidingOptionMenu( float distance ) {
        optionSelectMenuView.hidingMenu( distance );
        showingShutterButton( distance );
    }

    private void hidingPhotoMenu( float distance ) {
        photoSelectMenuView.hidingMenu( distance );
    }

    private void hidingShutterButton( float distance ) {
        if ( distance <= 1 ) distance = 1;
        float ratioDistance = distance / HarrisConfig.SWIPE_MAX_DISTANCE;
        if ( ratioDistance >= 1.0f ) ratioDistance = 1.0f;

        ibShutter.setAlpha( 1.0f - ratioDistance );
    }
}