package com.main.harriscam;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

public class CropActivity extends Activity {
	LinearLayout llCropView, llCropButtons;
	ImageButton ibCancel, ibRotate, ibShare, ibApply;
	ImageView ivCropView;

	Bitmap bitCropImage; // Result cropped Bitmap is here!
	public static String strCropImage = null; // Result cropped image filename is here.
	String strTempFilename[];
	float fHeightRate = 1; // Image scale rate.
	float fWidthRate = 1;
	int nImageWidth = 0; // Image size
	int nImageHeight = 0;

	int displayWidth; // Device's display size information.
	int displayHeight;

	private static final int DIALOG_APPLY = 0;
	private static final int DIALOG_CANCEL = 1;
	private static final int INTENT_SHARE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);

		displayWidth = MainActivity.displayWidth;
		displayHeight = MainActivity.displayHeight;

		llCropView = (LinearLayout) findViewById(R.id.llCrop);
		llCropButtons = (LinearLayout) findViewById(R.id.llCropButtons);

		ibCancel = (ImageButton) findViewById(R.id.ibCancel);
		ibRotate = (ImageButton) findViewById(R.id.ibRotate);
		ibShare = (ImageButton) findViewById(R.id.ibShare);
		ibApply = (ImageButton) findViewById(R.id.ibApply);

		ivCropView = (ImageView) findViewById(R.id.ivCropView);

		adjustViewSize(llCropView, displayWidth, displayHeight - displayWidth / 4);
		adjustViewSize(llCropButtons, displayWidth, displayWidth / 4);

		adjustViewSize(ibCancel, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibRotate, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibShare, displayWidth / 5, displayWidth / 5);
		adjustViewSize(ibApply, displayWidth / 5, displayWidth / 5);

		adjustViewSize(ivCropView, displayWidth, displayWidth);

		strTempFilename = MainActivity.strTempFilename;

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

						showDialog(DIALOG_CANCEL);

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

						} catch (Exception e) {
							showToast(e.toString());
						}

						break;
				}

				return false;
			}
		});

		ibShare.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.share_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.share);

						shareImage();

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

						showDialog(DIALOG_APPLY);

						break;
				}

				return false;
			}
		});

	}

	private void shareImage() {
		if (strCropImage != null) {
			File file = new File(strCropImage);
			file.delete();
		}

		saveApplyImage();

		try {
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			File temp = new File(strCropImage);
			Uri uri = Uri.fromFile(temp);
			sharingIntent.setType("image/jpg");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivityForResult(Intent.createChooser(sharingIntent, "Select an App to send"), INTENT_SHARE);
		} catch (Exception e) {
			showToast(e.toString(), Toast.LENGTH_LONG);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case INTENT_SHARE:

				switch (resultCode) {
					case RESULT_CANCELED:

						if (strCropImage != null) {
							File file = new File(strCropImage);
							file.delete();
						}
						showToast("Share canceled");

						break;
				}

				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		AlertDialog.Builder builder;

		switch (id) {
			case DIALOG_APPLY:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you want to apply and save?");
				builder.setCancelable(false);
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (strCropImage != null) {
							File file = new File(strCropImage);
							file.delete();
						}

						saveApplyImage();

						Intent i = getIntent();
						i.putExtra("image", strCropImage);
						setResult(RESULT_OK);
						dialog.dismiss();
						finish();
					}
				});
				dialog = builder.create();

				break;
			case DIALOG_CANCEL:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you want to cancel?");
				builder.setCancelable(false);
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (strCropImage != null) {
							File file = new File(strCropImage);
							file.delete();
						}

						setResult(RESULT_CANCELED);
						dialog.dismiss();
						finish();
					}
				});
				dialog = builder.create();

				break;
		}

		return dialog;
	}

	private void saveApplyImage() {
		try {
			strCropImage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HarrisCam/Harris_croped_"
					+ System.currentTimeMillis() + ".jpg";

			bitCropImage = Bitmap.createBitmap(bitCropImage, 0, 0, nImageWidth, nImageHeight);

			FileOutputStream fio = new FileOutputStream(strCropImage);

			bitCropImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);

			fio.close();
			System.gc();
		} catch (Exception e) {
			showToast(e.toString());
		}
	}

	private void initCropImage() {
		try {
			for (int i = 0; i < 3; i++) {
				String strFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HarrisCam/Harris_croped_"
						+ System.currentTimeMillis() + ".jpg";

				int nWidth = MainActivity.bitOriImage[i].getWidth();
				int nHeight = MainActivity.bitOriImage[i].getHeight();

				Matrix matrix = new Matrix();
				float fScaledRate = (float) MainActivity.nSaveResolution / nHeight;
				if (MainActivity.bFrontCam == false) {
					matrix.postRotate(90);
				} else {
					matrix.postRotate(270);
				}
				matrix.postScale(fScaledRate, fScaledRate);

				if (i > 0) {
					File file = new File(strTempFilename[i]);
					file.delete();
					System.gc();
				}

				FileOutputStream fio = new FileOutputStream(strFilename);

				Bitmap bitResImage = Bitmap.createBitmap(MainActivity.bitOriImage[i], 0, 0, nWidth, nHeight, matrix, true);
				bitResImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);
				strTempFilename[i] = strFilename;

				fio.close();
				System.gc();
			}
			Bitmap bitBMP = BitmapFactory.decodeFile(strTempFilename[0]);
			nImageHeight = bitBMP.getHeight();
			nImageWidth = bitBMP.getWidth();
			fHeightRate = MainActivity.displayHeight / nImageHeight;
			fWidthRate = MainActivity.displayWidth / nImageWidth;
			MainActivity.strTempFilename = strTempFilename;
		} catch (Exception e) {
			showToast(e.toString());
		}
	}

	private Bitmap applyHarrisShutter() {

		Bitmap bitResult = BitmapFactory.decodeFile(strTempFilename[0]).copy(Config.ARGB_8888, true);

		try {
			Bitmap bitRed = BitmapFactory.decodeFile(strTempFilename[1]);
			Bitmap bitGreen = BitmapFactory.decodeFile(strTempFilename[0]);
			Bitmap bitBlue = BitmapFactory.decodeFile(strTempFilename[2]);

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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog(DIALOG_CANCEL);
		}

		return super.onKeyDown(keyCode, event);
	}
}
