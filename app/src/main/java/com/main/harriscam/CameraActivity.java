package com.main.harriscam;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import kimdata.harriscam.camera.CameraSurfaceView;
import kimdata.harriscam.view.SlideMenuView;

public class CameraActivity extends Activity {
    SlideMenuView slideMenu;
    CameraSurfaceView cameraSurfaceView;
    private View.OnClickListener listenerShutter = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            cameraSurfaceView.takePhotos();
            slideMenu.setEnableShutter( false );

            Harris.toast( getBaseContext(), "Shutter" );
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_camera );

        initView();
    }

    private void initView() {
        HarrisConfig.PATH_SAVE = Harris.makeDir( "/DCIM/harriscam" );

        slideMenu = ( SlideMenuView ) findViewById( R.id.slideMenu );
        cameraSurfaceView = ( CameraSurfaceView ) findViewById( R.id.cameraSurfaceView );

        slideMenu.setOnClickShutter( listenerShutter );
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            if ( slideMenu.isVisibleModeMenu() || slideMenu.isVisibleOptionsMenu() ) {
                slideMenu.hideModeMenu();
                slideMenu.hideOptionMenu();

                return false;
            }

            if ( HarrisConfig.IsEND == false ) {

                Harris.toast( this, getString( R.string.msg_ask_quit ) );

                HarrisConfig.IsEND = true;

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
}