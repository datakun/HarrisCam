package com.main.harriscam;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;

public class GalleryActivity extends Activity {
	GridView gridGallery;
	ImageAdapter iaPicture;
	
	int displayWidth;
	int displayHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
		displayWidth = MainActivity.displayWidth;
		displayHeight = MainActivity.displayHeight;

		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setColumnWidth(displayWidth / 4);
		if (MainActivity.bGalleryMaked == false)
		{
			iaPicture = new ImageAdapter(this);
			MainActivity.bGalleryMaked = true;
		}
		gridGallery.setAdapter(iaPicture);
		gridGallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int position, long id) {
				showToast(position + "번째 그림 선택");
			}
		});
	}

	class ImageAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<String> thumbsDataList;
		private ArrayList<String> thumbsIDList;

		int[] nPictureID;

		public ImageAdapter(Context c) {
			mContext = c;
			thumbsDataList = new ArrayList<String>();
			thumbsIDList = new ArrayList<String>();
			getThumbsInfo(thumbsIDList, thumbsDataList);
		}

		private void getThumbsInfo(ArrayList<String> thumbsIDList, ArrayList<String> thumbsDataList) {
			String[] strDBProjection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.SIZE };

			Cursor cursorImage = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, strDBProjection, null, null, null);

			if (cursorImage != null && cursorImage.moveToFirst()) {
				if (cursorImage.getCount() > 0) {
					String strTitle;
					String strThumbsID;
					String strThumbsImgID;
					String strThumbsData;
					String strData;
					String strImgSize;

					int nThumbsIDCol = cursorImage.getColumnIndex(strDBProjection[0]);
					int nThumbsDataCol = cursorImage.getColumnIndex(strDBProjection[1]);
					int nThumbsImgIDCol = cursorImage.getColumnIndex(strDBProjection[2]);
					int nThumbsSizeCol = cursorImage.getColumnIndex(strDBProjection[3]);

					int nNum = 0;

					while (cursorImage != null) {
						strThumbsID = cursorImage.getString(nThumbsIDCol);
						strThumbsData = cursorImage.getString(nThumbsDataCol);
						strThumbsImgID = cursorImage.getString(nThumbsImgIDCol);
						strImgSize = cursorImage.getString(nThumbsSizeCol);

						if (strThumbsID != null) {
							thumbsIDList.add(strThumbsID);
							thumbsDataList.add(strThumbsData);
						}

						if (!cursorImage.moveToNext()) {
							break;
						}
					}
				} // if (cursorImage.getCount() > 0)
			} // if (cursorImage != null && cursorImage.moveToFirst())

			cursorImage.close();
		}

		private String getImageinfo(String ImageData, String Location, String thumbsID) {
			String strImgDataPath = null;

			String[] strDBProjection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.SIZE };

			Cursor cursorImage = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, strDBProjection, "_ID='" + thumbsID + "'",
					null, null);

			if (cursorImage != null && cursorImage.moveToFirst()) {
				if (cursorImage.getCount() > 0) {
					int nImgData = cursorImage.getColumnIndex(strDBProjection[1]);
					strImgDataPath = cursorImage.getString(nImgData);
				}
			}

			cursorImage.close();

			return strImgDataPath;
		}

		public int getCount() {
			return 100;
		}

		public Object getItem(int position) {
			return nPictureID[position % 3];
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView ivImage;

			if (convertView == null) {
				ivImage = new ImageView(mContext);
				ivImage.setLayoutParams(new GridView.LayoutParams(displayWidth / 3 - 2, displayWidth / 3 - 2));
				ivImage.setAdjustViewBounds(false);
				ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				ivImage.setPadding(2, 2, 2, 2);
			} else {
				ivImage = (ImageView) convertView;
			}
			BitmapFactory.Options bfOption = new BitmapFactory.Options();
			bfOption.inSampleSize = 8;
			Bitmap bitBMP = BitmapFactory.decodeFile(thumbsDataList.get(position), bfOption);
			Bitmap bitResized = Bitmap.createScaledBitmap(bitBMP, displayWidth / 3 - 2, displayWidth / 3 - 2,
					true);
			ivImage.setImageBitmap(bitResized);

			return ivImage;
		}

		public long getItemId(int position) {
			return position;
		}
	}

	public void showToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	public void showToast(String s, int LENGTH) {
		Toast.makeText(this, s, LENGTH).show();
	}
}