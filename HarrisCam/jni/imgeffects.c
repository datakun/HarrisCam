#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define color(R,G,B) ((0xff000000) | ((B)&(0xFF0000)) | ((G)&(0xFF00)) | (((R)&(0xFF))))

JNIEXPORT void JNICALL Java_com_main_harriscam_MainActivity_applyHarris(JNIEnv * env, jobject obj, jobject bitG, jobject bitR, jobject bitB)
{
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapR;
	void * lBitmapG;
	void * lBitmapB;

	AndroidBitmap_getInfo(env, bitG, &lBitmapInfo);
	AndroidBitmap_lockPixels(env, bitR, &lBitmapR);
	AndroidBitmap_lockPixels(env, bitG, &lBitmapG);
	AndroidBitmap_lockPixels(env, bitB, &lBitmapB);

	uint32_t *nBitR;
	uint32_t *nBitG;
	uint32_t *nBitB;

	nBitR = (uint32_t *)lBitmapR;
	nBitG = (uint32_t *)lBitmapG;
	nBitB = (uint32_t *)lBitmapB;

	int y,x;
	for (y=0; y<lBitmapInfo.height; y++)
	{
		for (x=0; x<lBitmapInfo.width; x++)
		{
			nBitG[y*lBitmapInfo.width+x] = color(nBitR[y*lBitmapInfo.width+x], nBitG[y*lBitmapInfo.width+x], nBitB[y*lBitmapInfo.width+x]);
		}
	}

	AndroidBitmap_unlockPixels(env, bitR);
	AndroidBitmap_unlockPixels(env, bitG);
	AndroidBitmap_unlockPixels(env, bitB);
}

JNIEXPORT void JNICALL Java_com_main_harriscam_MainActivity_applyScreen(JNIEnv * env, jobject obj, jobject bitG, jobject bitR, jobject bitB)
{
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapR;
	void * lBitmapG;
	void * lBitmapB;

	AndroidBitmap_getInfo(env, bitG, &lBitmapInfo);
	AndroidBitmap_lockPixels(env, bitR, &lBitmapR);
	AndroidBitmap_lockPixels(env, bitG, &lBitmapG);
	AndroidBitmap_lockPixels(env, bitB, &lBitmapB);

	uint32_t *nBitR;
	uint32_t *nBitG;
	uint32_t *nBitB;

	nBitR = (uint32_t *)lBitmapR;
	nBitG = (uint32_t *)lBitmapG;
	nBitB = (uint32_t *)lBitmapB;

	int y,x;
	for (y=0; y<lBitmapInfo.height; y++)
	{
		for (x=0; x<lBitmapInfo.width; x++)
		{
			nBitG[y*lBitmapInfo.width+x] = color(nBitR[y*lBitmapInfo.width+x], nBitG[y*lBitmapInfo.width+x], nBitB[y*lBitmapInfo.width+x]);
		}
	}

	AndroidBitmap_unlockPixels(env, bitR);
	AndroidBitmap_unlockPixels(env, bitG);
	AndroidBitmap_unlockPixels(env, bitB);
}
