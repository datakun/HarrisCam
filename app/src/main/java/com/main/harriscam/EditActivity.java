package com.main.harriscam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;
import com.view.harriscam.PhotoSelectMenuView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends Activity {
    FrameLayout flGalleryModeBackground;
    ImageView ivHarrisResult;
    ImageButton ibCancelSave;
    ImageButton ibSubmitSave;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit );

        flGalleryModeBackground = ( FrameLayout ) findViewById( R.id.flGalleryModeBackground );
        ivHarrisResult = ( ImageView ) findViewById( R.id.ivHarrisResult );
        ibCancelSave = ( ImageButton ) findViewById( R.id.ibCancelSave );
        ibSubmitSave = ( ImageButton ) findViewById( R.id.ibSubmitSave );

        ibCancelSave.setOnClickListener( listenerClickCancel );
        ibSubmitSave.setOnClickListener( listenerClickSubmit );

        HarrisConfig.checkGalleryBackground();

        flGalleryModeBackground.setBackgroundDrawable( HarrisConfig.BD_GALLERY_BACKGROUND );

        ivHarrisResult.setImageBitmap( HarrisConfig.BMP_HARRIS_RESULT );
    }

    private View.OnClickListener listenerClickSubmit = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
            HarrisConfig.FILE_PATH = HarrisConfig.SAVE_PATH + "/harriscam_" + format.format( now ) + "_";

            HarrisUtil.saveBitmapToFileCache( HarrisConfig.BMP_HARRIS_RESULT, HarrisConfig.FILE_PATH + "fx.jpg", 100 );
            HarrisUtil.singleBroadcast( EditActivity.this, HarrisConfig.FILE_PATH + "fx.jpg" );

            HarrisUtil.toast( EditActivity.this, getString( R.string.msg_saved_image ) );
            finish();
        }
    };

    private View.OnClickListener listenerClickCancel = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
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
    };
}
