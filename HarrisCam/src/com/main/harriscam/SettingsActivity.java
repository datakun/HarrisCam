package com.main.harriscam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class SettingsActivity extends Activity {
	LinearLayout llTitleSettings, llCameraMode, llAutoSave, llResolution, llAbout, llVersion, llAutoUpdate;
	ImageButton ibSettingsClose, ibCameraMode, ibAutoSave, ibResolution, ibAutoUpdate;
	TextView tvVersion;
	ImageView ivTitleCamera, ivTitleHarris;

	int displayWidth; // Device's display size information.
	int displayHeight;

	private static final int DIALOG_VERSION = 0;

	Intent iAboutActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		displayWidth = MainActivity.displayWidth;
		displayHeight = MainActivity.displayHeight;

		llTitleSettings = (LinearLayout) findViewById(R.id.llTitleSettings);
		// llCameraMode = (LinearLayout) findViewById(R.id.llCameraMode);
		llAutoSave = (LinearLayout) findViewById(R.id.llAutoSave);
		llResolution = (LinearLayout) findViewById(R.id.llResolution);
		llAbout = (LinearLayout) findViewById(R.id.llAbout);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llAutoUpdate = (LinearLayout) findViewById(R.id.llAutoUpdate);

		ibSettingsClose = (ImageButton) findViewById(R.id.ibSettingsClose);
		// ibCameraMode = (ImageButton) findViewById(R.id.ibCameraMode);
		ibAutoSave = (ImageButton) findViewById(R.id.ibAutoSave);
		ibResolution = (ImageButton) findViewById(R.id.ibResolution);
		ibAutoUpdate = (ImageButton) findViewById(R.id.ibAutoUpdate);

		ivTitleCamera = (ImageView) findViewById(R.id.ivTitleCamera);
		ivTitleHarris = (ImageView) findViewById(R.id.ivTitleHarrisCam);

		tvVersion = (TextView) findViewById(R.id.tvVersion);

		llTitleSettings.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		// llCameraMode.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		llAutoSave.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		llResolution.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		llAbout.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		llVersion.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		llAutoUpdate.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));

		ibSettingsClose.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));
		// ibCameraMode.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));
		ibAutoSave.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));
		ibResolution.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));
		ibAutoUpdate.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));

		tvVersion.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));

		ivTitleCamera.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
		ivTitleHarris.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));

		llAbout.setClickable(true);
		llVersion.setClickable(true);
		/*
		 * if (MainActivity.bScaledSquare == true) { ibCameraMode.setBackgroundResource(R.drawable.scale_square); } else
		 * { ibCameraMode.setBackgroundResource(R.drawable.scale_rectangle); }
		 */
		if (MainActivity.bOriginalAutoSave == true) {
			ibAutoSave.setBackgroundResource(R.drawable.btn_on);
		} else {
			ibAutoSave.setBackgroundResource(R.drawable.btn_off);
		}

		iAboutActivity = new Intent(this, AboutActivity.class);

		switch (MainActivity.nSaveResolution) {
			case MainActivity.SAVE_LOW:
				ibResolution.setBackgroundResource(R.drawable.res_low);

				break;
			case MainActivity.SAVE_MIDDLE:
				ibResolution.setBackgroundResource(R.drawable.res_mid);

				break;
			case MainActivity.SAVE_HIGH:
				ibResolution.setBackgroundResource(R.drawable.res_high);

				break;
		}

		if (MainActivity.bAutoUpdate == true) {
			ibAutoUpdate.setBackgroundResource(R.drawable.btn_on);
		} else {
			ibAutoUpdate.setBackgroundResource(R.drawable.btn_off);
		}

		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ibSettingsClose.getLayoutParams();
		lp.leftMargin = displayWidth - displayWidth / 5;
		ibSettingsClose.setLayoutParams(lp);
		/*
		 * lp = (LinearLayout.LayoutParams) ibCameraMode.getLayoutParams(); lp.leftMargin = displayWidth - displayWidth
		 * / 5; ibCameraMode.setLayoutParams(lp);
		 */
		lp = (LinearLayout.LayoutParams) ibAutoSave.getLayoutParams();
		lp.leftMargin = displayWidth - displayWidth / 5;
		ibAutoSave.setLayoutParams(lp);
		lp = (LinearLayout.LayoutParams) ibResolution.getLayoutParams();
		lp.leftMargin = displayWidth - displayWidth / 5;
		ibResolution.setLayoutParams(lp);
		lp = (LinearLayout.LayoutParams) ibAutoUpdate.getLayoutParams();
		lp.leftMargin = displayWidth - displayWidth / 5;
		ibAutoUpdate.setLayoutParams(lp);
		lp = (LinearLayout.LayoutParams) tvVersion.getLayoutParams();
		lp.leftMargin = displayWidth - displayWidth / 5;
		tvVersion.setLayoutParams(lp);
		/*
		 * ibCameraMode.setOnClickListener(new View.OnClickListener() {
		 * 
		 * public void onClick(View v) { if (MainActivity.bScaledSquare == true) { MainActivity.bScaledSquare = false;
		 * v.setBackgroundResource(R.drawable.scale_rectangle); } else { MainActivity.bScaledSquare = true;
		 * v.setBackgroundResource(R.drawable.scale_square); }
		 * 
		 * } });
		 */

		ibSettingsClose.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.btn_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.close);
						setResult(RESULT_OK);
						finish();

						break;
				}

				return false;
			}
		});

		ibAutoSave.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.btn_push);

						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundResource(R.drawable.btn_on);
						if (MainActivity.bOriginalAutoSave == true) {
							MainActivity.bOriginalAutoSave = false;
							v.setBackgroundResource(R.drawable.btn_off);
						} else {
							MainActivity.bOriginalAutoSave = true;
							v.setBackgroundResource(R.drawable.btn_on);
						}

						break;

				}

				return false;
			}
		});

		ibResolution.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.btn_push);

						break;
					case MotionEvent.ACTION_UP:
						switch (MainActivity.nSaveResolution) {
							case MainActivity.SAVE_LOW:
								MainActivity.nSaveResolution = MainActivity.SAVE_MIDDLE;
								v.setBackgroundResource(R.drawable.res_mid);

								break;
							case MainActivity.SAVE_MIDDLE:
								MainActivity.nSaveResolution = MainActivity.SAVE_HIGH;
								v.setBackgroundResource(R.drawable.res_high);

								break;
							case MainActivity.SAVE_HIGH:
								MainActivity.nSaveResolution = MainActivity.SAVE_LOW;
								v.setBackgroundResource(R.drawable.res_low);

								break;
						}

						break;

				}

				return false;
			}
		});

		ibAutoUpdate.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundResource(R.drawable.btn_push);

						break;
					case MotionEvent.ACTION_UP:
						if (MainActivity.bAutoUpdate == true) {
							MainActivity.bAutoUpdate = false;
							v.setBackgroundResource(R.drawable.btn_off);
						} else {
							MainActivity.bAutoUpdate = true;
							v.setBackgroundResource(R.drawable.btn_on);
						}

						break;

				}

				return false;
			}
		});

		llAbout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				openAboutActivity();

			}
		});

		llVersion.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (MainActivity.bLatestVersion == true) {
					showToast(MainActivity.lsSTRINGs.sLVersion);
				} else {
					updateVersion();
				}

			}
		});
	}

	private void openAboutActivity() {
		startActivity(iAboutActivity);
	}

	private void updateVersion() {
		float fMyVersion = Float.parseFloat(MainActivity.strAppVersion);
		float fServerVersion = Float.parseFloat(MainActivity.strLatestVersion);

		if (fServerVersion > fMyVersion) {
			showDialog(DIALOG_VERSION);
		}
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
			setResult(RESULT_OK);
			finish();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_VERSION:
				return new AlertDialog.Builder(this).setTitle(MainActivity.lsSTRINGs.mUpdating)
						.setMessage(MainActivity.lsSTRINGs.sUpdateAsking).setCancelable(true)
						.setPositiveButton(MainActivity.lsSTRINGs.aYes, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.main.harriscam"));

								startActivity(intent);
								MainActivity.bStarted = false;

								dialog.dismiss();
							}

						}).setNegativeButton(MainActivity.lsSTRINGs.aNo, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {

								dialog.cancel();
							}

						}).create();
		}

		return super.onCreateDialog(id);
	}
}
