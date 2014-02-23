package com.main.harriscam;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kimdata.kimdatautil.Kimdata;

public class ShareActivity extends Activity implements View.OnClickListener {
	ImageView ivPicture;
	Button ibTwitter, ibFacebook, ibTumblr, ibDone;

	boolean _isInited = false;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_share );

		ivPicture = (ImageView) findViewById( R.id.ivPicture );
		ibTwitter = (Button) findViewById( R.id.ibTwitter );
		ibFacebook = (Button) findViewById( R.id.ibFacebook );
		ibTumblr = (Button) findViewById( R.id.ibTumblr );
		ibDone = (Button) findViewById( R.id.ibDone );

		ibTwitter.setOnClickListener( this );
		ibFacebook.setOnClickListener( this );
		ibTumblr.setOnClickListener( this );
		ibDone.setOnClickListener( this );
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
		switch ( v.getId() ) {
		case R.id.ibTwitter:

			break;
		case R.id.ibFacebook:

			break;
		case R.id.ibTumblr:

			break;
		case R.id.ibDone:
			finish();

			break;
		}
	}

}
