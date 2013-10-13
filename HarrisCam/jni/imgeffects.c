#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define A() (0xFF000000)
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
#define OPACITY(f, o, b) ( ((f)*(o)) + ((b)*((1)-(o))) ) // f : front value(uint8_t), o : opacity(0~1), b : background value(uint8_t)
#define RB(n) ( A() | (B(n)) | (R(n)) ) // n : uint32_t
#define GB(n) ( A() | (B(n)) | (G(n)) ) // n : uint32_t

uint8_t weightedGrayValue(float fR, float fG, float fB, uint32_t rgbValue);
uint32_t screenMerge(uint32_t nBitBack, uint32_t nBitFront, uint8_t opacity);

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

	int y, x;
	for (y=0; y<lBitmapInfo.height; y++)
	{
		for (x=0; x<lBitmapInfo.width; x++)
		{
			nBitG[y*lBitmapInfo.width+x] = COLOR(nBitR[y*lBitmapInfo.width+x], nBitG[y*lBitmapInfo.width+x], nBitB[y*lBitmapInfo.width+x]);
		}
	}

	AndroidBitmap_unlockPixels(env, bitR);
	AndroidBitmap_unlockPixels(env, bitG);
	AndroidBitmap_unlockPixels(env, bitB);
}

JNIEXPORT void JNICALL Java_com_main_harriscam_MainActivity_applyScreen(JNIEnv * env, jobject obj, jobject bitRGB)
{
	AndroidBitmapInfo lBitmapInfo;
	void * lBitmapRGB;

	AndroidBitmap_getInfo(env, bitRGB, &lBitmapInfo);
	AndroidBitmap_lockPixels(env, bitRGB, &lBitmapRGB);

	uint32_t* nBitRGB;

	nBitRGB = (uint32_t *)lBitmapRGB;

	uint32_t GrayValue = 0;

	int offset = 5;
	int y, x;
	for (y=0; y<lBitmapInfo.height; y++)
	{
		for (x=0; x<lBitmapInfo.width; x++)
		{
			GrayValue = weightedGrayValue(0.3, 0.3, 0.4, nBitRGB[y*lBitmapInfo.width+x]);
			nBitRGB[y*lBitmapInfo.width+x] = GRAYCOLOR(GrayValue);

			if (x >= offset) {
				uint32_t rgbRB = RB(nBitRGB[y*lBitmapInfo.width+x - offset]);
				nBitRGB[y*lBitmapInfo.width+x] = screenMerge(nBitRGB[y*lBitmapInfo.width+x], rgbRB, 127);
			}

			if (x < lBitmapInfo.width - offset) {
				uint32_t rgbGB = GB(nBitRGB[y*lBitmapInfo.width+x + offset]);
				nBitRGB[y*lBitmapInfo.width+x] = screenMerge(nBitRGB[y*lBitmapInfo.width+x], rgbGB, 127);
			}
		}
	}

	AndroidBitmap_unlockPixels(env, bitRGB);
}

uint8_t weightedGrayValue(float fR, float fG, float fB, uint32_t rgbValue)
{
	return BLUEtoBYTE(rgbValue) * fB + GREENtoBYTE(rgbValue) * fG + REDtoBYTE(rgbValue) * fR;
}

uint32_t screenMerge(uint32_t nBitBack, uint32_t nBitFront, uint8_t opacity)
{
	uint8_t cFrontRed = REDtoBYTE(nBitFront) * ((float)opacity / 255.0);
	uint8_t cFrontGreen = GREENtoBYTE(nBitFront) * ((float)opacity / 255.0);
	uint8_t cFrontBlue = BLUEtoBYTE(nBitFront) * ((float)opacity / 255.0);

	uint8_t cBackRed = REDtoBYTE(nBitBack) * (1.0 - ((float)opacity / 255.0));
	uint8_t cBackGreen = GREENtoBYTE(nBitBack) * (1.0 - ((float)opacity / 255.0));
	uint8_t cBackBlue = BLUEtoBYTE(nBitBack) * (1.0 - ((float)opacity / 255.0));

	return COLOR(BYTEtoBLUE(cFrontBlue), BYTEtoGREEN(cFrontGreen), BYTEtoRED(cFrontRed));
}