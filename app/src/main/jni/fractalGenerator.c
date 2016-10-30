#include "fractal.h"
#include "fractalGenerator.h"

JNIEXPORT void JNICALL Java_com_fractaler_FractalView_drawFractal(JNIEnv *env, jobject thisObj, jobject bitmap, jobject jfractal) {

	char ret;
	Fractal fractal;

	// Get bitmap info
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &fractal.bitmapInfo)) < 0) {

		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	// Lock bitmap pixels
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &fractal.bitmap)) < 0) {

		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return;
	}


	// Get fractal info
	getFractalInfo(env,jfractal,&fractal);

	// Paint the bitmap pixels
	paintPixels(env,thisObj,&fractal,jfractal);

	// Unlock the bitmap pixels
	AndroidBitmap_unlockPixels(env, bitmap);

	//release elements
}



void paintPixels(JNIEnv *env, jobject viewObj, Fractal* fractal, jobject jfractal) {

	char detailIndex;
	short i, x, y, px, py, dx, dy, startX, endX, progressConst, progressVar;
	int colorIndex;
	float dwell;
	void *pixelData;
	BOOL toIterate;
	jclass FractalView, jFractal;
	jmethodID postInvalidate;
	jfieldID progressID, threadValidID, drawX0ID, drawY0ID, fixPassID, colorMapID;
	jintArray jcolors;


	FractalView = (*env)->GetObjectClass(env, viewObj);
	jFractal = (*env)->GetObjectClass(env, jfractal);

	// Get required fields from java
	drawX0ID = (*env)->GetFieldID(env, FractalView, "drawX0", "S");
	drawY0ID = (*env)->GetFieldID(env, FractalView, "drawY0", "S");
	progressID = (*env)->GetFieldID(env, FractalView, "progress", "B");
	fixPassID = (*env)->GetFieldID(env, FractalView, "fixPass", "Z");
	threadValidID = (*env)->GetFieldID(env, FractalView, "threadValid", "Z");
	postInvalidate = (*env)->GetMethodID(env, FractalView, "postInvalidate", "()V");

	// Get the colormap
	colorMapID = (*env)->GetFieldID(env, jFractal, "colorMap", "[I");
	jcolors = (*env)->GetObjectField(env, jfractal, colorMapID);
	if(!(fractal->colorMap = (*env)->GetIntArrayElements(env, jcolors, NULL))) {
		LOGE("Failed getting colors..");
		return;
	}

	// Calculate the roughness of the rendering algorithm passes
	if (fractal->bitmapInfo.height >= fractal->bitmapInfo.width) {

		detailIndex = (char) ((fractal->bitmapInfo.height > 12000) ? 0
				: (fractal->bitmapInfo.height > 2400) ? 1 : (fractal->bitmapInfo.height > 600) ? 2
						: (fractal->bitmapInfo.height > 200) ? 3 : 4);

	} else {

		detailIndex = (char) ((fractal->bitmapInfo.width > 12000) ? 0
				: (fractal->bitmapInfo.width > 2400) ? 1 : (fractal->bitmapInfo.width > 600) ? 2
						: (fractal->bitmapInfo.width > 200) ? 3 : 4);
	}


	// Check if need to fix image
	if ((char)(*env)->GetBooleanField(env, viewObj, fixPassID)) {

		(*env)->SetBooleanField(env, viewObj, fixPassID, JNI_FALSE);
		dx = 0;
		dy = 0;

	}

	// If image was still before moving then only calculate missing areas
	else {

		dx = (short)(*env)->GetShortField(env, viewObj, drawX0ID);
		dy = (short)(*env)->GetShortField(env, viewObj, drawY0ID);

	}

	// How many heights of bitmap we pass in total
	progressConst = fractal->bitmapInfo.height * (5-detailIndex);

	for(i=detailIndex; i < 5 ; i++) {

		// How many heights of bitmap we passed so far
		progressVar = progressConst - (5-i)*fractal->bitmapInfo.height;

		// Get the pixels pointer
		pixelData = fractal->bitmap;

		// Go over the height of the bitmap
		for (y = 0; y < fractal->bitmapInfo.height; y+=chunk[i]) {

			// Update progress
			(*env)->SetByteField(env, viewObj, progressID, (y+progressVar)*100/progressConst);

			(*env)->CallVoidMethod(env, viewObj, postInvalidate);

			// Calculate the whole map if required
			if (dx == 0 && dy == 0) {
				startX = 0;
				endX = fractal->bitmapInfo.width;
			}

			// Calculate only the missing areas if image was still before moving
			else {

				if (dx > 0)
					startX = 0;
				else if (dy > 0 && y < dy)
					startX = 0;
				else if (dy > 0 && y >= dy)
					startX = fractal->bitmapInfo.width + dx;
				else if (dy < 0 && y < fractal->bitmapInfo.height + dy -2)
					startX = fractal->bitmapInfo.width + dx;
				else
					startX = 0;

				if (dx < 0)
					endX = fractal->bitmapInfo.width;
				else if (dy > 0 && y < dy)
					endX = fractal->bitmapInfo.width;
				else if (dy > 0 && y >= dy)
					endX = dx;
				else if (dy < 0 && y < fractal->bitmapInfo.height + dy -2)
					endX = dx;
				else
					endX = fractal->bitmapInfo.width;

			}

			// Go over the width of the bitmap
			for (x = startX; x < endX; x+=chunk[i]) {

				// Make sure thread was not interrupted before proceeding with work
				if (!(*env)->GetBooleanField(env, viewObj, threadValidID)) {
					(*env)->ReleaseIntArrayElements(env, jcolors, fractal->colorMap,0);
					return;
				}

				// Determine square parents of the pixel point
				px = ((x % 2*chunk[i]) == 0) ? x : x - chunk[i];
				py = ((y % 2*chunk[i]) == 0) ? y : y - chunk[i];

				// On the first pass we calculate all points
				if(chunk[i] == chunk[detailIndex])
					toIterate = true;

				// No need to calculate a point which was already painted by parent square
				else if(x == px && y == py)
					toIterate = false;

				// Assume a color for points surrounded by the same color
				else if(allNeighborsSame(px,py,2*chunk[i],fractal))
					toIterate = false;

				else
					toIterate = true;

				if(toIterate) {

					// Get the iterations value
					if (fractal->type == MANDELBROT)
						dwell = iterateMandelbrot((double)x*fractal->complexScale + fractal->minX ,
								(double)-y*fractal->complexScale + fractal->maxY,
								fractal->constX,fractal->constY,
								fractal->iterations,fractal->bail,fractal->iterationValue);
					else if (fractal->type == JULIA)
						dwell = iterateJulia((double)x*fractal->complexScale + fractal->minX ,
								(double)-y*fractal->complexScale + fractal->maxY,
								fractal->constX,fractal->constY,
								fractal->iterations,fractal->bail,fractal->iterationValue);

					// Paint using colormap
					if (fractal->colorMethod == COLORMAP) {
						colorIndex = (short)dwell % COLOR_ARRAY_SIZE;
						fillSquare(x,y,chunk[i],((dwell == 0) ? 0xFF000000 : interpolateColor((unsigned int)fractal->colorMap[colorIndex],(unsigned int)fractal->colorMap[colorIndex+1],dwell-(short)dwell)),fractal);
					}
					// Paint using color delay
					else if (fractal->colorMethod == COLOR_DELAY) {

						fillSquare(x,y,chunk[i],getColorInt(dwell*fractal->delay.red,dwell*fractal->delay.green,dwell*fractal->delay.blue,255),fractal);
					}

				}

				// Paint according the parent square color
				else {
					fillSquare(x,y,chunk[i],getSrcColor(px,py,fractal->bitmap, &fractal->bitmapInfo),fractal);
				}

			}

			// Move to the next pixel line in the bitmap height
			pixelData = (char*) pixelData + fractal->bitmapInfo.stride*chunk[i];
		}

	}

	(*env)->ReleaseIntArrayElements(env, jcolors,fractal->colorMap,0);
}

JNIEXPORT void JNICALL Java_com_fractaler_FractalView_translateMap(JNIEnv *env, jobject thisObj, jobject srcMap, jobject dstMap) {

	char ret;
	short x, y, dx, dy, startX, endX, startY, endY;
	void *srcPixels, *dstPixels;
	AndroidBitmapInfo srcMapInfo, dstMapInfo;
	uint32_t *srcLine, *dstLine;
	jclass FractalView;
	jfieldID drawX0ID, drawY0ID;


	// Get maps info
	if ((ret = AndroidBitmap_getInfo(env, dstMap, &dstMapInfo)) < 0) {

		LOGE("AndroidBitmap_getInfo() dstMap failed ! error=%d", ret);
		return;
	}
	if ((ret = AndroidBitmap_getInfo(env, srcMap, &srcMapInfo)) < 0) {

		LOGE("AndroidBitmap_getInfo() dstMap failed ! error=%d", ret);
		return;
	}

	// Lock maps pixels
	if ((ret = AndroidBitmap_lockPixels(env, srcMap, &srcPixels)) < 0) {

		LOGE("AndroidBitmap_lockPixels() srcMap failed ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, dstMap, &dstPixels)) < 0) {

		LOGE("AndroidBitmap_lockPixels() dstMap failed ! error=%d", ret);
		return;
	}

	// Make sure bitmap dimensions match for copy procedure in bounds
	if (srcMapInfo.width != dstMapInfo.width || srcMapInfo.height != dstMapInfo.height
			|| srcMapInfo.stride != dstMapInfo.stride || srcMapInfo.format != dstMapInfo.format) {

		LOGE("Bitmap mismatch!");
		AndroidBitmap_unlockPixels(env, srcMap);
		AndroidBitmap_unlockPixels(env, dstMap);
		return;
	}

	// Get required fields from java
	FractalView = (*env)->GetObjectClass(env, thisObj);
	drawX0ID = (*env)->GetFieldID(env, FractalView, "drawX0", "S");
	drawY0ID = (*env)->GetFieldID(env, FractalView, "drawY0", "S");

	dx = (short)(*env)->GetShortField(env, thisObj, drawX0ID);
	dy = (short)(*env)->GetShortField(env, thisObj, drawY0ID);


	// Determine bitmap translation bounds
	startX = (dx > 0) ? dx : 0;
	endX = (dx > 0) ? dstMapInfo.width : dstMapInfo.width + dx;

	startY = (dy > 0) ? dy : 0;
	endY = (dy > 0) ? dstMapInfo.height : dstMapInfo.height + dy;


	// Adjust pixel pointers to bounds
	srcPixels = (char*) srcPixels + dstMapInfo.stride*((dy > 0) ? 0 : (-dy));
	dstPixels = (char*) dstPixels + dstMapInfo.stride*((dy > 0) ? dy : 0);

	// Go over maps and copy pixels from source map to destination map with translation by dx, dy
	for (y = startY; y < endY; y++) {

		srcLine = (uint32_t*) srcPixels;
		dstLine = (uint32_t*) dstPixels;

		for (x = startX; x < endX; x++)
			dstLine[x] = srcLine[x-dx];

		dstPixels = (char*) dstPixels + dstMapInfo.stride;
		srcPixels = (char*) srcPixels + dstMapInfo.stride;

	}

	// Unlock pixels of the bitmaps
	AndroidBitmap_unlockPixels(env, srcMap);
	AndroidBitmap_unlockPixels(env, dstMap);

}

