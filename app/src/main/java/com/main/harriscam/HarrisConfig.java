package com.main.harriscam;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class HarrisConfig {
    public static int FLAG_MODE = 0; // 0 : Camera, 1 : Gallery, 2 : Settings

	public static int INTERVAL = 500; // ms
	public static int OFFSET_INTERVAL = 500; // ms
	public static int PICTURE_COUNT = 3;

    public static int FLAG_FLASHLIGHT;

	public static boolean IsEND = false;

	public static final class HandlerAskQuit extends Handler {
		public void handleMessage( Message msg ) {
			if ( msg.what == 0 ) {
				IsEND = false;
			}
		}
	}

	public static String PATH_SAVE = "";
	public static String PATH_FILE = "";

	public static boolean IsEFFECTIVE = false;

	public static boolean IsSaveOriginalImage = true;

	public static Bitmap BMP_HARRIS = null;
}
