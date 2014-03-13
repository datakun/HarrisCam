package com.kimdata.harriscam.na;

import android.graphics.Bitmap;

public class NativeHarrisCam {
	static {
		System.loadLibrary( "HarrisCam" );
	}

	public static native void naApplyHarris( Bitmap bitG, Bitmap bitR, Bitmap bitB );

	public static native void naApplyScreen( Bitmap bitResult, Bitmap bitOrigin, Bitmap bitTemp );
}
