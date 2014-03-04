package com.kimdata.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kimdata.kimdatautil.Kimdata;
import com.main.harriscam.HarrisConfig;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	static {
		System.loadLibrary( "HarrisCam" );
	}

	public static native void naApplyHarris( Bitmap bitG, Bitmap bitR, Bitmap bitB );

	public static native void naApplyScreen( Bitmap bitResult, Bitmap bitOrigin, Bitmap bitTemp );

	Context _Context;
	SurfaceHolder _Holder;

	public Camera _Camera;
	Camera.Parameters _Params;
	List< Camera.Size > _listPreviewSize;
	List< Camera.Size > _listPictureSize;
	Camera.Size _sizePreview;

	public boolean _isCapture = false;

	int _totalCam;
	boolean _isFront = false;

	int _flagFlash; // 0: Off, 1: Auto, 2: Torch

	public byte[][] _byRawData; // 0: first, 1: second, 2: third Raw data
	int _idxData; // Index of data

	long _timeLast;
	long _timeInterval;

	public String _filepath;

	ProgressDialog _progDlg;

	SoundPool sound_pool;
	int sound_beep;

	public Bitmap[] bmpImage;

	public CameraPreview( Context context, AttributeSet attrs ) {
		super( context, attrs );

		_Context = context;

		_Holder = getHolder();
		_Holder.addCallback( this );
		_Holder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );

		_byRawData = new byte[3][];

		_progDlg = new ProgressDialog( _Context );
		_progDlg.setTitle( "Harris Cam" );
		_progDlg.setMessage( "효과를 적용 중입니다..." );
		_progDlg.setIndeterminate( true );
		_progDlg.setCancelable( false );
	}

	@Override
	public void surfaceCreated( SurfaceHolder holder ) {
		Kimdata.jlog( "created" );
		openCam( CameraInfo.CAMERA_FACING_BACK );

		_totalCam = Camera.getNumberOfCameras();
		_isFront = false;
		_flagFlash = 0;
		_idxData = 0;
	}

	private int rotatePreview( int rotate ) {
		int rotation = ( (Activity) _Context ).getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;

		switch ( rotation ) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;

		case Surface.ROTATION_90:
			degrees = 90;
			break;

		case Surface.ROTATION_180:
			degrees = 180;
			break;

		case Surface.ROTATION_270:
			degrees = 270;
			break;

		}

		int result = ( rotate - degrees + 360 ) % 360;
		return result;
	}

	@Override
	public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
		Kimdata.jlog( "changed" );
		startCam();
	}

	private void startCam() {
		if ( _Params != null && _Camera != null ) {
			_sizePreview = _listPreviewSize.get( 0 );
			_Params.setPreviewSize( _sizePreview.width, _sizePreview.height );
			_Camera.setParameters( _Params );
			_Camera.startPreview();

			Kimdata.jlog( _listPreviewSize.get( 0 ).width + ", " + _listPreviewSize.get( 0 ).height );
		}
	}

	@Override
	public void surfaceDestroyed( SurfaceHolder holder ) {
		Kimdata.jlog( "destroyed" );
		releaseCam();
	}

	public void initSound( int resId ) {
		sound_pool = new SoundPool( 5, AudioManager.STREAM_SYSTEM, 0 );
		sound_beep = sound_pool.load( getContext(), resId, 1 );
	}

	public void playSound() {
		sound_pool.play( sound_beep, 1f, 1f, 0, 0, 1f );
	}

	private void openCam( int camNum ) {
		try {
			_Camera = Camera.open( camNum );
			_Camera.setPreviewCallback( _callbackPreview );

			_Camera.setDisplayOrientation( rotatePreview( 90 ) );
			_Camera.setPreviewDisplay( _Holder );
		} catch ( IOException e ) {
			Kimdata.jlog( e );
		}

		_Params = _Camera.getParameters();
		_listPreviewSize = _Params.getSupportedPreviewSizes();
		_listPictureSize = _Params.getSupportedPictureSizes();

		Kimdata.sortCameraSize( _listPreviewSize, true );
		Kimdata.sortCameraSize( _listPictureSize, true );

		initResolution();
	}

	private void releaseCam() {
		if ( _Camera != null ) {
			_Camera.setPreviewCallback( null );
			_Camera.stopPreview();
			_Camera.release();
			_Camera = null;
		}
	}

	PreviewCallback _callbackPreview = new PreviewCallback() {

		@Override
		public void onPreviewFrame( byte[] data, Camera camera ) {
			if ( _isCapture ) {
				if ( _idxData <= 0 ) {
					_byRawData[_idxData++] = data;
					playSound();
				} else if ( _timeInterval <= System.currentTimeMillis() - _timeLast ) {
					_byRawData[_idxData++] = data;
					playSound();
					Kimdata.jlog( System.currentTimeMillis() - _timeLast + "ms" );
					_timeLast = System.currentTimeMillis();
				}

				if ( _idxData > 2 ) {
					_idxData = 0;
					_isCapture = false;

					if ( _progDlg != null ) {
						_progDlg.show();
					}

					bmpImage = new Bitmap[3];

					// TODO Bitmap으로 저장하는 부분; Preview data는 YUV 포맷이기 때문에 다음과 같은 방법으로 Bitmap을 만들어한다.
					// 이미지를 파일로 저장하는 부분을 스레드로 추출하자
					int i = 1;
					for ( byte[] byImage : _byRawData ) {
						int w = _Params.getPreviewSize().width;
						int h = _Params.getPreviewSize().height;
						int format = _Params.getPreviewFormat();
						YuvImage image = new YuvImage( byImage, format, w, h, null );

						ByteArrayOutputStream out = new ByteArrayOutputStream();
						Rect area = new Rect( 0, 0, w, h );
						image.compressToJpeg( area, 100, out );
						bmpImage[i - 1] = BitmapFactory.decodeByteArray( out.toByteArray(), 0, out.size() );

						String filename = _filepath + ( i ) + ".jpg";
						Bitmap bitmap = rotateBitmap( bmpImage[i - 1], 90 );
						bmpImage[i - 1] = cropBitmap( bitmap, HarrisConfig.OFFSET_PICTURE, HarrisConfig.DEVICE_W );
						Kimdata.SaveBitmapToFileCache( bmpImage[i - 1], filename );
						i++;
					}

					synchronized ( bmpImage[0] ) {
						naApplyHarris( bmpImage[0], bmpImage[1], bmpImage[2] );
					}

					Kimdata.SaveBitmapToFileCache( bmpImage[0], _filepath + "fx.jpg" );

					HarrisConfig.PATH_FILE = _filepath;

					if ( _progDlg != null ) {
						_progDlg.dismiss();
						HarrisConfig.IsSAVED = true;
					}
				}
			}
		}
	};

	public void captureImage() {
		_isCapture = true;
		_timeInterval = HarrisConfig.INTERVAL;
		_timeLast = System.currentTimeMillis();

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
		_filepath = HarrisConfig.PATH_SAVE + "/harriscam_" + format.format( now ) + "_";
	}

	public void switchCamera() {
		if ( _totalCam < 2 ) {
			Kimdata.toast( _Context, "카메라 개수가 적습니다." );

			return;
		}

		releaseCam();

		if ( !_isFront ) {
			openCam( Camera.CameraInfo.CAMERA_FACING_FRONT );
			_isFront = true;
		} else {
			openCam( Camera.CameraInfo.CAMERA_FACING_BACK );
			_isFront = false;
		}

		startCam();
	}

	public int switchFlash() {
		if ( _Context.getApplicationContext().getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA_FLASH ) ) {
			if ( _flagFlash == 0 ) {
				_flagFlash = 1;
				_Params.setFlashMode( Parameters.FLASH_MODE_AUTO );
			} else if ( _flagFlash == 1 ) {
				_flagFlash = 2;
				_Params.setFlashMode( Parameters.FLASH_MODE_TORCH );
			} else {
				_flagFlash = 0;
				_Params.setFlashMode( Parameters.FLASH_MODE_OFF );
			}

			try {
				_Camera.setParameters( _Params );
			} catch ( Exception e ) {
				Kimdata.jlog( e );
			}

			return _flagFlash;
		} else {
			return -1;
		}
	}

	// TODO 해야 함
	// 최고 해상도, 중간 해상도, 낮은 해상도를 설정
	private void initResolution() {
		int length = _listPreviewSize.size();

		Kimdata.jlog( _listPreviewSize.get( 0 ).width + " width1, " + _listPreviewSize.get( 0 ).height + " height1" );
		Kimdata.jlog( _listPreviewSize.get( length / 2 ).width + " width2, " + _listPreviewSize.get( length / 2 ).height + " height2" );
		Kimdata.jlog( _listPreviewSize.get( length - 1 ).width + " width3, " + _listPreviewSize.get( length - 1 ).height + " height3" );
	}

	private Size getOptimalResolution( List< Size > sizes, int w, int h ) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if ( sizes == null )
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;

		for ( Size size : sizes ) {
			double ratio = (double) size.width / size.height;

			if ( Math.abs( ratio - targetRatio ) > ASPECT_TOLERANCE )
				continue;
			if ( Math.abs( size.height - targetHeight ) < minDiff ) {
				optimalSize = size;
				minDiff = Math.abs( size.height - targetHeight );
			}
		}

		if ( optimalSize == null ) {
			minDiff = Double.MAX_VALUE;

			for ( Size size : sizes ) {
				if ( Math.abs( size.height - targetHeight ) < minDiff ) {
					optimalSize = size;
					minDiff = Math.abs( size.height - targetHeight );
				}
			}
		}

		return optimalSize;
	}

	public void switchResolution() {

	}

	private Bitmap rotateBitmap( Bitmap bitmap, int angle ) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.postRotate( angle );

		Bitmap resizedBitmap = Bitmap.createBitmap( bitmap, 0, 0, width, height, matrix, true );
		bitmap.recycle();

		return resizedBitmap;
	}

	private Bitmap cropBitmap( Bitmap bitmap, int offset, int width ) {
		Bitmap croppedBitmap = Bitmap.createBitmap( bitmap, 0, offset, width, width );
		bitmap.recycle();

		return croppedBitmap;
	}
}
