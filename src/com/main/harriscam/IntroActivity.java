package com.main.harriscam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class IntroActivity extends Activity {
	private Handler _Handler;
	private Runnable _Runnable;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_intro );

		_Runnable = new Runnable() {
			@Override
			public void run() {
				startActivity( new Intent( IntroActivity.this, CameraActivity.class ) );
				finish();
			}
		};

		_Handler = new Handler();
		_Handler.postDelayed( _Runnable, 1000 );
	}
}
