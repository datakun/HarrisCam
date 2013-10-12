package com.main.harriscam;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

public class CropActivity extends Activity {
	LinearLayout llCropView, llCropButtons;
	ImageButton ibCancel, ibRotate, ibShare, ibApply;
	ImageView ivCropView;

	Bitmap bitResultImage;
	public static String strCropImage = null; // Result cropped image filename
												// is here.
	public static String strShareImage = null; // Result share image filename is
												// here.
	float fHeightRate = 1; // Image scale rate.
	float fWidthRate = 1;
	int nImageWidth = 0; // Image size
	int nImageHeight = 0;

	int displayWidth; // Device's display size information.
	int displayHeight;

	boolean bVertical = true;

	private static final int DIALOG_APPLY = 0;
	private static final int DIALOG_CANCEL = 1;
	private static final int INTENT_SHARE = 0;

	private static final int RESULT_FAILED = 999;

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

		adjustViewSize(llCropView, displayWidth, displayHeight - displayWidth / 5);
		adjustViewSize(llCropButtons, displayWidth, displayWidth / 5);

		adjustViewSize(ibCancel, displayWidth / 6, displayWidth / 6);
		adjustViewSize(ibRotate, displayWidth / 6, displayWidth / 6);
		adjustViewSize(ibShare, displayWidth / 6, displayWidth / 6);
		adjustViewSize(ibApply, displayWidth / 6, displayWidth / 6);

		if (MainActivity.bScaledSquare == true) {
			adjustViewSize(ivCropView, displayWidth, displayWidth);
		} else {
			adjustViewSize(ivCropView, displayWidth, displayWidth * 4 / 3);
		}

		nImageHeight = MainActivity.bitOpened[0].getHeight();
		nImageWidth = MainActivity.bitOpened[0].getWidth();

		try {
			bitResultImage = Bitmap.createBitmap(MainActivity.bitResultImage);
		} catch (OutOfMemoryError e) {
			showToast("Memory space is full... Try again.");

			MainActivity.deleteAllBitmap();

			setResult(RESULT_FAILED);
			finish();
		}

		showToast(MainActivity.lsSTRINGs.cApplySuccess);

		ivCropView.setImageBitmap(bitResultImage);

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

					rotateImage();

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

	private void rotateImage() {
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		matrix.postScale(1, 1);

		try {
			Bitmap bitTemp = Bitmap.createBitmap(bitResultImage);

			if (bitResultImage != null) {
				bitResultImage.recycle();
				bitResultImage = null;
			}
			System.gc();

			bitResultImage = Bitmap.createBitmap(bitTemp, 0, 0, nImageWidth, nImageHeight, matrix, true);
			ivCropView.setImageBitmap(bitResultImage);

			if (bitTemp != null) {
				bitTemp.recycle();
				bitTemp = null;
			}
			System.gc();
		} catch (OutOfMemoryError e) {
			showToast("Memory space is full... Try again.");

			MainActivity.deleteAllBitmap();

			setResult(RESULT_FAILED);
			finish();
		}

		nImageWidth = bitResultImage.getWidth();
		nImageHeight = bitResultImage.getHeight();

		if (MainActivity.bScaledSquare == false) {
			if (bVertical == true) {
				bVertical = false;
				adjustViewSize(ivCropView, displayWidth, displayWidth * 3 / 4);
			} else {
				bVertical = true;
				adjustViewSize(ivCropView, displayWidth, displayWidth * 4 / 3);
			}
		}
	}

	private void shareImage() {
		if (saveTemporaryImage() == true) {
			try {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				File temp = new File(strShareImage);
				Uri uri = Uri.fromFile(temp);
				sharingIntent.setType("image/jpg");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivityForResult(Intent.createChooser(sharingIntent, MainActivity.lsSTRINGs.cShareMessage),
						INTENT_SHARE);
			} catch (Exception e) {
				showToast(e.toString(), Toast.LENGTH_LONG);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case INTENT_SHARE:
			if (strShareImage != null) {
				File file = new File(strShareImage);
				file.delete();
				strShareImage = null;
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
			builder.setMessage(MainActivity.lsSTRINGs.cImageSave);
			builder.setCancelable(false);
			builder.setNegativeButton(MainActivity.lsSTRINGs.aNo, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setPositiveButton(MainActivity.lsSTRINGs.aYes, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					if (saveApplyImage() == true) {
						Intent i = getIntent();
						i.putExtra("image", strCropImage);
						setResult(RESULT_OK);
						finish();
					}

					dialog.dismiss();
				}
			});
			dialog = builder.create();

			break;
		case DIALOG_CANCEL:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(MainActivity.lsSTRINGs.cImageCancel);
			builder.setCancelable(false);
			builder.setNegativeButton(MainActivity.lsSTRINGs.aNo, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setPositiveButton(MainActivity.lsSTRINGs.aYes, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					if (strCropImage != null) {
						File file = new File(strCropImage);
						file.delete();
						strCropImage = null;
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

	private boolean saveTemporaryImage() {
		boolean bResult = false;

		strShareImage = MainActivity.strFilePath + "tempshare.jpg";

		try {
			FileOutputStream fio = new FileOutputStream(strShareImage);

			bitResultImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);

			fio.close();
			fio = null;

			bResult = true;
		} catch (Exception e) {
			showToast(e.toString());
		}

		return bResult;
	}

	private boolean saveApplyImage() {
		boolean bResult = false;

		strCropImage = MainActivity.strFilePath + "Shutter_" + System.currentTimeMillis() + ".jpg";

		try {
			FileOutputStream fio = null;

			// Original file save.
			if (MainActivity.bOriginalAutoSave == true) {
				fio = new FileOutputStream(MainActivity.strOriFilename);

				MainActivity.bitOpened[0].compress(Bitmap.CompressFormat.JPEG, 100, fio);

				if (fio != null) {
					fio.close();
					fio = null;
				}

				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
						+ MainActivity.strOriFilename)));

				showToast(MainActivity.lsSTRINGs.mSaveSuccess);
			}

			// Result file save.
			fio = new FileOutputStream(strCropImage);

			bitResultImage.compress(Bitmap.CompressFormat.JPEG, 100, fio);

			fio.close();
			fio = null;

			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + CropActivity.strCropImage)));

			showToast(MainActivity.lsSTRINGs.mSaveSuccess);

			deleteCropBitmap();

			bResult = true;
		} catch (Exception e) {
			showToast(e.toString());

			deleteCropBitmap();
		}

		return bResult;
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

	@Override
	protected void onDestroy() {
		deleteCropBitmap();

		System.gc();

		super.onDestroy();
	}

	private void deleteCropBitmap() {
		if (bitResultImage != null) {
			bitResultImage.recycle();
			bitResultImage = null;
		}
	}
}
