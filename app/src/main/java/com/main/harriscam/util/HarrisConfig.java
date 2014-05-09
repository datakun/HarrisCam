package com.main.harriscam.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;

public class HarrisConfig {
    public enum VIEW_MODE {
        CAMERA, GALLERY, SETTINGS
    }
    public static VIEW_MODE FLAG_MODE = VIEW_MODE.CAMERA;

    public static final String TAG = "junu";
    public static final int PHOTO_MENU_SIZE = 128;
    public static final int MODE_MENU_SIZE = 96;
    public static final int OPTION_MENU_SIZE = 64;
    public static float SWIPE_MAX_DISTANCE;
    public static float SWIPE_MIN_DISTANCE;
    public static float SWIPE_POSSIBLE_DISTANCE;

    // Varable for pictures.
    public static int INTERVAL = 500; // ms, Capture time interval
    public static int OFFSET_INTERVAL = 500; // ms
    public static int PICTURE_COUNT = 3; // Count of pictures.
    public static Bitmap BMP_HARRIS_RESULT = null;
    public static boolean DOIN_CAPTURE = false;
    public static BitmapDrawable BD_GALLERY_BACKGROUND = null;

    // Varable for option flag
    public enum FLASH_MODE {
        OFF, ON, AUTO
    }
    public static FLASH_MODE FLAG_FLASHLIGHT = FLASH_MODE.OFF;

    public static boolean DOIN_FINISH = false;

    public static String SAVE_PATH = ""; // path/to/save
    public static String FILE_PATH = ""; // absolute path/to/file
    public static boolean IS_SAVE_ORIGINAL_IMAGE = true;

    public static final class HandlerAskQuit extends Handler {
        public void handleMessage( Message msg ) {
            if ( msg.what == 0 ) {
                DOIN_FINISH = false;
            }
        }
    }
}