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

	public static int OFFSET = 0;

	public static float INTERVAL = 0.5f;

	public static boolean IsEND = false;

	public static final class HandlerEnd extends Handler {
		public void handleMessage( Message msg ) {
			if ( msg.what == 0 ) {
				IsEND = false;
			}
		}
	}

	public static String PATH_SAVE;

	public static boolean IsSAVED = false;
}
