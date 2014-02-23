package com.main.harriscam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.kimdata.kimdatautil.Kimdata;

public class GalleryActivity extends Activity implements View.OnClickListener {
	ImageButton ibFirst, ibSecond, ibThird;
	ImageView ivPicture;
	Button ibCancel, ibSubmit;

	boolean _isInited = false;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_gallery );

		ibFirst = (ImageButton) findViewById( R.id.ibFirst );
		ibSecond = (ImageButton) findViewById( R.id.ibSecond );
		ibThird = (ImageButton) findViewById( R.id.ibThird );
		ivPicture = (ImageView) findViewById( R.id.ivPicture );
		ibCancel = (Button) findViewById( R.id.ibCancel );
		ibSubmit = (Button) findViewById( R.id.ibSubmit );

		ibFirst.setOnClickListener( this );
		ibSecond.setOnClickListener( this );
		ibThird.setOnClickListener( this );
		ibCancel.setOnClickListener( this );
		ibSubmit.setOnClickListener( this );
	}

	@Override
	public void onWindowAttributesChanged( LayoutParams params ) {
		if ( !_isInited ) {
			if ( ivPicture != null ) {
				Kimdata.resizeViewInFrame( ivPicture, HarrisConfig.PREVIEW_W - Kimdata.dp2px( 20, getResources() ), HarrisConfig.PREVIEW_H
						- Kimdata.dp2px( 20, getResources() ) );

				_isInited = true;
			}
		}

		super.onWindowAttributesChanged( params );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.gallery, menu );
		return true;
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event ) {
		if ( keyCode == KeyEvent.KEYCODE_BACK ) {
			askCancel();
		}

		return super.onKeyDown( keyCode, event );
	}

	private void askCancel() {
		AlertDialog.Builder builder = new Builder( this );
		builder.setTitle( "Harris Cam" );
		builder.setMessage( "정말 취소하시겠습니까?" );
		builder.setNegativeButton( "아니오", new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				dialog.dismiss();
			}
		} );
		builder.setPositiveButton( "예", new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				dialog.dismiss();
				finish();
			}
		} );

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick( View v ) {
		switch ( v.getId() ) {
		case R.id.ibFirst:

			break;
		case R.id.ibSecond:

			break;
		case R.id.ibThird:

			break;
		case R.id.ibCancel:
			askCancel();

			break;
		case R.id.ibSubmit:

			break;
		}
	}

}
