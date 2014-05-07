package com.main.harriscam;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;
import com.view.harriscam.CameraSurfaceView;
import com.view.harriscam.ModeSelectMenuView;
import com.view.harriscam.OptionSelectMenuView;
import com.view.harriscam.PhotoSelectMenuView;

public class CameraActivity extends Activity {
    // Constants for swipe
    private static int SWIPE_MIN_DISTANCE;
    private static int SWIPE_POSSIBLE_DISTANCE;

    // Views
    private CameraSurfaceView cameraSurfaceView;
    private ModeSelectMenuView modeSelectMenuView;
    private OptionSelectMenuView optionSelectMenuView;
    private PhotoSelectMenuView photoSelectMenuView;
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

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_camera );

        initializeOfView();

        isVisibleModeMenu = false;
        isVisibleOptionsMenu = false;
        isVisiblePhotoMenu = false;

        HarrisConfig.SAVE_PATH = HarrisUtil.makeDir( "/DCIM/harriscam" );
    }

    @Override
    protected void onPause() {
        cameraSurfaceView.releaseCamera();

        super.onPause();
    }

    private void initializeOfView() {
        cameraSurfaceView = ( CameraSurfaceView ) findViewById( R.id.cameraSurfaceView );
        modeSelectMenuView = ( ModeSelectMenuView ) findViewById( R.id.modeSelectMenu );
        optionSelectMenuView = ( OptionSelectMenuView ) findViewById( R.id.optionSelectMenu );
        photoSelectMenuView = ( PhotoSelectMenuView ) findViewById( R.id.photoSelectMenu );
        ibShutter = ( ImageButton ) findViewById( R.id.ibShutter );

        ibShutter.setOnClickListener( listenerShutter );
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

            if ( HarrisConfig.IS_FINISHING == false ) {
                HarrisUtil.toast( this, getString( R.string.msg_ask_quit ) );

                HarrisConfig.IS_FINISHING = true;

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
    public void onWindowFocusChanged( boolean hasFocus ) {
        super.onWindowFocusChanged( hasFocus );

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( metrics );

        SWIPE_MIN_DISTANCE = metrics.widthPixels / 6;
        SWIPE_POSSIBLE_DISTANCE = SWIPE_MIN_DISTANCE / 2;
    }

    @Override
    public boolean onTouchEvent( MotionEvent event ) {
        int x = ( int ) event.getX();
        int y = ( int ) event.getY();

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
            case SETTINGS:
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
                    }
                    if ( isOnFlingRightToLeft() ) {
                        showOptionMenu();
                    } else {
                        hideOptionMenu();
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
                    }
                    if ( isOnFlingRightToLeft() ) {
                        showPhotoMenu();
                    } else {
                        hidePhotoMenu();
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
            case SETTINGS:
                break;
        }
    }

    private boolean isSwipePossible( MotionEvent event ) {
        if ( Math.abs( startTrackPointY - event.getY() ) > SWIPE_POSSIBLE_DISTANCE ) {
            isPossibleTracking = false;
            return false;
        }

        if ( Math.abs( startTrackPointX - event.getX() ) > SWIPE_POSSIBLE_DISTANCE ) {
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
        return startTrackPointX - stopTrackPointX >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private boolean isOnFlingLeftToRight() {
        return stopTrackPointX - startTrackPointX >= SWIPE_MIN_DISTANCE ? true : false;
    }

    private void hideModeMenu() {
        modeSelectMenuView.hideMenu();
        isVisibleModeMenu = false;
    }

    private void hideOptionMenu() {
        optionSelectMenuView.hideMenu();
        isVisibleOptionsMenu = false;
    }

    private void hidePhotoMenu() {
        photoSelectMenuView.hideMenu();
        isVisiblePhotoMenu = false;
    }

    private void showModeMenu() {
        modeSelectMenuView.showMenu();
        isVisibleModeMenu = true;
    }

    private void showOptionMenu() {
        optionSelectMenuView.showMenu();
        isVisibleOptionsMenu = true;
    }

    private void showPhotoMenu() {
        photoSelectMenuView.showMenu();
        isVisiblePhotoMenu = true;
    }

    private void showingModeMenu( float distance ) {
        modeSelectMenuView.showingMenu( distance );
    }

    private void showingOptionMenu( float distance ) {
        optionSelectMenuView.showingMenu( distance );
    }

    private void showingPhotoMenu( float distance ) {
        photoSelectMenuView.showingMenu( distance );
    }

    private void hidingModeMenu( float distance ) {
        modeSelectMenuView.hidingMenu( distance );
    }

    private void hidingOptionMenu( float distance ) {
        optionSelectMenuView.hidingMenu( distance );
    }

    private void hidingPhotoMenu( float distance ) {
        photoSelectMenuView.hidingMenu( distance );
    }
}