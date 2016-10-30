
#ifndef FRACTAL_H

#define FRACTAL_H

#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define LOG_TAG "fractalGenerator"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef enum {false,true} BOOL;


typedef struct delay
{
	float red;
	float green;
	float blue;

}Delay;

typedef struct fractal
{
	char type;
	char iterationValue;
	char colorMethod;
	double minX;
	double maxY;
	double complexScale;
	double constX;
	double constY;
	short iterations;
	double bail;
	jint *colorMap;
	Delay delay;
	void *bitmap;
	AndroidBitmapInfo bitmapInfo;

}Fractal;

#define MANDELBROT 0
#define JULIA 1
#define COLORMAP 0
#define COLOR_DELAY 1
#define ESCAPE_ITERATIONS 0
#define RADIUS_SQUARE 1
#define GAUSSIAN_INTEGER 2
#define COLOR_ARRAY_SIZE 256
#define LOG2 0.6931471806

float iterateMandelbrot(double px, double py, double cx, double cy, short maxItr, double bail, char itrVal);
float iterateJulia(double px, double py, double cx, double cy, short maxItr, double bail, char itrVal);
BOOL allNeighborsSame(short px, short py, short spacing, Fractal *fractal);
void fillSquare(short x, short y, short size, unsigned int color, Fractal *fractal);
unsigned int getSrcColor(short x, short y, void *bitmap, AndroidBitmapInfo *info);
unsigned int getColorInt(unsigned char red, unsigned char green, unsigned char blue, unsigned char alpha);
void getIntRGB(unsigned int colorInt, unsigned char rgb[]);
void getFractalInfo(JNIEnv *env, jobject jfractal, Fractal *cfractal);
unsigned int interpolateColor(unsigned int startColor, unsigned int endColor, float length);
double getNearestGaussianIntegerDistance(double zx, double zy);


#endif
