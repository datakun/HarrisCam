#include "HarrisCam.h"

JNIEXPORT void JNICALL Java_com_image_harriscam_HarrisNative_naApplyHarris ( JNIEnv *pEnv, jobject pObj, jobject bitG, jobject bitR,
jobject bitB ) {
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


JNIEXPORT void JNICALL Java_com_image_harriscam_HarrisNative_naApplyScreen ( JNIEnv *pEnv, jobject pObj, jobject bitResult, jobject bitOrigin, jobject bitTemp ) {
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

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} rgba;

JNIEXPORT void JNICALL Java_com_image_harriscam_HarrisNative_naBlurBitmap(JNIEnv* env, jobject obj, jobject bitmapIn, jobject bitmapOut, jint radius) {
    // Properties
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;
    AndroidBitmapInfo   infoOut;
    void*               pixelsOut;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {

        return;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {

        return;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0) {

    }

    int h = infoIn.height;
    int w = infoIn.width;

    rgba* input = (rgba*) pixelsIn;
    rgba* output = (rgba*) pixelsOut;

    int wm = w - 1;
    int hm = h - 1;
    int wh = w * h;
    int whMax = max(w, h);
    int div = radius + radius + 1;

    int r[wh];
    int g[wh];
    int b[wh];
    int rsum, gsum, bsum, x, y, i, yp, yi, yw;
    rgba p;
    int vmin[whMax];

    int divsum = (div + 1) >> 1;
    divsum *= divsum;
    int dv[256 * divsum];
    for (i = 0; i < 256 * divsum; i++) {
        dv[i] = (i / divsum);
    }

    yw = yi = 0;

    int stack[div][3];
    int stackpointer;
    int stackstart;
    int rbs;
    int ir;
    int ip;
    int r1 = radius + 1;
    int routsum, goutsum, boutsum;
    int rinsum, ginsum, binsum;

    for (y = 0; y < h; y++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        for (i = -radius; i <= radius; i++) {
            p = input[yi + min(wm, max(i, 0))];

            ir = i + radius; // same as sir

            stack[ir][0] = p.red;
            stack[ir][1] = p.green;
            stack[ir][2] = p.blue;
            rbs = r1 - abs(i);
            rsum += stack[ir][0] * rbs;
            gsum += stack[ir][1] * rbs;
            bsum += stack[ir][2] * rbs;
            if (i > 0) {
                rinsum += stack[ir][0];
                ginsum += stack[ir][1];
                binsum += stack[ir][2];
            } else {
                routsum += stack[ir][0];
                goutsum += stack[ir][1];
                boutsum += stack[ir][2];
            }
        }
        stackpointer = radius;

        for (x = 0; x < w; x++) {

            r[yi] = dv[rsum];
            g[yi] = dv[gsum];
            b[yi] = dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            ir = stackstart % div; // same as sir

            routsum -= stack[ir][0];
            goutsum -= stack[ir][1];
            boutsum -= stack[ir][2];

            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm);
            }
            p = input[yw + vmin[x]];

            stack[ir][0] = p.red;
            stack[ir][1] = p.green;
            stack[ir][2] = p.blue;

            rinsum += stack[ir][0];
            ginsum += stack[ir][1];
            binsum += stack[ir][2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            ir = (stackpointer) % div; // same as sir

            routsum += stack[ir][0];
            goutsum += stack[ir][1];
            boutsum += stack[ir][2];

            rinsum -= stack[ir][0];
            ginsum -= stack[ir][1];
            binsum -= stack[ir][2];

            yi++;
        }
        yw += w;
    }
    for (x = 0; x < w; x++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        yp = -radius * w;
        for (i = -radius; i <= radius; i++) {
            yi = max(0, yp) + x;

            ir = i + radius; // same as sir

            stack[ir][0] = r[yi];
            stack[ir][1] = g[yi];
            stack[ir][2] = b[yi];

            rbs = r1 - abs(i);

            rsum += r[yi] * rbs;
            gsum += g[yi] * rbs;
            bsum += b[yi] * rbs;

            if (i > 0) {
                rinsum += stack[ir][0];
                ginsum += stack[ir][1];
                binsum += stack[ir][2];
            } else {
                routsum += stack[ir][0];
                goutsum += stack[ir][1];
                boutsum += stack[ir][2];
            }

            if (i < hm) {
                yp += w;
            }
        }
        yi = x;
        stackpointer = radius;
        for (y = 0; y < h; y++) {
            output[yi].red = dv[rsum];
            output[yi].green = dv[gsum];
            output[yi].blue = dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            ir = stackstart % div; // same as sir

            routsum -= stack[ir][0];
            goutsum -= stack[ir][1];
            boutsum -= stack[ir][2];

            if (x == 0) vmin[y] = min(y + r1, hm) * w;
            ip = x + vmin[y];

            stack[ir][0] = r[ip];
            stack[ir][1] = g[ip];
            stack[ir][2] = b[ip];

            rinsum += stack[ir][0];
            ginsum += stack[ir][1];
            binsum += stack[ir][2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            ir = stackpointer; // same as sir

            routsum += stack[ir][0];
            goutsum += stack[ir][1];
            boutsum += stack[ir][2];

            rinsum -= stack[ir][0];
            ginsum -= stack[ir][1];
            binsum -= stack[ir][2];

            yi += w;
        }
    }

    // Unlocks everything
    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
}

int min(int a, int b) {
    return a > b ? b : a;
}

int max(int a, int b) {
    return a > b ? a : b;
}