package com.main.harriscam;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.kimdata.kimdatautil.Kimdata;

public class ShareActivity extends Activity implements View.OnClickListener {
	ImageView ivPicture;
	ImageButton ibTwitter, ibFacebook, ibTumblr;
	Button ibDone;

	boolean _isInited = false;

	ProgressDialog progDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_share );

		ivPicture = (ImageView) findViewById( R.id.ivPicture );
		ibTwitter = (ImageButton) findViewById( R.id.ibTwitter );
		ibFacebook = (ImageButton) findViewById( R.id.ibFacebook );
		ibTumblr = (ImageButton) findViewById( R.id.ibGooglePlus );
		ibDone = (Button) findViewById( R.id.ibDone );

		ibTwitter.setOnClickListener( this );
		ibFacebook.setOnClickListener( this );
		ibTumblr.setOnClickListener( this );
		ibDone.setOnClickListener( this );

		ivPicture.setImageBitmap( HarrisConfig.BMP_HARRIS );

		progDialog = new ProgressDialog( this );
		progDialog.setMessage( "사진을 등록하는 중입니다..." );
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

				Kimdata.jlog( HarrisConfig.LAYOUT_W + ": " + HarrisConfig.LAYOUT_H );

				_isInited = true;
			}
		}

		super.onWindowFocusChanged( hasFocus );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.share, menu );
		return true;
	}

	@Override
	public void onClick( View v ) {
		SocialAuthAdapter _aAuth = new SocialAuthAdapter( new ResponseListener() );

		if ( progDialog != null ) {
			progDialog.show();
		}

		switch ( v.getId() ) {
		case R.id.ibTwitter:
			try {
				_aAuth.addProvider( Provider.TWITTER, R.drawable.twitter );
				_aAuth.addCallBack( Provider.TWITTER, "https://github.com/datakun/HarrisCam" );
				_aAuth.authorize( ShareActivity.this, Provider.TWITTER );
				_aAuth.uploadImageAsync( "#HarrisCam", HarrisConfig.PATH_FILE + "fx2.jpg", HarrisConfig.BMP_HARRIS, 100,
						new UploadImageListener() );
			} catch ( Exception e ) {
				Kimdata.jlog( e );
			}

			break;
		case R.id.ibFacebook:
			try {
				_aAuth.addProvider( Provider.FACEBOOK, R.drawable.facebook );
				_aAuth.authorize( ShareActivity.this, Provider.FACEBOOK );
				_aAuth.uploadImageAsync( "#HarrisCam", HarrisConfig.PATH_FILE + "fx2.jpg", HarrisConfig.BMP_HARRIS, 100,
						new UploadImageListener() );
			} catch ( Exception e ) {
				Kimdata.jlog( e );
			}

			break;
		case R.id.ibGooglePlus:

			break;
		case R.id.ibDone:
			finish();

			break;
		}
	}

	public class ResponseListener implements DialogListener {

		@Override
		public void onComplete( Bundle values ) {
			if ( progDialog != null ) {
				progDialog.dismiss();
			}

			Kimdata.jlog( "Authentication successful." );
		}

		@Override
		public void onError( SocialAuthError e ) {
			Kimdata.jlog( e );
			Kimdata.toast( ShareActivity.this, "SNS 인증에 실패했습니다. 다시 시도해주세요." );
			finish();
		}

		@Override
		public void onCancel() {
			Kimdata.jlog( "Authentication cancelled." );
			Kimdata.toast( ShareActivity.this, "SNS 인증을 취소했습니다." );
			finish();
		}

		@Override
		public void onBack() {
			Kimdata.jlog( "Dialog Closed." );
			Kimdata.toast( ShareActivity.this, "SNS 인증을 취소했습니다." );
			finish();
		}
	}

	public class MessageListener implements SocialAuthListener< Integer > {

		@Override
		public void onExecute( String provider, Integer t ) {
			Integer status = t;
			if ( status.intValue() == 200 || status.intValue() == 201 || status.intValue() == 204 ) {
				Kimdata.toast( ShareActivity.this, "사진 등록에 성공했습니다!" );
			} else {
				Kimdata.toast( ShareActivity.this, "사진 등록에 실패했습니다..." );
			}

			if ( progDialog != null ) {
				progDialog.dismiss();
			}
		}

		@Override
		public void onError( SocialAuthError e ) {
			Kimdata.jlog( e );
			Kimdata.toast( ShareActivity.this, "사진 등록에 실패했습니다..." );
		}
	}

	private final class UploadImageListener implements SocialAuthListener< Integer > {

		@Override
		public void onExecute( String provider, Integer t ) {
			Integer status = t;
			if ( status.intValue() == 200 || status.intValue() == 201 || status.intValue() == 204 ) {
				Kimdata.toast( ShareActivity.this, "사진 등록에 성공했습니다!" );
			} else {
				Kimdata.toast( ShareActivity.this, "사진 등록에 실패했습니다..." );
			}

			if ( progDialog != null ) {
				progDialog.dismiss();
			}

			ShareActivity.this.finish();
		}

		@Override
		public void onError( SocialAuthError e ) {
			if ( progDialog != null ) {
				progDialog.dismiss();
			}

			Kimdata.jlog( e );
			Kimdata.toast( ShareActivity.this, "사진 등록에 실패했습니다..." );
		}
	}

}
