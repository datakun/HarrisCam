package com.main.harriscam;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.kimdata.kimdatautil.Kimdata;

public class EditorActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
	ImageView ivPicture;
	HorizontalScrollView hsvEffects;
	Button ibFilter, ibTransform, ibRestore, ibApply;

	boolean _isInited = false;

	ThreadFileSaver _thread;
	long timeSave = 0;

	ProgressDialog progDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_editor );

		ivPicture = (ImageView) findViewById( R.id.ivPicture );
		hsvEffects = (HorizontalScrollView) findViewById( R.id.hsvEffects );
		ibFilter = (Button) findViewById( R.id.ibFilter );
		ibTransform = (Button) findViewById( R.id.ibTransform );
		ibRestore = (Button) findViewById( R.id.ibRestore );
		ibApply = (Button) findViewById( R.id.ibApply );

		ivPicture.setOnTouchListener( this );
		ibFilter.setOnClickListener( this );
		ibTransform.setOnClickListener( this );
		ibRestore.setOnClickListener( this );
		ibApply.setOnClickListener( this );

		ivPicture.setImageBitmap( HarrisConfig.BMP_HARRIS );

		progDialog = new ProgressDialog( this );
		progDialog.setMessage( "파일을 저장하는 중입니다..." );
		progDialog.setIndeterminate( true );
		progDialog.setCancelable( false );
	}

	@Override
	public void onWindowFocusChanged( boolean hasFocus ) {
		if ( !_isInited ) {
			if ( ivPicture != null ) {
				Kimdata.resizeViewInFrame( ivPicture, HarrisConfig.PREVIEW_W - Kimdata.dp2px( 20, getResources() ), HarrisConfig.PREVIEW_H
						- Kimdata.dp2px( 20, getResources() ) );

				Kimdata.positionViewInFrame( ivPicture, ( HarrisConfig.LAYOUT_W - ivPicture.getWidth() ) / 4, ( HarrisConfig.LAYOUT_H
						- ivPicture.getHeight() - Kimdata.dp2px( 121, getResources() ) ) / 2 );

				Kimdata.jlog( ( HarrisConfig.LAYOUT_W - ivPicture.getWidth() ) / 2 + " : "
						+ ( HarrisConfig.LAYOUT_H - ivPicture.getHeight() - Kimdata.dp2px( 60, getResources() ) ) / 2 );

				_isInited = true;
			}
		}

		super.onWindowFocusChanged( hasFocus );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.editor, menu );
		return true;
	}

	private void askApply() {
		AlertDialog.Builder builder = new Builder( this );
		builder.setTitle( "Harris Cam" );
		builder.setMessage( "이미지를 저장하시겠습니까?" );
		builder.setNegativeButton( "아니오", new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				dialog.dismiss();
			}
		} );
		builder.setPositiveButton( "예", new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				Kimdata.SaveBitmapToFileCache( HarrisConfig.BMP_HARRIS, HarrisConfig.PATH_FILE + "fx.jpg", 100 );
				Kimdata.singleBroadcast( EditorActivity.this, HarrisConfig.PATH_FILE + "fx.jpg" );
				timeSave = System.currentTimeMillis();

				progDialog.show();

				if ( _thread != null ) {
					_thread.interrupt();
					_thread = null;
				}
				_thread = new ThreadFileSaver();
				_thread.start();

				dialog.dismiss();
			}
		} );

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick( View v ) {
		switch ( v.getId() ) {
		case R.id.ibFilter:

			break;
		case R.id.ibTransform:

			break;
		case R.id.ibRestore:

			break;
		case R.id.ibApply:
			askApply();

			break;
		}
	}

	@Override
	public boolean onTouch( View v, MotionEvent event ) {
		switch ( v.getId() ) {
		case R.id.ivPicture:
			switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN:
				// 원본 사진

				break;
			case MotionEvent.ACTION_UP:
				// 원본 사진 해제

				break;
			}

			break;
		}

		return false;
	}

	public class ThreadFileSaver extends Thread implements Runnable {

		@Override
		public void run() {
			while ( !( new File( HarrisConfig.PATH_FILE + "fx.jpg" ).exists() ) ) {
				if ( System.currentTimeMillis() - timeSave > 10000 ) {
					( (Activity) EditorActivity.this ).runOnUiThread( new Runnable() {

						@Override
						public void run() {
							if ( progDialog != null ) {
								progDialog.dismiss();
							}

							Kimdata.toast( EditorActivity.this, "사진을 저장하지 못했습니다." );
						}
					} );

					Thread.currentThread().interrupt();

					return;
				}
			}

			( (Activity) EditorActivity.this ).runOnUiThread( new Runnable() {

				@Override
				public void run() {
					Kimdata.toast( EditorActivity.this, "사진이 저장되었습니다." );

					if ( progDialog != null ) {
						progDialog.dismiss();
					}
				}
			} );

			startActivity( new Intent( EditorActivity.this, ShareActivity.class ) );
			finish();

			Thread.currentThread().interrupt();
		}
	}

}
