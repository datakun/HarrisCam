package com.main.harriscam.util;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class HarrisConfig {
    public enum VIEW_MODE {
        CAMERA, GALLERY, SETTINGS
    }
    public static VIEW_MODE FLAG_MODE = VIEW_MODE.CAMERA;

    // Varable for pictures.
    public static int INTERVAL = 500; // ms
    public static int OFFSET_INTERVAL = 500; // ms
    public static int PICTURE_COUNT = 3; // Count of pictures.
    public static Bitmap BMP_HARRIS = null;

    // Varable for option flag
    public static int FLAG_FLASHLIGHT;

    public static boolean IS_FINISHING = false;

    public static String SAVE_PATH = ""; // path/to/save
    public static String FILE_PATH = ""; // absolute path/to/file
    public static boolean IS_SAVE_ORIGINAL_IMAGE = true;

    public static final class HandlerAskQuit extends Handler {
        public void handleMessage( Message msg ) {
            if ( msg.what == 0 ) {
                IS_FINISHING = false;
            }
        }
    }
}