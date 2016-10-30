package com.fractaler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Colorbar extends View {

	private static final byte MARKER_1 = 1;
	private static final byte MARKER_2 = 2;
	private static final byte GET_MIN = 0;
	private static final byte GET_MAX = 1;
	private static final byte EDGE_SENSITIVITY = 5;
	private static final byte DELAY_MAGNITUDE = 10;

	private boolean gotDimensions, changing, smoothing;
	private byte markerSwitch;
	private short unitHeight, startHeight, mark1, mark2, sectionStart, sectionEnd;
	private short[] reds, greens, blues;
	private Paint paint;
	public short redChange, greenChange, blueChange;

	public Colorbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public Colorbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Colorbar(Context context) {
		super(context);
		init();
	}

	private void init() {

		gotDimensions = false;
		changing = false;
		smoothing = true;
		markerSwitch = MARKER_1;
		mark1 = 0;
		mark2 = Fractal.COLOR_ARRAY_SIZE -1;
		reds = new short[Fractal.COLOR_ARRAY_SIZE];
		greens = new short[Fractal.COLOR_ARRAY_SIZE];
		blues = new short[Fractal.COLOR_ARRAY_SIZE];
		paint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Get colorbar dimensions
		if (!gotDimensions) {

			unitHeight = (short) (getHeight() / Fractal.COLOR_ARRAY_SIZE);
			startHeight = (short) ((float) ((getHeight() / Fractal.COLOR_ARRAY_SIZE) - unitHeight)
					* Fractal.COLOR_ARRAY_SIZE / 2);
			gotDimensions = true;

		}

		// Draw the colorbar itself
		for (short i = startHeight; i < Fractal.COLOR_ARRAY_SIZE; i++) {

			paint.setARGB(255, reds[i], greens[i], blues[i]);
			canvas.drawRect(0, i * unitHeight, getWidth(), i * unitHeight
					+ unitHeight, paint);

		}

		// Draw section markers
		paint.setColor(Color.BLACK);
		canvas.drawRect(0, mark1 * unitHeight, getWidth(), mark1 * unitHeight
				+ unitHeight, paint);
		canvas.drawRect(0, mark2 * unitHeight, getWidth(), mark2 * unitHeight
				+ unitHeight, paint);

		// Draw the change in section according to the color sliders
		if (changing) {

			for (short i = sectionStart; i < sectionEnd; i++) {

				paint.setARGB(255, redChange, greenChange, blueChange);
				canvas.drawRect(0, i * unitHeight, getWidth(), i * unitHeight
						+ unitHeight, paint);
			}

			changing = false;

		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		short height;

		switch (action) {

		case MotionEvent.ACTION_DOWN: {

			// Get section marker positions

			height = (short) event.getY();

			if (markerSwitch == MARKER_1) {

				mark1 = (short) ((height >= startHeight && height <= Fractal.COLOR_ARRAY_SIZE
						* unitHeight) ? height / unitHeight
						: (height >= startHeight) ? Fractal.COLOR_ARRAY_SIZE - 1 : 0);
				
				// Make clicking edges easier
				
				mark1 = (mark1 < EDGE_SENSITIVITY) ? 0 : (mark1 > Fractal.COLOR_ARRAY_SIZE - EDGE_SENSITIVITY) ? Fractal.COLOR_ARRAY_SIZE - 1 : mark1; 

				markerSwitch = MARKER_2;
			}

			else if (markerSwitch == MARKER_2) {

				mark2 = (short) ((height >= startHeight && height <= Fractal.COLOR_ARRAY_SIZE
						* unitHeight) ? height / unitHeight
						: (height >= startHeight) ? Fractal.COLOR_ARRAY_SIZE - 1 : 0);
				
				mark2 = (mark2 < EDGE_SENSITIVITY) ? 0 : (mark2 > Fractal.COLOR_ARRAY_SIZE - EDGE_SENSITIVITY) ? Fractal.COLOR_ARRAY_SIZE - 1 : mark2;

				markerSwitch = MARKER_1;
			}
			
			performClick();
			invalidate();
			break;
		}

		}

		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return true;
	}

	public void changeRed(float scale) {
		changing = true;
		redChange = (short) (255 * scale);
		getSection();
		invalidate();

	}

	public void changeGreen(float scale) {
		changing = true;
		greenChange = (short) (255 * scale);
		getSection();
		invalidate();

	}

	public void changeBlue(float scale) {
		changing = true;
		blueChange = (short) (255 * scale);
		getSection();
		invalidate();

	}

	public void changeBright(float scale) {

		short min, max, range;

		changing = true;

		// Determine brightness range;
		
		min = getMinMaxChange(GET_MIN);
		max = getMinMaxChange(GET_MAX);

		range = (short) (255 - max + min);

		// Adjust Brightness
		redChange = (short) (redChange - min + range * scale);
		greenChange = (short) (greenChange - min + range * scale);
		blueChange = (short) (blueChange - min + range * scale) ;
		
		getSection();
		invalidate();
	}
	
	public void updateColors() {
		
		short range, midSection, redLowStep, redHighStep, greenLowStep, greenHighStep, blueLowStep, blueHighStep;
		
		getSection();		
		adjustEdges();
		
		// Get section length and the middle member
		range = (short) (sectionEnd - sectionStart);
		midSection = (short) (sectionStart + range/2);
		
		// Make sure we are not dividing by zero and we have at least 1 color block to change
		range = (range > 0) ? range : 1;
			
		// Extend colors to edge colors
		if(smoothing) {
			
			reds[midSection] = redChange;
			greens[midSection] = greenChange;
			blues[midSection] = blueChange;
			
			redLowStep =  (short) (2*(reds[sectionStart -1] - redChange)/range);
			redHighStep =  (short) (2*(reds[sectionEnd +1] - redChange)/range);
			
			greenLowStep =  (short) (2*(greens[sectionStart -1] - greenChange)/range);
			greenHighStep =  (short) (2*(greens[sectionEnd +1] - greenChange)/range);
			
			blueLowStep =  (short) (2*(blues[sectionStart -1] - blueChange)/range);
			blueHighStep =  (short) (2*(blues[sectionEnd +1] - blueChange)/range);
			
			for(short i = 1; i <= range/2 ; i++) {
				
				reds[midSection - i] = (short) (reds[midSection] + redLowStep*i);
				reds[midSection + i] = (short) (reds[midSection] + redHighStep*i);
				
				greens[midSection - i] = (short) (greens[midSection] + greenLowStep*i);
				greens[midSection + i] = (short) (greens[midSection] + greenHighStep*i);
				
				blues[midSection - i] = (short) (blues[midSection] + blueLowStep*i);
				blues[midSection + i] = (short) (blues[midSection] + blueHighStep*i);

			}
		}
		
		// Just fill the colors without fancy smoothing
		else if (!smoothing) {
			
			for(short i = sectionStart; i <= sectionEnd; i++) {
				
				reds[i] = redChange;
				greens[i] = greenChange;
				blues[i] = blueChange;
				
			}
			
		}

		invalidate();
			
	}
		
	
	private void getSection() {
		
		if (mark1 >= mark2) {

			sectionStart = mark2;
			sectionEnd = (short) (sectionStart + mark1 - mark2);
			
		} else {

			sectionStart = mark1;
			sectionEnd = (short) (sectionStart + mark2 - mark1);
		}
		
	}
	
	private short getMinMaxChange(byte select) {
		
		short min, max;
		

		if (redChange <= greenChange) {
			min = redChange;
			max = greenChange;
		} else {
			min = greenChange;
			max = redChange;
		}

		min = (min <= blueChange) ? min : blueChange;
		max = (max >= blueChange) ? max : blueChange;
			
		if (select == GET_MIN)
			return min;
		if (select == GET_MAX)
			return max;
		
		return -1;	
		
	}
	
	private void adjustEdges() {
		
		// Adjust section to not go out of array bounds;
		sectionStart = (sectionStart > 0) ? sectionStart : 1;	
		sectionEnd = (sectionEnd < Fractal.COLOR_ARRAY_SIZE - 1) ? sectionEnd : Fractal.COLOR_ARRAY_SIZE - 2;
		
		// Adjust edges
		reds[1] = (short) ((reds[2] -1 > 0) ? reds[2] -1 : 0);
		greens[1] = (short) ((greens[2] -1 > 0) ? greens[2] -1 : 0);
		blues[1] = (short) ((blues[2] -1 > 0) ? blues[2] -1 : 0);
		
		reds[0] = (short) ((reds[1] -1 > 0) ? reds[1] -1 : 0);
		greens[0] = (short) ((greens[1] -1 > 0) ? greens[1] -1 : 0);
		blues[0] = (short) ((blues[1] -1 > 0) ? blues[1] -1 : 0);
		
		reds[Fractal.COLOR_ARRAY_SIZE - 1] = (short) ((reds[0] -1 > 0) ? reds[0] -1 : 0);
		greens[Fractal.COLOR_ARRAY_SIZE - 1] = (short) ((greens[0] -1 > 0) ? greens[0] -1 : 0);
		blues[Fractal.COLOR_ARRAY_SIZE - 1] = (short) ((blues[0] -1 > 0) ? blues[0] -1 : 0);
			
		reds[Fractal.COLOR_ARRAY_SIZE - 2] = (short) ((reds[Fractal.COLOR_ARRAY_SIZE - 1] -1 > 0) ? reds[Fractal.COLOR_ARRAY_SIZE - 1] -1 : 0);
		greens[Fractal.COLOR_ARRAY_SIZE - 2] = (short) ((greens[Fractal.COLOR_ARRAY_SIZE - 1] -1 > 0) ? greens[Fractal.COLOR_ARRAY_SIZE - 1] -1 : 0);
		blues[Fractal.COLOR_ARRAY_SIZE - 2] = (short) ((blues[Fractal.COLOR_ARRAY_SIZE - 1] -1 > 0) ? blues[Fractal.COLOR_ARRAY_SIZE - 1] -1 : 0);


	}
	
	public void importColors(int[] colors) {
		
		for(short i = 0; i < Fractal.COLOR_ARRAY_SIZE; i++) {
			
			reds[i] = (short) (colors[i] & 0x000000FF);
			greens[i] = (short) ((colors[i] & 0x0000FF00) >> 8);
			blues[i] = (short) ((colors[i] & 0x00FF0000) >> 16);
					
		}
			
	}
	
	public int[] exportColors() {
		
		int[] colors = new int[Fractal.COLOR_ARRAY_SIZE];
		
		for(short i = 0; i < Fractal.COLOR_ARRAY_SIZE; i++) {
			
			colors[i] = ((255 << 24)|(blues[i] << 16)|(greens[i] << 8)|(reds[i]));		
		}
		
		return colors;
		
	}
	public float[] exportDelay() {
		
		float[] delay = {DELAY_MAGNITUDE*redChange/Fractal.COLOR_ARRAY_SIZE,DELAY_MAGNITUDE*greenChange/Fractal.COLOR_ARRAY_SIZE,DELAY_MAGNITUDE*blueChange/Fractal.COLOR_ARRAY_SIZE};
		return delay;
	}
	
	public void setSmoothing(boolean state) {
		
		smoothing = state;
		
	}	
	
	public int getTextColor() {
		
		return ((255 << 24)|(redChange << 16)|(greenChange << 8)|(blueChange));
		
	}
	
}
