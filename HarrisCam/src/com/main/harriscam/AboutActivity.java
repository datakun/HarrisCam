package com.main.harriscam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class AboutActivity extends Activity {
	ImageView ivAbout;
	TextView tvAbout;
	LinearLayout llAboutTitle;

	int displayWidth; // Device's display size information.
	int displayHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		displayWidth = MainActivity.displayWidth;
		displayHeight = MainActivity.displayHeight;

		ivAbout = (ImageView) findViewById(R.id.ivAbout);
		ivAbout.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth));

		tvAbout = (TextView) findViewById(R.id.tvAbout);

		tvAbout.setText(MainActivity.lsSTRINGs.aAbout);

		llAboutTitle = (LinearLayout) findViewById(R.id.llAboutTitle);
		llAboutTitle.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));

		ivAbout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

		tvAbout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
	}
}
