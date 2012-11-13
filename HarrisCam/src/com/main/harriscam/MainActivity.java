package com.main.harriscam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.hardware.*;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Camera;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.RadioGroup.LayoutParams;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	boolean bEnd = false;
	Handler mHandler;

	private Preview mPreview;
	Camera mCamera;
	int nTimes = 0;
	public static final int IMAGE_WIDTH = 768;
	public static final int IMAGE_HEIGHT = 1024;
	public static final int ID_AUTOSHOT = 502;
	public static final int ID_FLASH = 702;
	private static final int FLASH_OFF = 0;
	private static final int FLASH_ON = 1;
	private static final int FLASH_AUTO = 2;
	private static final int ACTIVITY_CROP = 0;

	Camera.PictureCallback mPictureCallbackJpeg;
	Camera.ShutterCallback mShutterCallback;
	Camera.PictureCallback mPictureCallbackRaw;
	Camera.AutoFocusCallback mAutoFocusCallback;
	byte byOriImage[][];
	public static String strTempFilename[];
	public static Bitmap bitOriImage[];
	Bitmap bitResImage;
	long LastTime;
	boolean bAutoCapture = true;
	int nAutoInterval = 500;

	private ImageView mCropImageView;

	LinearLayout llButtons, llUpperButtons;
	FrameLayout flPreview;
	public static ImageButton ibGallery, ibShutter, ibTurn, ibAutoshot, ibFlash;
	SeekBar sbTimeInterval;

	public static int displayWidth;
	public static int displayHeight;

	public static boolean bFrontCam = false;
	int bFlash = FLASH_OFF;

	private SensorManager sm;
	private Sensor oriSensor;
	private SensorEventListener oriL;
	private long nLastTime;
	private float lastOX, lastOY, lastOZ;

	Intent iCropActivity;
	Intent iGalleryActivity;

	public static boolean bGalleryMaked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		displayWidth = display.getWidth();
		displayHeight = display.getHeight();

		iCropActivity = new Intent(this, CropActivity.class);
		iGalleryActivity = new Intent(this, GalleryActivity.class);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		sm = (SensorManager) getSystemService(SENSOR_SERVICE); // SensorManager
		oriSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		nLastTime = System.currentTimeMillis();
		oriL = new oriListener(); // Accelerometer Listener
		sm.registerListener(oriL, oriSensor, SensorManager.SENSOR_DELAY_NORMAL); // Register Orientation Listener
		lastOX = lastOY = lastOZ = 0;

		// For finish this application.
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0) {
					bEnd = false;
				}
			}
		};

		mPreview = new Preview(this);

		byOriImage = new byte[3][];
		strTempFilename = new String[3];
		bitOriImage = new Bitmap[3];

		mCropImageView = (ImageView) findViewById(R.id.ivCropView);

		mPictureCallbackJpeg = new Camera.PictureCallback() {

			public void onPictureTaken(byte[] data, Camera camera) {
				LastTime = System.currentTimeMillis();

				byOriImage[nTimes] = data;

				if (saveImage() != 0) {
					showToast("Save failed");

					return;
				}

				if (camera != null) {
					camera.lock();
					try {
						camera.startPreview();
					} catch (Exception e) {
					}
				}

				if (bAutoCapture == true) {
					while (System.currentTimeMillis() - LastTime <= nAutoInterval) {
					}

					LastTime = System.currentTimeMillis();

					if (nTimes < 2) {
						nTimes++;
						camera.takePicture(mShutterCallback, mPictureCallbackRaw, mPictureCallbackJpeg);
					} else {
						showToast("Apply the harris shutter.");
						nTimes = 0;

						sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strTempFilename[0])));

						openCropActivity();
						// bitResImage = applyHarrisShutter();
					}
				} else {
					if (nTimes < 2) {
						nTimes++;

						if (nTimes == 1) {
							ibShutter.setBackgroundResource(R.drawable.shutter_2);
						} else if (nTimes == 2) {
							ibShutter.setBackgroundResource(R.drawable.shutter_1);
						}
					} else {
						showToast("Apply the harris shutter.");
						nTimes = 0;

						sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strTempFilename[0])));

						openCropActivity();
						// bitResImage = applyHarrisShutter();

						try {
							String strFilename = Environment.getExternalStorageDirectory().getAbsolutePath()
									+ "/Pictures/HarrisCam/Harris_" + System.currentTimeMillis() + ".jpg";
							FileOutputStream fio = new FileOutputStream(strFilename);

							bitResImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);

							fio.close();
							System.gc();

							showToast("Apply successful.");

							File file = new File(strTempFilename[1]);
							file.delete();
							file = new File(strTempFilename[2]);
							file.delete();
							System.gc();

							// 1 File scanning
							sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strTempFilename[0])));
							sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strFilename)));
						} catch (Exception e) {
							showToast(e.toString());
						}
					}
				}
			}
		};

		mShutterCallback = new Camera.ShutterCallback() {

			public void onShutter() {

			}
		};

		mPictureCallbackRaw = new Camera.PictureCallback() {

			public void onPictureTaken(byte[] data, Camera c) {

			}
		};

		mAutoFocusCallback = new Camera.AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {

			}
		};

		ibGallery = (ImageButton) findViewById(R.id.ibGallery);
		ibShutter = (ImageButton) findViewById(R.id.ibShutter);
		ibTurn = (ImageButton) findViewById(R.id.ibTurn);

		flPreview = (FrameLayout) findViewById(R.id.flPreview);
		llButtons = (LinearLayout) findViewById(R.id.llButtons);

		adjustViewSize(ibGallery, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibShutter, displayWidth / 4, displayWidth / 4);
		adjustViewSize(ibTurn, displayWidth / 5, displayWidth / 5);

		adjustViewSize(flPreview, displayWidth, displayHeight - displayWidth / 4);
		adjustViewSize(llButtons, displayWidth, displayWidth / 4);

		ibGallery.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.gallery_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.gallery);

						openGallery(); // Use intent

						break;
				}

				return false;
			}
		});

		ibShutter.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.shutter_push);

						break;
					case MotionEvent.ACTION_UP:
						if (bAutoCapture == true) {
							v.setBackgroundResource(R.drawable.shutter_auto);
						} else if (nTimes == 2) {
							v.setBackgroundResource(R.drawable.shutter_3);
						}

						if (mCamera != null) {
							takePictureNow();
						}

						break;
				}

				return false;
			}
		});

		ibTurn.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.turnover1_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.turnover1);

						if (bFrontCam == false) {
							openFrontCamera(true);
						} else {
							openFrontCamera(false);
						}

						break;
				}

				return false;
			}
		});

		flPreview.addView(mPreview, new LinearLayout.LayoutParams(displayWidth, displayHeight));

		llUpperButtons = new LinearLayout(this);
		llUpperButtons.setBackgroundResource(R.drawable.background);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.width = displayWidth;
		params.height = displayWidth / 6;
		llUpperButtons.setLayoutParams(params);
		flPreview.addView(llUpperButtons);

		ibAutoshot = new ImageButton(this);
		ibAutoshot.setClickable(true);
		ibAutoshot.setId(ID_AUTOSHOT);
		ibAutoshot.setBackgroundResource(R.drawable.auto05);
		FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params2.gravity = Gravity.LEFT;
		params2.setMargins(displayWidth / 3, 0, 0, 0);
		params2.width = displayWidth / 3;
		params2.height = displayWidth / 6;
		ibAutoshot.setLayoutParams(params2);
		flPreview.addView(ibAutoshot);

		ibFlash = new ImageButton(this);
		ibFlash.setClickable(true);
		ibFlash.setId(ID_FLASH);
		ibFlash.setBackgroundResource(R.drawable.flash_off);
		params2 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params2.gravity = Gravity.LEFT;
		params2.setMargins(0, 0, 0, 0);
		params2.width = displayWidth / 3;
		params2.height = displayWidth / 7;
		ibFlash.setLayoutParams(params2);
		flPreview.addView(ibFlash);

		mPreview.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				try {
					mCamera.autoFocus(null);
				} catch (Exception e) {

				}

				return false;
			}
		});

		ibAutoshot.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.shot_push);

						break;
					case MotionEvent.ACTION_UP:
						if (bAutoCapture == true && nAutoInterval == 2000) {
							bAutoCapture = false;
							v.setBackgroundResource(R.drawable.manual);
							ibShutter.setBackgroundResource(R.drawable.shutter_3);
							nAutoInterval = 0;
						} else {
							bAutoCapture = true;
							deleteTempFiles();
							if (nAutoInterval == 0) {
								nAutoInterval = 500;
								v.setBackgroundResource(R.drawable.auto05);
								ibShutter.setBackgroundResource(R.drawable.shutter_auto);
							} else if (nAutoInterval == 500) {
								nAutoInterval = 1000;
								v.setBackgroundResource(R.drawable.auto10);
							} else if (nAutoInterval == 1000) {
								nAutoInterval = 1500;
								v.setBackgroundResource(R.drawable.auto15);
							} else if (nAutoInterval == 1500) {
								nAutoInterval = 2000;
								v.setBackgroundResource(R.drawable.auto20);
							}
						}

						break;
				}

				return false;
			}
		});

		ibFlash.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						break;
					case MotionEvent.ACTION_UP:
						Parameters p = mCamera.getParameters();

						if (bFlash == FLASH_OFF) {
							bFlash = FLASH_AUTO;
							v.setBackgroundResource(R.drawable.flash_auto);
							p.setFlashMode(Parameters.FLASH_MODE_AUTO);
						} else if (bFlash == FLASH_AUTO) {
							bFlash = FLASH_ON;
							v.setBackgroundResource(R.drawable.flash_on);
							p.setFlashMode(Parameters.FLASH_MODE_ON);
						} else {
							bFlash = FLASH_OFF;
							v.setBackgroundResource(R.drawable.flash_off);
							p.setFlashMode(Parameters.FLASH_MODE_OFF);
						}

						mCamera.setParameters(p);

						break;
				}

				return false;
			}
		});
	}

	private void deleteTempFiles() {
		for (int i = 0; i < nTimes; i++) {
			File temp = new File(strTempFilename[i]);
			temp.delete();
		}

		nTimes = 0;
	}

	private void openGallery() {
		startActivity(iGalleryActivity);
	}

	private void openCropActivity() {
		startActivityForResult(iCropActivity, ACTIVITY_CROP);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
	}

	private int saveImage() {
		int nResult = 0;

		try {
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Pictures/HarrisCam");
			dir.mkdirs();

			String strFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HarrisCam/Harris_"
					+ System.currentTimeMillis() + ".jpg";
			FileOutputStream fio = new FileOutputStream(strFilename);

			fio.write(byOriImage[nTimes]);
			fio.close();
			fio = null;

			bitOriImage[nTimes] = BitmapFactory.decodeFile(strFilename);

			int nWidth = bitOriImage[nTimes].getWidth();
			int nHeight = bitOriImage[nTimes].getHeight();

			Matrix matrix = new Matrix();
			matrix.postScale(1, 1);
			if (bFrontCam == false) {
				matrix.postRotate(90);
			} else {
				matrix.postRotate(270);
			}

			File temp = new File(strFilename);
			temp.delete();

			fio = new FileOutputStream(strFilename);

			bitResImage = Bitmap.createBitmap(bitOriImage[nTimes], 0, 0, nWidth, nHeight, matrix, true);
			bitResImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);
			strTempFilename[nTimes] = strFilename;

			fio.close();
			System.gc();
		} catch (Exception e) {
			showToast(e.toString(), Toast.LENGTH_LONG);
			nResult = -1;
		}

		return nResult;
	}

	private void takePictureNow() {
		mCamera.takePicture(null, null, mPictureCallbackJpeg);
	}

	private class oriListener implements SensorEventListener {
		public void onSensorChanged(SensorEvent event) {
			float fOX, fOY, fOZ;
			long currentTime = System.currentTimeMillis();
			long gabOfTime = (currentTime - nLastTime);

			fOX = event.values[0];
			fOY = event.values[1];
			fOZ = event.values[2];

			if (Math.abs(lastOX + lastOY + lastOZ - (fOX + fOY + fOZ)) > 8) {
				if (gabOfTime > 1000) {

					nLastTime = currentTime;

					try {
						mCamera.autoFocus(null);
					} catch (Throwable t) {

					}

					lastOX = fOX;
					lastOY = fOY;
					lastOZ = fOZ;
				}
			}

		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	}

	public void openFrontCamera(boolean bFront) {
		int cameraCount = 0;
		int camNum = 0;
		Camera.CameraInfo ciCamera = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();

		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, ciCamera);

			if (bFront == true) {
				if (ciCamera.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camNum = i;
				}

				bFrontCam = true;
			} else {
				if (ciCamera.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camNum = i;
				}

				bFrontCam = false;
			}
		}

		try {
			mCamera.release();
			mCamera = null;

			mCamera = Camera.open(camNum);
			mCamera.setDisplayOrientation(90);
			try {
				mCamera.setPreviewDisplay(mPreview.mHolder);
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
			}

			mCamera.startPreview();
		} catch (RuntimeException e) {
			showToast(e.toString());
		}
	}

	public void adjustViewSize(View view, int nWidth, int nHeight) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
		lp.width = nWidth;
		lp.height = nHeight;
		view.setLayoutParams(lp);
	}

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		SurfaceHolder mHolder;

		Preview(Context context) {
			super(context);

			// SurfaceHolder.Callback. For surface create, destroy.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// Surface has been created, set a position to drawing preview.
			mCamera = Camera.open();
			mCamera.setDisplayOrientation(90);
			try {
				mCamera.setPreviewDisplay(holder);

				Camera.Parameters parameters = mCamera.getParameters();

				List<Size> sizes = parameters.getSupportedPictureSizes();
				Size optimalSize;
				optimalSize = getOptimalPreviewSize(sizes, IMAGE_WIDTH, IMAGE_HEIGHT);
				parameters.setPictureSize(optimalSize.width, optimalSize.height);
				mCamera.setParameters(parameters);
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// If surface has been destroyed, release a camera resource.
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// I know preview size!!! So I'll change size.
			Camera.Parameters parameters = mCamera.getParameters();

			List<Size> sizes = parameters.getSupportedPreviewSizes();
			Size optimalSize = getOptimalPreviewSize(sizes, w, h);
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);

			// parameters.setPreviewSize(w, h);
			mCamera.setParameters(parameters);
			mCamera.startPreview();
		}

	}

	// Find a optimal preview size of device
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;

			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;

			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}

	public void showToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	public void showToast(String s, int LENGTH) {
		Toast.makeText(this, s, LENGTH).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (bEnd == false) {
				showToast("Do you want to close this app? One more 'Back'");

				bEnd = true;
				mHandler.sendEmptyMessageDelayed(0, 2000);

				return false;
			} else {
				finish();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		android.os.Process.killProcess(android.os.Process.myPid());

		super.finish();
	}

}
