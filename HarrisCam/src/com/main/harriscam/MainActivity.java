package com.main.harriscam;

import java.io.*;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private Uri cropedImageUri = null;
	public static final int CAMERA = 1;
	public static final int ALBUM = 2;
	public static final int CHOOSER = 3;
	public static final int CROP = 4;
	public static final int DIALOG = 502;

	static final String[] strOptions = { "Take a photo", "Pick up in a gallery", "Pick up in app" };

	Button btnShutter;
	
	Dialog dlgPhoto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dlgPhoto = new AlertDialog.Builder(MainActivity.this).setTitle("Picture import")
				.setItems(strOptions, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								startActivityForResult(cameraIntent, CAMERA);
								break;
							case 1:
								Intent albumIntent = new Intent(Intent.ACTION_PICK);
								albumIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
								startActivityForResult(albumIntent, ALBUM);
								break;
							case 2:
								Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
								contentIntent.setType("image/*");
								startActivityForResult(Intent.createChooser(contentIntent, "사진 첨부하기"), CHOOSER);
								break;
						}

					}
				}).create();
		
		dlgPhoto.show();

		btnShutter = (Button) findViewById(R.id.shutter);
		btnShutter.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				dlgPhoto.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(MainActivity.this).setTitle("Picture import")
				.setItems(strOptions, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								startActivityForResult(cameraIntent, CAMERA);
								break;
							case 1:
								Intent albumIntent = new Intent(Intent.ACTION_PICK);
								albumIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
								startActivityForResult(albumIntent, ALBUM);
								break;
							case 2:
								Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
								contentIntent.setType("image/*");
								startActivityForResult(Intent.createChooser(contentIntent, "사진 첨부하기"), CHOOSER);
								break;
						}

					}
				}).create();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent;

		File toFile = null; // Copy for temporary file path
		File fromFile = null; // Save to open file path

		FileInputStream inputStream = null; // Read a original file
		FileOutputStream outputStream = null; // Write to temporary file

		switch (requestCode) {
			case CAMERA:
				// If didn't shot or cancel, will skip
				if (resultCode == RESULT_CANCELED)
					return;
				// For delete a temporary file
				if (cropedImageUri != null) {
					File f = new File(cropedImageUri.getPath());
					if (f.exists())
						f.delete();
				}

				// DB table
				Uri uriImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				int id = -1; // ID in DB for shot image

				// In DB table, we will find conditioned record
				String[] IMAGE_PROJECTION = { MediaStore.Images.ImageColumns.DATA, // Absolute path for data
						MediaStore.Images.ImageColumns._ID, // ID for data
						MediaStore.Images.ImageColumns.DATE_TAKEN // date
				};

				try {
					Cursor cursorImages = getContentResolver().query(uriImages, IMAGE_PROJECTION, null, null, null);

					if (cursorImages != null && cursorImages.moveToLast()) {
						fromFile = new File(cursorImages.getString(0));
						id = cursorImages.getInt(1); // ID!
						cursorImages.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Temporary file path
				new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HarrisCam").mkdir();
				toFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HarrisCam/" + System.currentTimeMillis()
						+ ".jpg");

				if (toFile.exists()) {
					toFile.delete();
				}

				try {
					inputStream = new FileInputStream(fromFile);
					outputStream = new FileOutputStream(toFile);
					FileChannel fcin = inputStream.getChannel();
					FileChannel fcout = outputStream.getChannel();

					long size = fcin.size();

					fcin.transferTo(0, size, fcout);

					fcout.close();
					fcin.close();
					outputStream.close();
					inputStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Delete a original
				fromFile.delete();
				// Delete a original record in DB(For looks good)
				getBaseContext().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						MediaStore.Images.ImageColumns._ID + "=" + id, null);

				cropedImageUri = Uri.fromFile(toFile);

				intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(cropedImageUri, "image/*");
				// intent.putExtra("outputX", GlobalData.dip(this, 512));
				// intent.putExtra("outputY", GlobalData.dip(this, 512));
				// aspectX, aspectY is ratio for transform a region
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("scale", true);
				intent.putExtra("output", cropedImageUri);

				startActivityForResult(intent, CROP);

				break;
			case ALBUM: // Pick up in a gallery or app
			case CHOOSER:
				if (resultCode == RESULT_CANCELED)
					return;

				if (cropedImageUri != null) {
					File f = new File(cropedImageUri.getPath());
					if (f.exists()) {
						f.delete();
					}
				}

				cropedImageUri = data.getData();
				Cursor c = getContentResolver().query(cropedImageUri, null, null, null, null);
				c.moveToNext();
				String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
				fromFile = new File(path);
				c.close();

				new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HarrisCam").mkdirs();
				toFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HarrisCam/" + System.currentTimeMillis()
						+ ".jpg");

				if (toFile.exists()) {
					toFile.delete();
				}

				try {
					inputStream = new FileInputStream(fromFile);
					outputStream = new FileOutputStream(toFile);
					FileChannel fcin = inputStream.getChannel();
					FileChannel fcout = outputStream.getChannel();

					long size = fcin.size();

					fcin.transferTo(0, size, fcout);

					fcout.close();
					fcin.close();
					outputStream.close();
					inputStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				cropedImageUri = Uri.fromFile(toFile);
				intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(cropedImageUri, "image/*");
				// intent.putExtra("outputX", GlobalData.dip(this, 512));
				// intent.putExtra("outputY", GlobalData.dip(this, 512));
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("scale", true);
				intent.putExtra("output", cropedImageUri);
				startActivityForResult(intent, CROP);

				break;
			case CROP:
				if (resultCode == RESULT_OK) {
					
				} else {
					if (cropedImageUri != null) {
						File f = new File(cropedImageUri.getPath());
						if (f.exists()) {
							f.delete();
						}
					}
				}

				break;
		}
	}
}
