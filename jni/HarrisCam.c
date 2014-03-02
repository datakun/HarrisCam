#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

#include <android/log.h>
#include <string.h>
#include <android/bitmap.h>

#define A() (0xFF000000)
#define AA(a) ( (a)&(0xFF000000) )
#define B(b) ( (b)&(0xFF0000) )
#define G(g) ( (g)&(0xFF00) )
#define R(r) ( (r)&(0xFF) )
#define BLUEtoBYTE(b) ( (((b)&(0xFF0000))>>(16))&(0xFF) ) // b : uint32_t
#define GREENtoBYTE(g) ( (((g)&(0xFF00))>>(8))&(0xFF) ) // g : uint32_t
#define REDtoBYTE(r) ( (r)&(0xFF) ) // r : uint32_t
#define BYTEtoBLUE(b) ( ((b)<<(16))&(0xFF0000) ) // b : uint8_t
#define BYTEtoGREEN(g) ( ((g)<<(8))&(0xFF00) ) // g : uint8_t
#define BYTEtoRED(r) ( (r)&(0xFF) ) // r : uint8_t
#define COLOR(r, g, b) ( A() | (B(b)) | (G(g)) | (R(r)) ) // r, g, b : uint32_t
#define GRAYCOLOR(c) ( A() | (B((c)<<(16))) | (G((c)<<(8))) | (R((c))) ) // c : uint8_t
#define RB(n) ( (0xFFFF00FF)&(n) ) // n : uint32_t
#define GB(n) ( (0xFFFFFF00)&(n) ) // n : uint32_t

#define elog(...) __android_log_print(6, "junu", __VA_ARGS__);
#define jlog(...) __android_log_print(4, "junu", __VA_ARGS__);

JNIEXPORT void JNICALL Java_com_kimdata_camera_CameraPreview_naApplyHarris ( JNIEnv * env, jobject obj, jobject bitG, jobject bitR, jobject bitB ) {
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapR;
	void * lBitmapG;
	void * lBitmapB;

	AndroidBitmap_getInfo( env, bitG, &lBitmapInfo );
	AndroidBitmap_lockPixels( env, bitR, &lBitmapR );
	AndroidBitmap_lockPixels( env, bitG, &lBitmapG );
	AndroidBitmap_lockPixels( env, bitB, &lBitmapB );

	uint32_t *nBitR;
	uint32_t *nBitG;
	uint32_t *nBitB;

	nBitR = (uint32_t *) lBitmapR;
	nBitG = (uint32_t *) lBitmapG;
	nBitB = (uint32_t *) lBitmapB;

	int y, x;
	for ( y = 0; y < lBitmapInfo.height; y++ ) {
		for ( x = 0; x < lBitmapInfo.width; x++ ) {
			nBitG[y * lBitmapInfo.width + x] = COLOR( nBitR[y * lBitmapInfo.width + x],
					nBitG[y * lBitmapInfo.width + x], nBitB[y * lBitmapInfo.width + x] );
		}
	}

	AndroidBitmap_unlockPixels( env, bitR );
	AndroidBitmap_unlockPixels( env, bitG );
	AndroidBitmap_unlockPixels( env, bitB );
}
/*
jint JNI_OnLoad ( JavaVM* pVm, void* reserved ) {
	JNIEnv* env;
	if ( pVm->GetEnv( (void **) &env, JNI_VERSION_1_6 ) != JNI_OK ) {
		return -1;
	}

	JNINativeMethod nm[2];

	nm[0].name = "naApplyHarris";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*) naApplyHarris;

	jclass cls = env->FindClass( "com/kimdata/camera/CameraPreview" );

	env->RegisterNatives( cls, nm, 1 );

	return JNI_VERSION_1_6;
}
*/
