#include <jni.h>

#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>

#define A() (0xFF000000)
#define B(b) ( (b)&(0xFF0000) )
#define G(g) ( (g)&(0xFF00) )
#define R(r) ( (r)&(0xFF) )
#define COLOR(r, g, b) ( A() | (B(b)) | (G(g)) | (R(r)) ) // r, g, b : uint32_t
#define BLUEtoBYTE(b) ( (((b)&(0xFF0000))>>(16))&(0xFF) ) // b : uint32_t
#define GREENtoBYTE(g) ( (((g)&(0xFF00))>>(8))&(0xFF) ) // g : uint32_t
#define REDtoBYTE(r) ( (r)&(0xFF) ) // r : uint32_t
#define GRAYCOLOR(c) ( A() | (B((c)<<(16))) | (G((c)<<(8))) | (R((c))) ) // c : uint8_t
#define RB(n) ( (0xFFFF00FF)&(n) ) // n : uint32_t
#define GB(n) ( (0xFFFFFF00)&(n) ) // n : uint32_t

#define elog(...) __android_log_print(6, "junu", __VA_ARGS__);
#define jlog(...) __android_log_print(4, "junu", __VA_ARGS__);

uint8_t weightedGrayValue ( float fR, float fG, float fB, uint32_t rgbValue );

JNIEXPORT void JNICALL Java_com_image_harriscam_HarrisNative_naApplyHarris
( JNIEnv *pEnv, jobject pObj, jobject bitResult, jobject bitG, jobject bitR, jobject bitB );