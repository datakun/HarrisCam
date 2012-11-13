package com.main.harriscam;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

public class CropActivity extends Activity {
	LinearLayout llCropView, llCropButtons, llEmpty;
	ImageButton ibCancel, ibRotate, ibScale, ibApply;
	ImageView ivCropView;

	Bitmap bitCropImage;
	String strCropImage;
	int nOffsetX = 0;
	int nOffsetY = 0;
	float fHeightRate = 1;
	float fWidthRate = 1;
	int nImageWidth = 0;
	int nImageHeight = 0;

	int displayWidth;
	int displayHeight;

	boolean bScaleModeSquare = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);

		displayWidth = MainActivity.displayWidth;
		displayHeight = MainActivity.displayHeight;

		llCropView = (LinearLayout) findViewById(R.id.llCrop);
		llCropButtons = (LinearLayout) findViewById(R.id.llCropButtons);
		llEmpty = (LinearLayout) findViewById(R.id.llEmpty);

		ibCancel = (ImageButton) findViewById(R.id.ibCancel);
		ibRotate = (ImageButton) findViewById(R.id.ibRotate);
		ibScale = (ImageButton) findViewById(R.id.ibScale);
		ibApply = (ImageButton) findViewById(R.id.ibApply);

		ivCropView = (ImageView) findViewById(R.id.ivCropView);

		adjustViewSize(llCropView, displayWidth, displayWidth);
		adjustViewSize(llEmpty, displayWidth, displayHeight - displayWidth - displayWidth / 4);
		adjustViewSize(llCropButtons, displayWidth, displayWidth / 4);

		adjustViewSize(ibCancel, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibRotate, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibScale, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibApply, displayWidth / 5, displayWidth / 5);

		adjustViewSize(ivCropView, displayWidth, displayWidth);

		initCropImage();

		bitCropImage = applyHarrisShutter();
		bitCropImage = Bitmap.createBitmap(bitCropImage, 0, 0, nImageWidth, nImageHeight);
		ivCropView.setImageBitmap(bitCropImage);

		ibCancel.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.cancel_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.cancel);

						for (int i = 0; i < 3; i++) {
							File file = new File(MainActivity.strTempFilename[i]);
							file.delete();
							System.gc();
						}

						showToast("Apply canceled.");

						setResult(RESULT_CANCELED);
						finish();

						break;
				}

				return false;
			}
		});

		ibRotate.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.rotate_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.rotate);

						try {
							Matrix matrix = new Matrix();
							matrix.postRotate(90);
							matrix.postScale(1, 1);
							
							int nWidth = bitCropImage.getWidth();
							int nHeight = bitCropImage.getHeight();

							bitCropImage = Bitmap.createBitmap(bitCropImage, 0, 0, nWidth, nHeight, matrix, true);
							ivCropView.setImageBitmap(bitCropImage);

							/*
							int temp = nImageHeight;
							nImageHeight = nImageWidth;
							nImageWidth = temp;
							*/
						} catch (Exception e) {
							showToast(e.toString());
						}

						break;
				}

				return false;
			}
		});

		ibScale.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (bScaleModeSquare) {
							v.setBackgroundResource(R.drawable.scale_square_push);
						} else {
							v.setBackgroundResource(R.drawable.scale_rect_push);
						}

						break;
					case MotionEvent.ACTION_UP:
						if (bScaleModeSquare) {
							bScaleModeSquare = false;
							v.setBackgroundResource(R.drawable.scale_rect);

							adjustViewSize(llCropView, displayWidth, displayHeight - displayWidth / 4);
							adjustViewSize(llEmpty, displayWidth, 0);

							adjustViewSize(ivCropView, displayWidth, displayWidth);

							showToast("Apply as square");
						} else {
							bScaleModeSquare = true;
							v.setBackgroundResource(R.drawable.scale_square);

							adjustViewSize(llCropView, displayWidth, displayWidth);
							adjustViewSize(llEmpty, displayWidth, displayHeight - displayWidth - displayWidth / 4);

							adjustViewSize(ivCropView, displayWidth, displayWidth);

							showToast("Apply as rectangle");
						}

						break;
				}

				return false;
			}
		});

		ibApply.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.apply_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.apply);

						try {
							strCropImage = Environment.getExternalStorageDirectory().getAbsolutePath()
									+ "/Pictures/HarrisCam/Harris_croped_" + System.currentTimeMillis() + ".jpg";

							if (bScaleModeSquare == true) {
								bitCropImage = Bitmap.createBitmap(bitCropImage, 0, 0, nImageWidth, nImageWidth);
							}

							FileOutputStream fio = new FileOutputStream(strCropImage);

							bitCropImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);

							fio.close();

							showToast("Apply successful.");

							for (int i = 0; i < 3; i++) {
								File file = new File(MainActivity.strTempFilename[i]);
								file.delete();
								System.gc();
							}
							System.gc();

							sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + strCropImage)));
						} catch (Exception e) {
							showToast(e.toString());
						}

						Intent i = getIntent();
						i.putExtra("image", strCropImage);
						setResult(RESULT_OK);
						finish();

						break;
				}

				return false;
			}
		});

	}

	public void initCropImage() {
		try {
			for (int i = 0; i < 3; i++) {
				String strFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HarrisCam/Harris_croped_"
						+ System.currentTimeMillis() + ".jpg";

				int nWidth = MainActivity.bitOriImage[i].getWidth();
				int nHeight = MainActivity.bitOriImage[i].getHeight();

				Matrix matrix = new Matrix();
				float fScaledRate = (float) 512 / nHeight;
				if (MainActivity.bFrontCam == false) {
					matrix.postRotate(90);
				} else {
					matrix.postRotate(270);
				}
				matrix.postScale(fScaledRate, fScaledRate);

				if (i > 0) {
					File file = new File(MainActivity.strTempFilename[i]);
					file.delete();
					System.gc();
				}

				FileOutputStream fio = new FileOutputStream(strFilename);

				Bitmap bitResImage = Bitmap.createBitmap(MainActivity.bitOriImage[i], 0, 0, nWidth, nHeight, matrix, true);
				bitResImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);
				MainActivity.strTempFilename[i] = strFilename;

				fio.close();
				System.gc();
			}
			Bitmap bitBMP = BitmapFactory.decodeFile(MainActivity.strTempFilename[0]);
			nImageHeight = bitBMP.getHeight();
			nImageWidth = bitBMP.getWidth();
			fHeightRate = MainActivity.displayHeight / nImageHeight;
			fWidthRate = MainActivity.displayWidth / nImageWidth;
		} catch (Exception e) {
			showToast(e.toString());
		}
	}

	private Bitmap applyHarrisShutter() {

		Bitmap bitResult = BitmapFactory.decodeFile(MainActivity.strTempFilename[0]).copy(Config.ARGB_8888, true);

		try {
			Bitmap bitRed = BitmapFactory.decodeFile(MainActivity.strTempFilename[1]);
			Bitmap bitGreen = BitmapFactory.decodeFile(MainActivity.strTempFilename[0]);
			Bitmap bitBlue = BitmapFactory.decodeFile(MainActivity.strTempFilename[2]);

			for (int i = 0; i < bitResult.getHeight(); i++) {
				for (int j = 0; j < bitResult.getWidth(); j++) {
					int nRed = Color.red(bitRed.getPixel(j, i));
					int nGreen = Color.green(bitGreen.getPixel(j, i));
					int nBlue = Color.blue(bitBlue.getPixel(j, i));

					bitResult.setPixel(j, i, Color.argb(255, nRed, nGreen, nBlue));
				}
			}
		} catch (Exception e) {
			showToast(e.toString());
		}

		return bitResult;
	}

	public void adjustViewSize(View view, int nWidth, int nHeight) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
		lp.width = nWidth;
		lp.height = nHeight;
		view.setLayoutParams(lp);
	}

	public void showToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	public void showToast(String s, int LENGTH) {
		Toast.makeText(this, s, LENGTH).show();
	}
}
