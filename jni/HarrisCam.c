/*
 Copyright (C) 2014, Jun-woo Kim (kimdatagoon@gmail.com)

 This code is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <jni.h>

#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>

#define A() (0xFF000000)
#define AA(a) ( (a)&(0xFF000000) )
#define B(b) ( (b)&(0xFF0000) )
#define G(g) ( (g)&(0xFF00) )
#define R(r) ( (r)&(0xFF) )
#define BLUEtoBYTE(b) ( (((b)&(0xFF0000))>>(16))&(0xFF) ) // b : uint32_t#define GREENtoBYTE(g) ( (((g)&(0xFF00))>>(8))&(0xFF) ) // g : uint32_t#define REDtoBYTE(r) ( (r)&(0xFF) ) // r : uint32_t#define BYTEtoBLUE(b) ( ((b)<<(16))&(0xFF0000) ) // b : uint8_t#define BYTEtoGREEN(g) ( ((g)<<(8))&(0xFF00) ) // g : uint8_t#define BYTEtoRED(r) ( (r)&(0xFF) ) // r : uint8_t#define COLOR(r, g, b) ( A() | (B(b)) | (G(g)) | (R(r)) ) // r, g, b : uint32_t#define GRAYCOLOR(c) ( A() | (B((c)<<(16))) | (G((c)<<(8))) | (R((c))) ) // c : uint8_t#define RB(n) ( (0xFFFF00FF)&(n) ) // n : uint32_t#define GB(n) ( (0xFFFFFF00)&(n) ) // n : uint32_t#define elog(...) __android_log_print(6, "junu", __VA_ARGS__);#define jlog(...) __android_log_print(4, "junu", __VA_ARGS__);

uint8_t weightedGrayValue ( float fR, float fG, float fB, uint32_t rgbValue );

JNIEXPORT void JNICALL Java_com_kimdata_harriscam_na_NativeHarrisCam_naApplyHarris ( JNIEnv *pEnv, jobject pObj, jobject bitG, jobject bitR, jobject bitB ) {
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapR;
	void * lBitmapG;
	void * lBitmapB;

	AndroidBitmap_getInfo( pEnv, bitG, &lBitmapInfo );
	AndroidBitmap_lockPixels( pEnv, bitR, &lBitmapR );
	AndroidBitmap_lockPixels( pEnv, bitG, &lBitmapG );
	AndroidBitmap_lockPixels( pEnv, bitB, &lBitmapB );

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

	AndroidBitmap_unlockPixels( pEnv, bitR );
	AndroidBitmap_unlockPixels( pEnv, bitG );
	AndroidBitmap_unlockPixels( pEnv, bitB );
}

JNIEXPORT void JNICALL Java_com_kimdata_harriscam_na_NativeHarrisCam_naApplyScreen ( JNIEnv *pEnv, jobject pObj, jobject bitResult, jobject bitOrigin, jobject bitTemp ) {
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapResult;
	void * lBitmapOrigin;
	void * lBitmapTemp;

	AndroidBitmap_getInfo( pEnv, bitResult, &lBitmapInfo );
	AndroidBitmap_lockPixels( pEnv, bitResult, &lBitmapResult );
	AndroidBitmap_lockPixels( pEnv, bitOrigin, &lBitmapOrigin );
	AndroidBitmap_lockPixels( pEnv, bitTemp, &lBitmapTemp );

	uint32_t* nBitResult;
	uint32_t* nBitOrigin;
	uint32_t* nBitTemp;

	nBitResult = (uint32_t *) lBitmapResult;
	nBitOrigin = (uint32_t *) lBitmapOrigin;
	nBitTemp = (uint32_t *) lBitmapTemp;

	uint32_t GrayValue = 0;

	int offset = 5;
	int y, x;
	for ( y = 0; y < lBitmapInfo.height; y++ ) {
		for ( x = 0; x < lBitmapInfo.width; x++ ) {
			// Background
			GrayValue = weightedGrayValue( 0.3, 0.3, 0.4, nBitOrigin[y * lBitmapInfo.width + x] );
			nBitTemp[y * lBitmapInfo.width + x] = GRAYCOLOR( GrayValue );
		}
	}

	uint32_t OpacityR = 0;
	uint32_t OpacityG = 0;
	uint32_t OpacityB = 0;

	float fOpacity = 0.0;

	for ( y = 0; y < lBitmapInfo.height; y++ ) {
		for ( x = 0; x < lBitmapInfo.width; x++ ) {
			if ( offset <= x && x < lBitmapInfo.width - offset ) {
				uint32_t rgbGB = GB( nBitTemp[y * lBitmapInfo.width + x + offset] );
				uint32_t rgbRB = RB( nBitTemp[y * lBitmapInfo.width + x - offset] );
				nBitResult[y * lBitmapInfo.width + x] = COLOR( rgbRB, rgbGB, rgbGB );
			} else {
				nBitResult[y * lBitmapInfo.width + x] = 0xFF000000;
			}

			// Apply Shutter Opacity
			if ( 0 <= ( y % 8 ) && ( y % 8 ) < 4 ) {
				OpacityR = R( (uint32_t)((uint32_t)(10) + R(nBitResult[y*lBitmapInfo.width+x]) * 0.95) );
				OpacityG = G( (uint32_t)(((uint32_t)(10)<<8) + G(nBitResult[y*lBitmapInfo.width+x]) * 0.95) );
				OpacityB = B( (uint32_t)(((uint32_t)(10)<<16) + B(nBitResult[y*lBitmapInfo.width+x]) * 0.95) );
				nBitResult[y * lBitmapInfo.width + x] = COLOR( OpacityR, OpacityG, OpacityB );
			}

		}
	}

	AndroidBitmap_unlockPixels( pEnv, bitResult );
	AndroidBitmap_unlockPixels( pEnv, bitOrigin );
	AndroidBitmap_unlockPixels( pEnv, bitTemp );
}

uint8_t weightedGrayValue ( float fR, float fG, float fB, uint32_t rgbValue ) {
	return BLUEtoBYTE(rgbValue) * fB + GREENtoBYTE( rgbValue ) * fG + REDtoBYTE( rgbValue ) * fR;
}
/*
jint JNI_OnLoad ( JavaVM* pVm, void* reserved ) {
	JNIEnv* env;

	if ( ( *pVm )->GetEnv( pVm, (void **) &env, JNI_VERSION_1_6 ) != JNI_OK ) {
		return -1;
	}

	JNINativeMethod nm[4];

	jlog( "before native1" );

	nm[0].name = "naApplyHarris";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*) naApplyHarris;

	jlog( "before native2" );

	nm[1].name = "naApplyScreen";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*) naApplyScreen;

	jlog( "before native3" );

	jclass cls = ( *env )->FindClass( env, "com/kimdata/harriscam/na/NativeHarrisCam" );
	//Register methods with env->RegisterNatives.
	( *env )->RegisterNatives( env, cls, nm, 2 );

	jlog( "before native4" );

	return JNI_VERSION_1_6;
}
*/
