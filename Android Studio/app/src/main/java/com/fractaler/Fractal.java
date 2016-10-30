package com.fractaler;

import android.os.Parcel;
import android.os.Parcelable;


public class Fractal implements Parcelable {

	// Fractal types
	protected static final byte MANDELBROT = 0;
	protected static final byte JULIA = 1;
	// Value types
	protected static final byte ESCAPE_ITERATIONS = 0;
	protected static final byte RADIUS_SQUARE = 1;
	protected static final byte GAUSSIAN_INTEGER = 2;
	// Fractal bounds
	protected static final byte MIN_ITERATIONS = 100;
	protected static final short MAX_ITERATIONS = 5000;
	protected static final float MAX_ITERATIONS_CONST = 5.1f;
	protected static final byte MIN_BAIL = 4;
	protected static final double MAX_BAIL = 1e15;
	protected static final double MIN_RADIUS = 1e-15;
	protected static final double MAX_RADIUS = 1e15;
	// Color constants
	protected static final short COLOR_ARRAY_SIZE = 256;
	protected static final byte COLORMAP = 0;
	protected static final byte COLOR_DELAY = 1;
	

	protected byte type, iterationValue, colorMethod;
	protected short iterations;
	protected float iterationsConst;
	protected double minX, maxY, complexScale, focusX, focusY,constX, constY, radius, bail;
	
	// Color Variables
	protected int colorMap[];
	protected float redDelay, greenDelay, blueDelay;
	

    public Fractal() {
    	
    	initColors();
		type = MANDELBROT;
		iterationValue = ESCAPE_ITERATIONS;
		colorMethod = COLOR_DELAY;
		iterations = MIN_ITERATIONS;
		iterationsConst = 3.5f;
		radius = 1.5;
		focusX = -0.7;
		focusY = 0;
		constX = 0;
		constY = 0;
		bail = 4;
    }
  
    /**
    * Storing the Fractal data to Parcel object
    **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	
        dest.writeByte(type);
        dest.writeByte(iterationValue);
        dest.writeByte(colorMethod);
        dest.writeFloat(iterationsConst);
        dest.writeInt(iterations);
        dest.writeDouble(radius);
        dest.writeDouble(focusX);
        dest.writeDouble(focusY);  
        dest.writeDouble(constX);
        dest.writeDouble(constY);
        dest.writeDouble(complexScale);
        dest.writeDouble(minX);
        dest.writeDouble(maxY);
        dest.writeDouble(bail);    
    }
    
    /**
    * Retrieving Fractal data from Parcel object
    * This constructor is invoked by the method createFromParcel(Parcel source) of
    * the object CREATOR
    **/
    private Fractal(Parcel in){
    	
    	type = in.readByte();
    	iterationValue = in.readByte();
    	colorMethod = in.readByte();
    	iterationsConst = in.readFloat();
    	iterations = (short) in.readInt();
    	radius = in.readDouble();
    	focusX = in.readDouble();
    	focusY = in.readDouble();
    	constX = in.readDouble();
    	constY = in.readDouble();
    	complexScale = in.readDouble();
    	minX = in.readDouble();
    	maxY = in.readDouble();
    	bail = in.readDouble();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Fractal> CREATOR = new Parcelable.Creator<Fractal>() {

        @Override
        public Fractal createFromParcel(Parcel source) {
            return new Fractal(source);
        }

        @Override
        public Fractal[] newArray(int size) {
            return new Fractal[size];
        }
    };
      
	public void setComplexAxis(short width, short height, boolean vertical) {
		
		// Simplification of complex plane representation for image generation
		if (vertical) {
	
			complexScale = 2 * radius / width;
			minX = focusX - radius;
			maxY = focusY + height * complexScale / 2;
		}
	
		else {
	
			complexScale = 2 * radius / height;
			minX = focusX - width * complexScale / 2;
			maxY = focusY + radius;
		}
	
	}
	
	private void initColors() {

		colorMap = new int[COLOR_ARRAY_SIZE];

		for (int i = 0; i < COLOR_ARRAY_SIZE; i++) {
			colorMap[i] = ((255 << 24) | (i << 16) | (0 << 8) | (0));
		}

		redDelay = 2;
		greenDelay = 4;
		blueDelay = 10;
	}
	
}
