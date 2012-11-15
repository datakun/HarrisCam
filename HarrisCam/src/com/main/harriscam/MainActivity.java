package com.main.harriscam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.hardware.*;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	boolean bEnd = false;
	Handler mHandler;

	private Preview mPreview;
	Camera mCamera;

	// Constant about state and properties
	private static final int FLASH_OFF = 0;
	private static final int FLASH_ON = 1;
	private static final int FLASH_AUTO = 2;
	private static final int ACTIVITY_CROP = 0;
	private static final int SAVE_HIGH = 600;
	private static final int SAVE_MIDDLE = 480;
	private static final int SAVE_LOW = 360;
	private static final int AUDIO_FOCUS = 0;

	// ID about view
	private static final int ID_UPPERLAYOUT = 101;
	private static final int ID_LOWERLAYOUT = 102;
	private static final int ID_AUTOSHOT = 103;
	private static final int ID_FLASH = 104;
	private static final int ID_SHUTTER = 105;
	private static final int ID_TURNOVER = 106;
	private static final int ID_UPPERBLIND = 107;
	private static final int ID_LOWERBLIND = 108;
	private static final int ID_SETTINGS = 109;

	Camera.PictureCallback mPictureCallbackJpeg;
	Camera.ShutterCallback mShutterCallback;
	Camera.PictureCallback mPictureCallbackRaw;
	Camera.AutoFocusCallback mAutoFocusCallback;
	byte byOriImage[][]; // Opened JPEG byte file is here.
	public static String strTempFilename[]; // Temporary image filename is here.
	public static Bitmap bitOriImage[]; // Opened Bitmap is here.
	public static String strOriFilename; // Opened original image filenameis here.
	Bitmap bitResImage; // Result Bitmap is here!
	long LastTime; // Difference time.
	boolean bAutoCapture = true; // Using an autoshot?
	int nAutoInterval = 500; // Autoshot time interval.
	public static int nTimes = 0; // Manual shot times.

	LinearLayout llButtons, llUpperButtons, llPreview, llUpperBlind, llLowerBlind;
	FrameLayout flPreview;
	public static ImageButton ibShutter, ibTurn, ibAutoshot, ibFlash, ibSettings;

	public static int displayWidth; // Device's display size information.
	public static int displayHeight;

	public static boolean bFrontCam = false; // Using an front camera?
	int bFlash = FLASH_OFF; // Flashlight state.

	// Sensor for auto focus.
	private SensorManager sm;
	private Sensor oriSensor;
	private SensorEventListener oriL;
	private long nLastTime;
	private float lastOX, lastOY, lastOZ;

	Intent iCropActivity;

	// I wanna play a auto focus sound!@#@!
	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;

	// Advanced Users settings
	boolean bOriginalAutoSave = true;
	public static int nSaveResolution = SAVE_MIDDLE;
	boolean bScaledSquare = true;
	int nCameraOffset = 0;
	int nImageOffset = 0;
	boolean bAutoFocusBeep = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		displayWidth = display.getWidth();
		displayHeight = display.getHeight();

		iCropActivity = new Intent(this, CropActivity.class);

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

		byOriImage = new byte[3][];
		strTempFilename = new String[3];
		bitOriImage = new Bitmap[3];

		mSoundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new HashMap<Integer, Integer>();

		mSoundPoolMap.put(AUDIO_FOCUS, mSoundPool.load(getBaseContext(), R.raw.focus, 1));

		mPictureCallbackJpeg = new Camera.PictureCallback() {

			public void onPictureTaken(byte[] data, Camera camera) {
				LastTime = System.currentTimeMillis();

				byOriImage[nTimes] = data;

				saveImage();

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

						strOriFilename = strTempFilename[0];

						openCropActivity();
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

						strOriFilename = strTempFilename[0];

						openCropActivity();
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
				if (bAutoFocusBeep == true) {
					// mSoundPool.play(mSoundPoolMap.get(AUDIO_FOCUS), 0.3f, 0.3f, 1, 0, 1f);
				}
			}
		};

		flPreview = (FrameLayout) findViewById(R.id.flPreview);
		mPreview = new Preview(this);
		// flPreview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
		// ViewGroup.LayoutParams.MATCH_PARENT));
		flPreview.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayHeight));
		flPreview.addView(mPreview, new LinearLayout.LayoutParams(displayWidth, displayHeight));

		settingBlindLayout();
		settingUpperButtons();
		settingLowerButtons();

		ibSettings.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.settings_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.settings);

						openSettingsPreference();

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
							mCamera.takePicture(null, null, mPictureCallbackJpeg);
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

		mPreview.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				try {
					mCamera.autoFocus(mAutoFocusCallback);
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
							deleteTempFiles();

							bAutoCapture = false;
							v.setBackgroundResource(R.drawable.manual);
							ibShutter.setBackgroundResource(R.drawable.shutter_3);
							nAutoInterval = 0;
						} else {
							deleteTempFiles();

							bAutoCapture = true;
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

	private void settingUpperButtons() {
		llUpperButtons = new LinearLayout(this);
		llUpperButtons.setId(ID_UPPERLAYOUT);
		llUpperButtons.setBackgroundResource(R.drawable.background);
		llUpperButtons.setOrientation(LinearLayout.HORIZONTAL);
		llUpperButtons.setGravity(Gravity.LEFT);
		llUpperButtons.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 5));

		ibFlash = new ImageButton(this);
		ibFlash.setId(ID_FLASH);
		ibFlash.setBackgroundResource(R.drawable.flash_off);
		ibFlash.setLayoutParams(new LinearLayout.LayoutParams((int) (displayWidth / 3.5), displayWidth / 7));

		ibAutoshot = new ImageButton(this);
		ibAutoshot.setId(ID_FLASH);
		ibAutoshot.setBackgroundResource(R.drawable.auto05);
		ibAutoshot.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));

		flPreview.addView(llUpperButtons);

		LinearLayout llLeft = new LinearLayout(this);
		llLeft.setOrientation(LinearLayout.HORIZONTAL);
		llLeft.setGravity(Gravity.CENTER);
		llLeft.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));
		llLeft.addView(ibFlash);

		LinearLayout llMid = new LinearLayout(this);
		llMid.setOrientation(LinearLayout.HORIZONTAL);
		llMid.setGravity(Gravity.CENTER);
		llMid.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));
		llMid.addView(ibAutoshot);

		LinearLayout llRight = new LinearLayout(this);
		llRight.setOrientation(LinearLayout.HORIZONTAL);
		llRight.setGravity(Gravity.CENTER);
		llRight.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));

		llUpperButtons.addView(llLeft);
		llUpperButtons.addView(llMid);
		llUpperButtons.addView(llRight);
	}

	private void settingLowerButtons() {
		llButtons = new LinearLayout(this);
		llButtons.setId(ID_LOWERLAYOUT);
		llButtons.setBackgroundResource(R.drawable.background);
		llButtons.setOrientation(LinearLayout.HORIZONTAL);
		llButtons.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 5));

		ibSettings = new ImageButton(this);
		ibSettings.setId(ID_SETTINGS);
		ibSettings.setBackgroundResource(R.drawable.settings);
		ibSettings.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));

		ibShutter = new ImageButton(this);
		ibShutter.setId(ID_SHUTTER);
		ibShutter.setBackgroundResource(R.drawable.shutter_auto);
		ibShutter.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 5, displayWidth / 5));

		ibTurn = new ImageButton(this);
		ibTurn.setId(ID_TURNOVER);
		ibTurn.setBackgroundResource(R.drawable.turnover1);
		ibTurn.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));

		flPreview.addView(llButtons);
		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) llButtons.getLayoutParams();
		flp.gravity = Gravity.TOP;
		flp.setMargins(0, displayHeight - displayWidth / 5, 0, 0);
		llButtons.setLayoutParams(flp);

		LinearLayout llLeft = new LinearLayout(this);
		llLeft.setOrientation(LinearLayout.HORIZONTAL);
		llLeft.setGravity(Gravity.CENTER);
		llLeft.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));
		llLeft.addView(ibSettings);

		LinearLayout llMid = new LinearLayout(this);
		llMid.setOrientation(LinearLayout.HORIZONTAL);
		llMid.setGravity(Gravity.CENTER);
		llMid.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));
		llMid.addView(ibShutter);

		LinearLayout llRight = new LinearLayout(this);
		llRight.setOrientation(LinearLayout.HORIZONTAL);
		llRight.setGravity(Gravity.CENTER);
		llRight.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 5));
		llRight.addView(ibTurn);

		llButtons.addView(llLeft);
		llButtons.addView(llMid);
		llButtons.addView(llRight);
	}

	private void settingBlindLayout() {
		int nBlindHeight = displayHeight - 2 * (displayWidth / 5);

		if (bScaledSquare == true) {
			nBlindHeight = (nBlindHeight - displayWidth) / 2;
		} else {
			nBlindHeight = (nBlindHeight - (4 * displayWidth) / 3) / 2;
		}

		nCameraOffset = displayWidth / 5 + nBlindHeight;

		llUpperBlind = new LinearLayout(this);
		llUpperBlind.setId(ID_UPPERBLIND);
		llUpperBlind.setBackgroundColor(Color.argb(255, 0, 0, 0));
		llUpperBlind.setOrientation(LinearLayout.HORIZONTAL);
		llUpperBlind.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, nCameraOffset));

		llLowerBlind = new LinearLayout(this);
		llLowerBlind.setId(ID_LOWERBLIND);
		llLowerBlind.setBackgroundColor(Color.argb(255, 0, 0, 0));
		llLowerBlind.setOrientation(LinearLayout.HORIZONTAL);
		llLowerBlind.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, nCameraOffset));

		flPreview.addView(llUpperBlind);
		flPreview.addView(llLowerBlind);
		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) llLowerBlind.getLayoutParams();
		flp.gravity = Gravity.TOP;
		flp.setMargins(0, displayHeight - nCameraOffset, 0, 0);
		llLowerBlind.setLayoutParams(flp);
	}

	public static void deleteTempFiles() {
		for (int i = 0; i < 3; i++) {
			if (strTempFilename[i] != null) {
				File temp = new File(strTempFilename[i]);
				temp.delete();
			}
		}
	}

	private void openSettingsPreference() {

	}

	private void openCropActivity() {
		startActivityForResult(iCropActivity, ACTIVITY_CROP);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			showToast("Save canceled");
		} else if (resultCode == RESULT_OK) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + CropActivity.strCropImage)));

			showToast("Save successful");
		}

		if (bOriginalAutoSave == false) {
			File temp = new File(strOriFilename);
			temp.delete();
		} else {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strOriFilename)));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		deleteTempFiles();

		if (bOriginalAutoSave == false) {
			File temp = new File(strOriFilename);
			temp.delete();
		} else {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strOriFilename)));
		}

		super.onResume();
	}

	private void saveImage() {
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

			// bitOriImage[nTimes] = Bitmap.createBitmap(bitOriImage[nTimes], nCameraOffset, 0, nHeight, nHeight);

			if (nWidth > nHeight) {
				nImageOffset = (nCameraOffset * nWidth) / displayHeight;
				bitOriImage[nTimes] = Bitmap.createBitmap(bitOriImage[nTimes], nImageOffset, 0, nHeight, nHeight);
			} else {
				nImageOffset = (nCameraOffset * nHeight) / displayWidth;
				bitOriImage[nTimes] = Bitmap.createBitmap(bitOriImage[nTimes], 0, nImageOffset, nWidth, nWidth);
			}

			nWidth = bitOriImage[nTimes].getWidth();
			nHeight = bitOriImage[nTimes].getHeight();

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
		}
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
				optimalSize = getOptimalPreviewSize(sizes, displayWidth, displayHeight);
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
