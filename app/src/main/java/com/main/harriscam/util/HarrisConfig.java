package com.main.harriscam.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import com.main.harriscam.R;

import java.util.ArrayList;

public class HarrisConfig {

    public static final int CAMERA = 0;
    public static final int GALLERY = 1;
    public static int FLAG_MODE = CAMERA;

    public static final String TAG = "junu";
    public static final int PHOTO_MENU_SIZE = 128;
    public static final int MODE_MENU_SIZE = 96;
    public static final int OPTION_MENU_SIZE = 64;
    public static float SWIPE_MAX_DISTANCE;
    public static float SWIPE_MIN_DISTANCE;
    public static float SWIPE_POSSIBLE_DISTANCE;

    // For photos.
    public static int CAPTURE_INTERVAL = 500; // ms
    public static int INTERVAL_OFFSET = 500; // ms
    public static int PHOTO_COUNT = 3; // Count of pictures.
    public static Bitmap BMP_HARRIS_RESULT = null;
    public static boolean DOIN_CAPTURE = false;
    public static BitmapDrawable BD_GALLERY_BACKGROUND = null;
    public static ArrayList< Camera.Size > PHOTO_QUALITY_LIST = new ArrayList< Camera.Size >();
    public static int QUALITY_INDEX = 0;
    public static int MIN_PHOTO_WIDTH = 640;

    // For photo menu
    public static final int REQUEST_FIRST = 101;
    public static final int REQUEST_SECOND = 102;
    public static final int REQUEST_THIRD = 103;
    public static boolean IS_SCALE_FILL = true;

    // For option flag
    public static final int OFF = 0;
    public static final int ON = 1;
    public static int FLAG_FLASHLIGHT = OFF;
    public static int FLAG_GUIDELINE = OFF;
    public static int FLAG_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;

    public static int RESOURCE_FLASHLIGHT[] = { R.drawable.ic_flashlight_off, R.drawable.ic_flashlight,
            R.drawable.ic_flashlight_auto };
    public static int RESOURCE_GUIDELINE[] = { R.drawable.ic_guideline_off, R.drawable.ic_guideline };
    public static int RESOURCE_WATCH[] = { R.drawable.ic_watch_manual, R.drawable.ic_watch_05, R.drawable.ic_watch_10,
            R.drawable.ic_watch_15 };

    public static boolean DOIN_FINISH = false;
    public static ArrayList< String > STORAGE_PATH_LIST = new ArrayList< String >();
    public static String SAVE_PATH = ""; // path/to/save
    public static String FILE_PATH = ""; // absolute path/to/file
    public static boolean IS_SAVE_ORIGINAL_IMAGE = true;
    public static boolean IS_SAVE_ORIGINAL_IMAGE_OF_ALL = true;

    public static final class HandlerAskQuit extends Handler {
        public void handleMessage( Message msg ) {
            if ( msg.what == 0 ) {
                DOIN_FINISH = false;
            }
        }
    }
}