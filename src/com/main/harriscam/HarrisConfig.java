package com.main.harriscam;

import android.os.Handler;
import android.os.Message;

public class HarrisConfig {
	public static boolean IsINITED = false;

	public static int DEVICE_W = 0;
	public static int DEVICE_H = 0;

	public static int LAYOUT_W = 0;
	public static int LAYOUT_H = 0;

	public static int PREVIEW_W = 0;
	public static int PREVIEW_H = 0;

	public static int PICTURE_W = 0;
	public static int PICTURE_H = 0;

	public static int OFFSET_PICTURE = 0;

	public static int INTERVAL = 500; // ms
	public static int OFFSET_INTERVAL = 500; // ms
	public static int PICTURE_COUNT = 3;

	public static boolean IsEND = false;

	public static final class HandlerEnd extends Handler {
		public void handleMessage( Message msg ) {
			if ( msg.what == 0 ) {
				IsEND = false;
			}
		}
	}

	public static String PATH_SAVE;
	public static String PATH_FILE;

	public static boolean IsSAVED = false;

	public static int RESOLUTION = 0; // 0: high, 1: mid, 2: low
	public static int RES_HIGH = 0;
	public static int RES_MID = 0;
	public static int RES_LOW = 480;
}
