package com.main.harriscam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.kimdata.camera.CameraPreview;
import com.kimdata.kimdatautil.Kimdata;

public class GalleryActivity extends Activity implements View.OnClickListener {
	private final int CODE_FIRST = 101;
	private final int CODE_SECOND = 102;
	private final int CODE_THIRD = 103;

	ImageButton ibFirst, ibSecond, ibThird;
	ImageView ivPicture;
	Button ibCancel, ibSubmit;
	String imgPath1, imgPath2, imgPath3;
	Bitmap bmpImage[];

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

		bmpImage = new Bitmap[3];
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
		switch ( requestCode ) {
		case CODE_FIRST:
			imgPath1 = Kimdata.getRealPathFromURI( this, data.getData() );
			if ( bmpImage[0] != null ) {
				bmpImage[0].recycle();
				bmpImage[0] = null;
			}
			bmpImage[0] = BitmapFactory.decodeFile( imgPath1 );
			Kimdata.unbindViewDrawable( ibFirst );
			ibFirst.setImageBitmap( bmpImage[0] );
			// ibFirst.setImageURI( data.getData() );
			// ibFirst.setBackgroundDrawable( Drawable.createFromPath( imgPath1 ) );

			break;
		case CODE_SECOND:
			imgPath2 = Kimdata.getRealPathFromURI( this, data.getData() );
			if ( bmpImage[1] != null ) {
				bmpImage[1].recycle();
				bmpImage[1] = null;
			}
			bmpImage[1] = BitmapFactory.decodeFile( imgPath2 );
			Kimdata.unbindViewDrawable( ibSecond );
			ibSecond.setImageBitmap( bmpImage[1] );
			// ibSecond.setImageURI( data.getData() );
			// ibSecond.setBackgroundDrawable( Drawable.createFromPath( imgPath2 ) );

			break;
		case CODE_THIRD:
			imgPath3 = Kimdata.getRealPathFromURI( this, data.getData() );
			if ( bmpImage[2] != null ) {
				bmpImage[2].recycle();
				bmpImage[2] = null;
			}
			bmpImage[2] = BitmapFactory.decodeFile( imgPath3 );
			Kimdata.unbindViewDrawable( ibThird );
			ibThird.setImageBitmap( bmpImage[2] );
			// ibThird.setImageURI( data.getData() );
			// ibThird.setBackgroundDrawable( Drawable.createFromPath( imgPath3 ) );

			break;
		}

		synchronized ( bmpImage[0] ) {
			CameraPreview.naApplyHarris( bmpImage[0], bmpImage[1], bmpImage[2] );
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

			break;
		}
	}
}
