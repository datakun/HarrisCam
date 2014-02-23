package com.main.harriscam;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kimdata.camera.CameraPreview;
import com.kimdata.kimdatautil.Kimdata;

public class CameraActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
	FrameLayout flMain;
	Button ibFlash, ibTimer, ibResolution, ibGuideline;
	Button ibShutter, ibGallery, ibSwitch;
	CameraPreview cpPreview;
	LinearLayout llPanelTop, llPanelBottom;
	ImageView ivStatFlash, ivStatTimer;

	ThreadShutter _thread;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_camera );
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

		flMain = (FrameLayout) findViewById( R.id.flMain );

		ibFlash = (Button) findViewById( R.id.ibFlash );
		ibTimer = (Button) findViewById( R.id.ibTimer );
		ibResolution = (Button) findViewById( R.id.ibResolution );
		ibGuideline = (Button) findViewById( R.id.ibGuideline );

		ibShutter = (Button) findViewById( R.id.ibShutter );
		ibGallery = (Button) findViewById( R.id.ibGallery );
		ibSwitch = (Button) findViewById( R.id.ibSwitch );

		cpPreview = (CameraPreview) findViewById( R.id.cpPreview );

		llPanelTop = (LinearLayout) findViewById( R.id.llPanelTop );
		llPanelBottom = (LinearLayout) findViewById( R.id.llPanelBottom );

		ivStatFlash = (ImageView) findViewById( R.id.ivStatFlash );
		ivStatTimer = (ImageView) findViewById( R.id.ivStatTimer );

		cpPreview.setOnTouchListener( this );

		ibFlash.setOnClickListener( this );
		ibTimer.setOnClickListener( this );
		ibResolution.setOnClickListener( this );
		ibGuideline.setOnClickListener( this );

		ibShutter.setOnClickListener( this );
		ibGallery.setOnClickListener( this );
		ibSwitch.setOnClickListener( this );
	}

	@Override
	public void onWindowFocusChanged( boolean hasFocus ) {
		if ( !HarrisConfig.IsINITED ) {
			HarrisConfig.DEVICE_W = Kimdata.getScreenRes( this )[0];
			HarrisConfig.DEVICE_H = Kimdata.getScreenRes( this )[1];

			HarrisConfig.LAYOUT_W = flMain.getWidth();
			HarrisConfig.LAYOUT_H = flMain.getHeight();

			HarrisConfig.PREVIEW_W = HarrisConfig.DEVICE_W;
			HarrisConfig.PREVIEW_H = HarrisConfig.DEVICE_W;

			HarrisConfig.OFFSET = ( HarrisConfig.DEVICE_H - HarrisConfig.PREVIEW_W ) / 2;

			Kimdata.resizeViewInFrame( llPanelTop, HarrisConfig.DEVICE_W, HarrisConfig.OFFSET );
			Kimdata.resizeViewInFrame( llPanelBottom, HarrisConfig.DEVICE_W, HarrisConfig.OFFSET );

			Kimdata.positionViewInFrame( llPanelBottom, 0, HarrisConfig.DEVICE_H - HarrisConfig.OFFSET );

			Kimdata.positionViewInFrame( ivStatFlash, Kimdata.dp2px( 5, getResources() ),
					HarrisConfig.OFFSET + Kimdata.dp2px( 5, getResources() ) );
			Kimdata.positionViewInFrame( ivStatTimer, ivStatFlash.getRight() + Kimdata.dp2px( 5, getResources() ), HarrisConfig.OFFSET
					+ Kimdata.dp2px( 5, getResources() ) );

			// TODO 폴더를 찾아주는 알고리즘으로 경로 설정해야 함 (2014-02-23)
			HarrisConfig.PATH_SAVE = Kimdata.makeDir( "/DCIM/harriscam" );

			cpPreview.initSound( R.raw.shutter );

			HarrisConfig.IsINITED = true;
		}

		super.onWindowFocusChanged( hasFocus );
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event ) {
		if ( keyCode == KeyEvent.KEYCODE_BACK ) {
			if ( HarrisConfig.IsEND == false ) {
				Kimdata.toast( this, "'이전' 버튼을 한 번 더 누르면 앱이 종료됩니다." );

				HarrisConfig.IsEND = true;

				Handler handler = new HarrisConfig.HandlerEnd();
				handler.sendEmptyMessageDelayed( 0, 2000 );

				return false;
			} else {
				finish();
			}
		} else if ( keyCode == KeyEvent.KEYCODE_HOME ) {
			finish();
		}

		return super.onKeyDown( keyCode, event );
	}

	@Override
	public void finish() {
		android.os.Process.killProcess( android.os.Process.myPid() );

		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.camera, menu );
		return true;
	}

	@Override
	public void onClick( View v ) {
		switch ( v.getId() ) {
		case R.id.ibFlash:
			switch ( cpPreview.switchFlash() ) {
			case 0:

				break;
			case 1:

				break;
			case 2:

				break;
			default:
				Kimdata.toast( this, "플래시를 사용할 수 없습니다." );

				break;
			}

			break;
		case R.id.ibTimer:

			break;
		case R.id.ibResolution:

			break;
		case R.id.ibGuideline:

			break;
		case R.id.ibGallery:
			startActivity( new Intent( this, GalleryActivity.class ) );

			break;
		case R.id.ibShutter:
			cpPreview.captureSurfaceView( HarrisConfig.INTERVAL );

			if ( _thread != null ) {
				_thread.interrupt();
				_thread = null;
			}
			_thread = new ThreadShutter();
			_thread.start();

			break;
		case R.id.ibSwitch:
			cpPreview.switchCamera();

			break;
		}
	}

	public class ThreadShutter extends Thread implements Runnable {

		@Override
		public void run() {
			while ( !Thread.currentThread().isInterrupted() ) {
				if ( HarrisConfig.IsSAVED ) {
					HarrisConfig.IsSAVED = false;
					startActivity( new Intent( CameraActivity.this, EditorActivity.class ) );
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Override
	public boolean onTouch( View v, MotionEvent event ) {
		switch ( v.getId() ) {
		case R.id.cpPreview:
			switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN:
				cpPreview._Camera.autoFocus( new AutoFocusCallback() {

					@Override
					public void onAutoFocus( boolean success, Camera cam ) {
						// TODO Auto-generated method stub

					}
				} );

				break;
			}

			break;
		}

		return false;
	}
}
