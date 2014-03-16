package com.main.harriscam;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.kimdata.harriscam.na.NativeHarrisCam;
import com.kimdata.kimdatautil.Kimdata;

public class GalleryActivity extends Activity implements View.OnClickListener {
	private final int CODE_FIRST = 101;
	private final int CODE_SECOND = 102;
	private final int CODE_THIRD = 103;

	ImageButton ibFirst, ibSecond, ibThird;
	ImageView ivPicture;
	Button ibCancel, ibSubmit;
	byte[] byImage1, byImage2, byImage3;
	boolean isFirst = false;
	boolean isSecond = false;
	boolean isThird = false;

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

		ibSecond.setEnabled( false );
		ibThird.setEnabled( false );
	}

	@Override
	protected void onDestroy() {
		Kimdata.unbindViewDrawable( ibFirst );
		Kimdata.unbindViewDrawable( ibSecond );
		Kimdata.unbindViewDrawable( ibThird );
		// Kimdata.unbindViewDrawable( ivPicture );

		super.onDestroy();
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
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		if ( data == null ) {
			return;
		}

		InputStream is = Kimdata.getISFromURI( this, data.getData() );
		switch ( requestCode ) {
		case CODE_FIRST:
			isFirst = true;
			Kimdata.unbindViewDrawable( ibFirst );
			ibFirst.setImageBitmap( BitmapFactory.decodeStream( is ) );
			byImage1 = null;
			System.gc();
			byImage1 = Kimdata.bitmapToByteArray( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap() );

			if ( isSecond ) {
				Kimdata.unbindViewDrawable( ibSecond );
				if ( HarrisConfig.SCALE_FILL ) {
					ibSecond.setImageBitmap( scaleToFillBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), byImage2 ) );
				} else {
					ibSecond.setImageBitmap( scaleToStretchBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), byImage2 ) );
				}
			}

			if ( isThird ) {
				Kimdata.unbindViewDrawable( ibThird );
				if ( HarrisConfig.SCALE_FILL ) {
					ibThird.setImageBitmap( scaleToFillBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), byImage3 ) );
				} else {
					ibThird.setImageBitmap( scaleToStretchBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), byImage3 ) );
				}
			}

			ibSecond.setEnabled( true );
			ibThird.setEnabled( true );

			break;
		case CODE_SECOND:
			isSecond = true;
			Kimdata.unbindViewDrawable( ibSecond );
			if ( HarrisConfig.SCALE_FILL ) {
				ibSecond.setImageBitmap( scaleToFillBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), is ) );
			} else {
				ibSecond.setImageBitmap( scaleToStretchBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), is ) );
			}
			byImage2 = null;
			System.gc();
			byImage2 = Kimdata.bitmapToByteArray( ( (BitmapDrawable) ibSecond.getDrawable() ).getBitmap() );

			break;
		case CODE_THIRD:
			isThird = true;
			Kimdata.unbindViewDrawable( ibThird );
			if ( HarrisConfig.SCALE_FILL ) {
				ibThird.setImageBitmap( scaleToFillBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), is ) );
			} else {
				ibThird.setImageBitmap( scaleToStretchBitmap( ( (BitmapDrawable) ibFirst.getDrawable() ).getBitmap(), is ) );
			}
			byImage3 = null;
			System.gc();
			byImage3 = Kimdata.bitmapToByteArray( ( (BitmapDrawable) ibThird.getDrawable() ).getBitmap() );

			break;
		}
		try {
			is.close();
		} catch ( IOException e ) {
			Kimdata.jlog( e );
		}

		if ( isFirst && isSecond && isThird ) {
			HarrisConfig.BMP_HARRIS = BitmapFactory.decodeByteArray( byImage1, 0, byImage1.length );
			NativeHarrisCam.naApplyHarris( HarrisConfig.BMP_HARRIS, ( (BitmapDrawable) ibSecond.getDrawable() ).getBitmap(),
					( (BitmapDrawable) ibThird.getDrawable() ).getBitmap() );
			Kimdata.unbindViewDrawable( ivPicture );
			ivPicture.setImageBitmap( HarrisConfig.BMP_HARRIS );

			Kimdata.logMemInfo( (ActivityManager) getSystemService( ACTIVITY_SERVICE ) );
		}
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
		Intent intent = new Intent( Intent.ACTION_PICK );
		intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE );
		switch ( v.getId() ) {
		case R.id.ibFirst:
			startActivityForResult( intent, CODE_FIRST );

			break;
		case R.id.ibSecond:
			startActivityForResult( intent, CODE_SECOND );

			break;
		case R.id.ibThird:
			startActivityForResult( intent, CODE_THIRD );

			break;
		case R.id.ibCancel:
			askCancel();

			break;
		case R.id.ibSubmit:
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
			HarrisConfig.PATH_FILE = HarrisConfig.PATH_SAVE + "/harriscam_" + format.format( now ) + "_";

			startActivity( new Intent( GalleryActivity.this, EditorActivity.class ) );

			finish();

			break;
		}
	}

	private Bitmap scaleToFillBitmap( Bitmap dst, InputStream is ) {
		Bitmap src = BitmapFactory.decodeStream( is );

		float scaled = 1.0f;
		if ( (float) dst.getWidth() / (float) src.getWidth() < (float) dst.getHeight() / (float) src.getHeight() ) {
			scaled = (float) dst.getHeight() / (float) src.getHeight();
		} else {
			scaled = (float) dst.getWidth() / (float) src.getWidth();
		}

		Bitmap bmpScaled = Bitmap.createScaledBitmap( src, (int) Math.ceil( src.getWidth() * scaled ),
				(int) Math.ceil( src.getHeight() * scaled ), true );
		// src.recycle(); BitmapFactory로 만든 것들을 recylce하면, 그 원본을 이용하여 Bitmap.create~한 객체들을 사용할 수 없게 된다. 그래서 주석처리

		Kimdata.logMemInfo( (ActivityManager) getSystemService( ACTIVITY_SERVICE ) );

		int offsetX = 0;
		int offsetY = 0;
		offsetX = bmpScaled.getWidth() - dst.getWidth() != 0 ? ( bmpScaled.getWidth() - dst.getWidth() ) / 2 : 0;
		offsetY = bmpScaled.getHeight() - dst.getHeight() != 0 ? ( bmpScaled.getHeight() - dst.getHeight() ) / 2 : 0;

		return Bitmap.createBitmap( bmpScaled, offsetX, offsetY, dst.getWidth(), dst.getHeight() );
	}

	private Bitmap scaleToFillBitmap( Bitmap dst, byte[] byImage ) {
		Bitmap src = BitmapFactory.decodeByteArray( byImage, 0, byImage.length );

		float scaled = 1.0f;
		if ( (float) dst.getWidth() / (float) src.getWidth() < (float) dst.getHeight() / (float) src.getHeight() ) {
			scaled = (float) dst.getHeight() / (float) src.getHeight();
		} else {
			scaled = (float) dst.getWidth() / (float) src.getWidth();
		}

		Bitmap bmpScaled = Bitmap.createScaledBitmap( src, (int) Math.ceil( src.getWidth() * scaled ),
				(int) Math.ceil( src.getHeight() * scaled ), true );
		// src.recycle(); BitmapFactory로 만든 것들을 recylce하면, 그 원본을 이용하여 Bitmap.create~한 객체들을 사용할 수 없게 된다. 그래서 주석처리

		Kimdata.logMemInfo( (ActivityManager) getSystemService( ACTIVITY_SERVICE ) );

		int offsetX = 0;
		int offsetY = 0;
		offsetX = bmpScaled.getWidth() - dst.getWidth() != 0 ? ( bmpScaled.getWidth() - dst.getWidth() ) / 2 : 0;
		offsetY = bmpScaled.getHeight() - dst.getHeight() != 0 ? ( bmpScaled.getHeight() - dst.getHeight() ) / 2 : 0;

		return Bitmap.createBitmap( bmpScaled, offsetX, offsetY, dst.getWidth(), dst.getHeight() );
	}

	private Bitmap scaleToStretchBitmap( Bitmap dst, InputStream is ) {
		Bitmap src = BitmapFactory.decodeStream( is );

		Kimdata.logMemInfo( (ActivityManager) getSystemService( ACTIVITY_SERVICE ) );

		return Bitmap.createScaledBitmap( src, dst.getWidth(), dst.getHeight(), true );
	}

	private Bitmap scaleToStretchBitmap( Bitmap dst, byte[] byImage ) {
		Bitmap src = BitmapFactory.decodeByteArray( byImage, 0, byImage.length );

		Kimdata.logMemInfo( (ActivityManager) getSystemService( ACTIVITY_SERVICE ) );

		return Bitmap.createScaledBitmap( src, dst.getWidth(), dst.getHeight(), true );
	}
}
