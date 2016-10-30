#include "fractal.h"

float iterateMandelbrot(double px, double py, double cx, double cy, short maxItr, double bail, char itrVal) {


	double zx,zy,zx2,zy2,val;
	short i=0;

	zx=cx;
	zy=cy;
	zx2=zx*zx;
	zy2=zy*zy;

	while(zx2+zy2<bail && i<maxItr) {

		zy=2*zx*zy+py;
		zx=zx2-zy2+px;
		zx2=zx*zx;
		zy2=zy*zy;
		i++;
	}

	if(i==maxItr)
		return 0;

	switch(itrVal) {

	case ESCAPE_ITERATIONS:
		return i + 1 - log(0.5*log(zx2+zy2))/LOG2;

	case RADIUS_SQUARE:
		return zx2+zy2 + i;
	}

}
float iterateJulia(double px, double py, double cx, double cy, short maxItr, double bail, char itrVal) {


	double zx,zy,zx2,zy2;
	short i=0;

	zx=px;
	zy=py;
	zx2=zx*zx;
	zy2=zy*zy;

	while(zx2+zy2<bail && i<maxItr) {

		zy=2*zx*zy+cy;
		zx=zx2-zy2+cx;
		zx2=zx*zx;
		zy2=zy*zy;
		i++;
	}

	if(i==maxItr)
		return 0;

	switch(itrVal) {

	case ESCAPE_ITERATIONS:
		return i + 1 - log(0.5*log(zx2+zy2))/LOG2;

	case RADIUS_SQUARE:
		return zx2+zy2 + i;

	}

}

BOOL allNeighborsSame(short px, short py, short spacing, Fractal *fractal) {

	unsigned int c;
	int pxmin, pxmax, pymin, pymax;

	pxmin = (px-spacing > 0) ? px-spacing : 1;
	pxmax = (px+spacing < fractal->bitmapInfo.width) ? px+spacing : fractal->bitmapInfo.width;
	pymin = (py-spacing > 0) ? py-spacing : 1;
	pymax = (py+spacing < fractal->bitmapInfo.height) ? py+spacing : fractal->bitmapInfo.height;

	c=getSrcColor(pxmin,pymin,fractal->bitmap, &fractal->bitmapInfo);

	if(c!=getSrcColor(px,pymin,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(pxmax,pymin,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(pxmin,py,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(pxmin,pymax,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(px,pymax,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(pxmax,pymax,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	if(c!=getSrcColor(pxmax,py,fractal->bitmap, &fractal->bitmapInfo))
		return false;

	return true;
}

void fillSquare(short x, short y, short size, unsigned int color, Fractal *fractal) {

	int i, j;
	void *pixelData = (char*) fractal->bitmap + fractal->bitmapInfo.stride*y;
	uint32_t *line;

	for(i=0; i < size && (y+i) < fractal->bitmapInfo.height; i++) {

		line = (uint32_t*) pixelData;

		for(j=0; j < size && (x+j) < fractal->bitmapInfo.width; j++) {

			line[x+j] = color;
		}

		pixelData = (char*) fractal->bitmap + fractal->bitmapInfo.stride*(y+i);
	}
}

unsigned int getSrcColor(short x, short y, void *bitmap, AndroidBitmapInfo *info) {

	void *pixelData = (char *) bitmap + info->stride*y;
	uint32_t *line = (uint32_t*) pixelData;

	return (unsigned int) line[x];
}

unsigned int getColorInt(unsigned char red, unsigned char green, unsigned char blue, unsigned char alpha) {

	return (unsigned int) (alpha << 24) | (blue << 16) | (green << 8) | (red);

}

void getIntRGB(unsigned int colorInt, unsigned char rgb[]) {

	if(colorInt == 0xFF000000) {
		rgb[0] = 0;
		rgb[1] = 0;
		rgb[2] = 0;
	}
	else {
		rgb[0] = (colorInt & 0x000000FF);
		rgb[1] = ((colorInt & 0x0000FF00) >> 8);
		rgb[2] = ((colorInt & 0x00FF0000) >> 16);
	}

}

void getFractalInfo(JNIEnv *env, jobject jfractal, Fractal *cfractal) {

	jclass jFractal;
	jfieldID minXID, maxYID, complexScaleID, constXID, constYID,
		iterationsID, iterationValueID, colorMethodID, typeID,
		colorMapID, redDelayID, greenDelayID, blueDelayID, bailID;

	jFractal = (*env)->GetObjectClass(env, jfractal);

	typeID = (*env)->GetFieldID(env, jFractal, "type", "B");
	iterationValueID = (*env)->GetFieldID(env, jFractal, "iterationValue", "B");
	colorMethodID = (*env)->GetFieldID(env, jFractal, "colorMethod", "B");
	minXID = (*env)->GetFieldID(env, jFractal, "minX", "D");
	maxYID = (*env)->GetFieldID(env, jFractal, "maxY", "D");
	complexScaleID = (*env)->GetFieldID(env, jFractal, "complexScale", "D");
	iterationsID = (*env)->GetFieldID(env, jFractal, "iterations", "S");
	bailID = (*env)->GetFieldID(env, jFractal, "bail", "D");
	redDelayID = (*env)->GetFieldID(env, jFractal, "redDelay", "F");
	greenDelayID = (*env)->GetFieldID(env, jFractal, "greenDelay", "F");
	blueDelayID = (*env)->GetFieldID(env, jFractal, "blueDelay", "F");

	cfractal->type = (char)(*env)->GetByteField(env, jfractal, typeID);
	cfractal->iterationValue = (char)(*env)->GetByteField(env, jfractal, iterationValueID);
	cfractal->colorMethod = (char)(*env)->GetByteField(env, jfractal, colorMethodID);
	cfractal->minX = (double)(*env)->GetDoubleField(env, jfractal, minXID);
	cfractal->maxY = (double)(*env)->GetDoubleField(env, jfractal, maxYID);
	cfractal->complexScale = (double)(*env)->GetDoubleField(env, jfractal, complexScaleID);
	cfractal->iterations = (short)(*env)->GetShortField(env, jfractal, iterationsID);
	cfractal->bail = (double)(*env)->GetDoubleField(env, jfractal, bailID);

	cfractal->delay.red = (float)(*env)->GetFloatField(env, jfractal, redDelayID);
	cfractal->delay.green = (float)(*env)->GetFloatField(env, jfractal, greenDelayID);
	cfractal->delay.blue = (float)(*env)->GetFloatField(env, jfractal, blueDelayID);


	if (cfractal->type == MANDELBROT) {

		cfractal->constX = 0;
		cfractal->constY = 0;

	}
	else if (cfractal->type == JULIA) {

		constXID = (*env)->GetFieldID(env, jFractal, "constX", "D");
		constYID = (*env)->GetFieldID(env, jFractal, "constY", "D");

		cfractal->constX = (float)(*env)->GetDoubleField(env, jfractal, constXID);
		cfractal->constY = (float)(*env)->GetDoubleField(env, jfractal, constYID);

	}

}

unsigned int interpolateColor(unsigned int startColor, unsigned int endColor, float length) {

	unsigned char startRGB[3] = {0};
	unsigned char endRGB[3] = {0};

	// Get start and end colors
	getIntRGB(startColor,startRGB);
	getIntRGB(endColor,endRGB);


	// linear interpolation
	return getColorInt(startRGB[0] + length * (endRGB[0] - startRGB[0]),
			startRGB[1] + length * (endRGB[1] - startRGB[1]),
			startRGB[2] + length * (endRGB[2] - startRGB[2]), 255);
}

double getNearestGaussianIntegerDistance(double zx, double zy) {

	int gx, gy;

	gx = (abs(zx - (int)zx) >= 0.5) ? ceil(zx) : floor(zx);
	gy = (abs(zy - (int)zy) >= 0.5) ? ceil(zy) : floor(zy);

	return sqrt((zx-gx)*(zx-gx)+(zy-gy)*(zy-gy));
}

