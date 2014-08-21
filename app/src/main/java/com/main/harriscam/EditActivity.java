package com.main.harriscam;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;
import com.view.harriscam.PhotoSelectMenuView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends Activity {
    private LinearLayout llBackground;
    private ActionBar actionBar;
    private boolean isInitialized = false;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit );

        actionBar = getActionBar();
        if ( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled( true );
        }

        ImageView ivHarrisResult = ( ImageView ) findViewById( R.id.ivHarrisResult );
        llBackground = ( LinearLayout ) findViewById( R.id.llBackground );

        ivHarrisResult.setImageBitmap( HarrisConfig.BMP_HARRIS_RESULT );
    }

    @Override
    public void onWindowFocusChanged( boolean hasFocus ) {
        if ( isInitialized == false ) {
            // llBackground를 포함하는 것의 LayoutParams를 반환
            FrameLayout.LayoutParams params = ( FrameLayout.LayoutParams ) llBackground.getLayoutParams();
            params.height = llBackground.getHeight() - ( int ) getResources().getDimension( R.dimen.option_menu_size )
                    - actionBar.getHeight();
            llBackground.setLayoutParams( params );

            isInitialized = true;
        }

        super.onWindowFocusChanged( hasFocus );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.edit, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                askCancelApplyEffect();

                return true;
            case R.id.action_done:
                saveApplyEffect();

                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            askCancelApplyEffect();

            return false;
        }

        return super.onKeyDown( keyCode, event );
    }

    private void saveApplyEffect() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
        HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";

        HarrisUtil.saveBitmapToFileCache( HarrisConfig.BMP_HARRIS_RESULT, HarrisConfig.FILE_PATH + "fx.jpg", 100 );
        HarrisUtil.singleBroadcast( EditActivity.this, HarrisConfig.FILE_PATH + "fx.jpg" );

        HarrisUtil.toast( EditActivity.this, getString( R.string.msg_saved_image ) );

        finish();
    }

    private void askCancelApplyEffect() {
        AlertDialog.Builder builder = new AlertDialog.Builder( EditActivity.this );
        builder.setTitle( getString( R.string.app_name ) );
        builder.setMessage( getString( R.string.msg_ask_cancel ) );
        builder.setNegativeButton( getString( R.string.no ), new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
            }
        } );
        builder.setPositiveButton( getString( R.string.yes ), new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
                if ( HarrisConfig.BD_GALLERY_BACKGROUND != null ) {
                    HarrisConfig.BD_GALLERY_BACKGROUND.getBitmap().recycle();
                    HarrisConfig.BD_GALLERY_BACKGROUND = null;
                }
                finish();
            }
        } );

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
