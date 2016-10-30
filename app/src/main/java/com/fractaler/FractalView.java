package com.fractaler;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FractalView extends View {

	// Text display constants
	private static final byte SHOW_DEFAULT = 'd';
	private static final byte SHOW_TIME = 't';
	private static final byte SHOW_COORDINATE = 'c';
	private static final byte SHOW_MOVE = 'm';
	private static final byte SHOW_ZOOM = 'z';
	private static final byte TEXT_SIZE = 22;
	// Control constants
	private static final float ZOOM_CONSTANT = 0.0055f;
	private static final float MOVE_SENSITIVITY = 3.0f;

	// View variables
	private boolean gotBitmap, needRefresh, showInfo, zoomView, moveView,
			verticalView, resetReq, changing;
	private byte txtDisplay, firstIndex, lastIndex;
	private short mapWidth, mapHeight, drawX0, drawY0;
	private long time;
	private float touchX, touchY, moveX, moveY, zoom0, zoom, txtOffset,
			txtBoxScale;
	private double focusX0, focusY0, radius0;
	private Bitmap fractalMap, tempMap;
	private Paint txtPaint, txtBackground;
	private Rect clipBounds, bitmapRect, canvasRect;
	private Thread render;
	private Fractal fractal;
	private Runnable generator;

	// Native variables
	private byte progress;
	private boolean threadValid, fixPass;

	public FractalView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public FractalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FractalView(Context context) {
		super(context);
		init();
	}

	static {

		System.loadLibrary("fractalGenerator");
	}

	private native void drawFractal(Bitmap fmap, Fractal jfractal);

	private native void translateMap(Bitmap src, Bitmap dst);


	private void init() {

		initRender();
		fractal = new Fractal();
		gotBitmap = false;
		needRefresh = true;
		fixPass = true;
		txtPaint = new Paint();
		txtBackground = new Paint();
		txtPaint.setColor(Color.WHITE);
		txtBackground.setARGB(80, 30, 30, 30);
		clipBounds = new Rect();
		bitmapRect = new Rect();
		canvasRect = new Rect();
	}

	private void initRender() {

		render = new Thread(generator);

		generator = new Runnable() {

			@Override
			public void run() {

				progress = 0;
				threadValid = true;
				time = System.currentTimeMillis();
				drawFractal(fractalMap, fractal);
				time = System.currentTimeMillis() - time;
				postGenerate();

			}
		};
	}

	private void setMap(int width, int height) {

		mapWidth = (short) width;
		mapHeight = (short) height;

		// Determine screen orientation
		verticalView = mapHeight >= mapWidth;

		canvasRect.set(0, 0, mapWidth, mapHeight);
		bitmapRect.set(0, 0, mapWidth, mapHeight);

		fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
				Bitmap.Config.ARGB_8888);

		fractal.setComplexAxis((short) width, (short) height, verticalView);

		if (verticalView) {

			// Calculate display text dimensions
			txtPaint.setTextSize(TEXT_SIZE * mapHeight / 1000);
			txtOffset = (TEXT_SIZE * mapHeight / 1000) * 0.6f;

		} else {

			txtPaint.setTextSize(TEXT_SIZE * mapWidth / 1000);
			txtOffset = (TEXT_SIZE * mapWidth / 1000) * 0.6f;
		}

		txtBoxScale = TEXT_SIZE / 650f;

	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		canvas.getClipBounds(clipBounds);

		// Setting map with screen dimensions
		if (!gotBitmap) {

			setMap(getWidth(), getHeight());
			gotBitmap = true;
			resetReq = true;
		}

		// Drawing render thread progress
		if (render.isAlive()) {

			// Stop thread and fix unfinished areas if interrupted
			if (render.isInterrupted()) {

				threadValid = false;
				fixPass = true;
			}

			showInfo = true;

			if (zoomView)
				canvas.drawBitmap(tempMap, bitmapRect, canvasRect, null);

			canvas.drawBitmap(fractalMap, 0, 0, null);

			canvas.drawRect(0, 0, mapWidth, ((verticalView) ? mapHeight
					: mapWidth) * txtBoxScale, txtBackground);

			canvas.drawText("Processing..." + String.valueOf(progress) + " "
					+ "%", clipBounds.left + txtOffset, clipBounds.top
					+ txtOffset * 1.55f, txtPaint);

		}

		// Standart display
		else {

			canvas.drawBitmap(fractalMap, bitmapRect, canvasRect, null);

			switch (txtDisplay) {

			case SHOW_TIME: {

				canvas.drawRect(0, 0, mapWidth, ((verticalView) ? mapHeight
						: mapWidth) * txtBoxScale, txtBackground);

				canvas.drawText(
						"Image generation time: "
								+ String.valueOf((float) time / 1000)
								+ " Seconds", clipBounds.left + txtOffset,
						clipBounds.top + txtOffset * 1.55f, txtPaint);

				break;

			}

			case SHOW_COORDINATE: {

				canvas.drawRect(0, 0, mapWidth, ((verticalView) ? mapHeight
						: mapWidth) * txtBoxScale * 2, txtBackground);

				canvas.drawText(
						"Re(Z) = "
								+ String.valueOf(touchX * fractal.complexScale
										+ fractal.minX), clipBounds.left
								+ txtOffset,
						clipBounds.top + txtOffset * 1.55f, txtPaint);
				canvas.drawText(
						"Im(Z) = "
								+ String.valueOf(-touchY * fractal.complexScale
										+ fractal.maxY), clipBounds.left
								+ txtOffset,
						clipBounds.top + txtOffset * 3.55f, txtPaint);
				break;

			}

			case SHOW_MOVE: {

				canvas.drawRect(0, 0, mapWidth, ((verticalView) ? mapHeight
						: mapWidth) * txtBoxScale * 2, txtBackground);

				canvas.drawText("Move X: " + String.valueOf(moveX),
						clipBounds.left + txtOffset, clipBounds.top + txtOffset
								* 1.55f, txtPaint);
				canvas.drawText("Move Y: " + String.valueOf(moveY),
						clipBounds.left + txtOffset, clipBounds.top + txtOffset
								* 3.55f, txtPaint);
				break;

			}

			case SHOW_ZOOM: {

				canvas.drawRect(0, 0, mapWidth, ((verticalView) ? mapHeight
						: mapWidth) * txtBoxScale, txtBackground);

				canvas.drawText(
						"Zoom: "
								+ String.valueOf(1 + ((zoom - zoom0 <= -1) ? -0.99f
										: zoom - zoom0)), clipBounds.left
								+ txtOffset,
						clipBounds.top + txtOffset * 1.55f, txtPaint);

				break;

			}
			case SHOW_DEFAULT: {

				break;

			}

			}
		}

		if (showInfo) {

			canvas.drawRect(0, mapHeight
					- ((verticalView) ? mapHeight : mapWidth) * txtBoxScale
					* 2.33f, mapWidth, mapHeight, txtBackground);

			canvas.drawText("Radius: " + String.valueOf(fractal.radius),
					clipBounds.left + 10,
					clipBounds.bottom - txtOffset * 1.55f, txtPaint);
			canvas.drawText(
					"Iterations: " + String.valueOf(fractal.iterations),
					clipBounds.left + 10,
					clipBounds.bottom - txtOffset * 3.55f, txtPaint);

		}

		if (resetReq) {
			refresh();
			resetReq = false;
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction() & MotionEvent.ACTION_MASK;

		boolean safeTouch = true;

		switch (action) {

		// Get touch coordinates
		case MotionEvent.ACTION_DOWN: {

			touchX = event.getX();
			touchY = event.getY();
			if (fractal.type == Fractal.MANDELBROT) {

				fractal.constX = touchX * fractal.complexScale + fractal.minX;
				fractal.constY = -touchY * fractal.complexScale + fractal.maxY;

			}
			txtDisplay = SHOW_COORDINATE;
			performClick();
			invalidate();
			break;
		}

		// Refresh image if changes were made
		case MotionEvent.ACTION_UP: {

			drawX0 = (short) canvasRect.left;
			drawY0 = (short) canvasRect.top;
			txtDisplay = SHOW_DEFAULT;
			refresh();
			invalidate();
			break;
		}

		case MotionEvent.ACTION_MOVE: {

			// Make sure user actually want to move if pass sensitivity check
			if (render.isAlive()
					&& Math.hypot(event.getX() - touchX, event.getY() - touchY) > MOVE_SENSITIVITY)
				stopRendering();

			// Zoom if user tries to pinch zoom
			if (event.getPointerCount() > 1 && !moveView) {

				if (safeTouch && firstIndex != lastIndex && firstIndex >= 0
						&& firstIndex < event.getPointerCount()
						&& lastIndex > 0 && lastIndex < event.getPointerCount()) {

					txtDisplay = SHOW_ZOOM;

					zoom = (float) (ZOOM_CONSTANT * Math.hypot(
							(event.getX(lastIndex) - event.getX(firstIndex)),
							((event.getY(lastIndex) - event.getY(firstIndex)))));

					zoom((event.getX(lastIndex) + event.getX(firstIndex)) / 2,
							(event.getY(lastIndex) + event.getY(firstIndex)) / 2,
							1 + ((zoom - zoom0 <= -1) ? -0.99f : zoom - zoom0));

				}
			}

			else if (!zoomView) {

				// Move the image
				if (Math.hypot(event.getX() - touchX, event.getY() - touchY) > MOVE_SENSITIVITY) {

					txtDisplay = SHOW_MOVE;

					move(event.getX() - touchX, event.getY() - touchY);
				}

				else
					txtDisplay = SHOW_COORDINATE;

			}

			invalidate();
			break;
		}

		case MotionEvent.ACTION_POINTER_DOWN: {

			// Updating zoom variables and getting finger indexes for pinch zoom
			safeTouch = (event.getPointerCount() <= 2);

			lastIndex = (byte) event.getActionIndex();
			firstIndex = (byte) ((lastIndex - 1 > 0) ? lastIndex - 1 : 0);

			if (safeTouch && firstIndex != lastIndex && firstIndex >= 0
					&& firstIndex < event.getPointerCount() && lastIndex > 0
					&& lastIndex < event.getPointerCount())
				zoom0 = (float) (ZOOM_CONSTANT * Math.hypot(
						(event.getX(lastIndex) - event.getX(firstIndex)),
						((event.getY(lastIndex) - event.getY(firstIndex)))));

			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {

			// Finger lifted. Prevent crash by disabling safeTouch
			safeTouch = false;
			break;
		}

		}

		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		refresh();
		return true;
	}

	private void zoom(float px, float py, float scale) {

		short itr;

		zoomView = true;

		if (!render.isAlive()) {

			// Zoom in
			if (scale >= 1) {

				// Focus on a smaller rectangle proportional to screen size on
				// the bitmap
				bitmapRect.set((int) (px - mapWidth / (scale * 2)),
						(int) (py - mapHeight / (scale * 2)),
						(int) (px + mapWidth / (scale * 2)),
						(int) (py + mapHeight / (scale * 2)));

				// Calculate new radius
				fractal.radius = (verticalView) ? fractal.complexScale
						* (bitmapRect.right - bitmapRect.left) / 2
						: fractal.complexScale
								* (bitmapRect.bottom - bitmapRect.top) / 2;

				// Calculate new focus
				fractal.focusX = fractal.minX + px * fractal.complexScale;
				fractal.focusY = fractal.maxY - py * fractal.complexScale;

				// Zoom out
			} else {

				// Make canvas display area smaller creating a centered zoom out
				// effect
				canvasRect.set((int) (mapWidth / 2 - mapWidth * scale / 2),
						(int) (mapHeight / 2 - mapHeight * scale / 2),
						(int) (mapWidth / 2 + mapWidth * scale / 2),
						(int) (mapHeight / 2 + mapHeight * scale / 2));

				// Adjust radius to the new scale
				fractal.radius = radius0 / scale;
			}

			// Calculate new maximum iteration value according to the zoom scale
			itr = (fractal.radius < 1) ? (short) (fractal.iterationsConst
					* Math.log(1 / fractal.radius) * Math
					.log(1 / fractal.radius)) : Fractal.MIN_ITERATIONS;
			fractal.iterations = (itr > Fractal.MIN_ITERATIONS && itr < Fractal.MAX_ITERATIONS) ? itr
					: (itr > Fractal.MAX_ITERATIONS) ? Fractal.MAX_ITERATIONS
							: fractal.iterations;

			needRefresh = true;
		}

	}

	private void move(float dx, float dy) {

		moveView = true;

		// Calculate how much we move in complex plane scale for display
		moveX = (float) (-dx * fractal.complexScale);
		moveY = (float) (dy * fractal.complexScale);

		if (!render.isAlive()) {

			// Move the canvas display area with the bitmap according to user
			// request
			canvasRect.set((int) (drawX0 + dx), (int) (drawY0 + dy),
					(int) (canvasRect.left + mapWidth),
					(int) (canvasRect.top + mapHeight));

			fractal.focusX = focusX0 - dx * fractal.complexScale;
			fractal.focusY = focusY0 + dy * fractal.complexScale;

			needRefresh = true;
		}

	}

	private void generate() {

		// Start the rendering process
		if (!render.isAlive()) {
			
			// Lock screen orientation
			((Fractaler) getContext()).lockOrientation();
			
			render = new Thread(generator);

			// Temp bitmap to stabilize the image as the user left it before
			// rendering
			tempMap = fractalMap.copy(Bitmap.Config.ARGB_8888, true);

			fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
					Bitmap.Config.ARGB_8888);

			// If only part of the image needs redrawing after movement generate
			// a pre drawn translated map
			if (moveView)
				translateMap(tempMap, fractalMap);

			if (zoomView) {

				drawX0 = 0;
				drawY0 = 0;
			}

			fractal.setComplexAxis(mapWidth, mapHeight, verticalView);
			render.start();
		}
	}

	private void postGenerate() {
		
		// Unlock screen orientation
		if (!changing)
			((Fractaler) getContext()).unlockOrientation();
		
		// Adjust variables after rendering process
		canvasRect.set(0, 0, mapWidth, mapHeight);
		bitmapRect.set(0, 0, mapWidth, mapHeight);
		focusX0 = fractal.focusX;
		focusY0 = fractal.focusY;
		radius0 = fractal.radius;
		drawX0 = 0;
		drawY0 = 0;
		showInfo = false;
		zoomView = false;
		moveView = false;
		changing = false;
		

		if (render.isInterrupted()) {

			txtDisplay = SHOW_DEFAULT;
			render = new Thread(generator);
			return;
		}

		txtDisplay = SHOW_TIME;
		postInvalidate();

	}

	private void refresh() {

		if (needRefresh) {

			if (!render.isAlive())
				needRefresh = false;

			generate();

		}
	}

	public void reset() {

		stopRendering();

		fractal.iterations = Fractal.MIN_ITERATIONS;

		if (fractal.type == Fractal.MANDELBROT) {

			fractal.radius = 1.5;
			fractal.focusX = -0.7;
			fractal.focusY = 0;
			fractal.constX = 0;
			fractal.constY = 0;
		}

		else if (fractal.type == Fractal.JULIA) {

			fractal.radius = 1.5;
			fractal.focusX = 0;
			fractal.focusY = 0;

		}

		fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
				Bitmap.Config.ARGB_8888);

		needRefresh = true;
		resetReq = true;
		fixPass = true;

		invalidate();

	}
	
	public void setFractal(Fractal fract) {
		
		changing = true;
		
		stopRendering();
		
		// Adjust incoming fractal for view
		fract.setComplexAxis(mapWidth, mapHeight, verticalView);
		fract.colorMap = fractal.colorMap;
		fract.redDelay = fractal.redDelay;
		fract.greenDelay = fractal.greenDelay;
		fract.blueDelay = fractal.blueDelay;
		
		// Set the new fractal
		fractal = fract;
		
		fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
				Bitmap.Config.ARGB_8888);

		needRefresh = true;
		resetReq = true;
		fixPass = true;

		invalidate();
		
	}

	public void setColors(int[] colors) {

		changing = true;
		
		stopRendering();
		
		// Lock screen orientation
		((Fractaler) getContext()).lockOrientation();
		
		fractal.colorMap = colors;

		fractal.colorMethod = Fractal.COLORMAP;

		fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
				Bitmap.Config.ARGB_8888);

		needRefresh = true;
		resetReq = true;
		fixPass = true;

		invalidate();

	}

	public void setColorDelay(float[] delay) {

		changing = true;
		
		stopRendering();
		
		// Lock screen orientation
		((Fractaler) getContext()).lockOrientation();

		fractal.redDelay = delay[0];
		fractal.greenDelay = delay[1];
		fractal.blueDelay = delay[2];

		fractal.colorMethod = Fractal.COLOR_DELAY;

		fractalMap = Bitmap.createBitmap(mapWidth, mapHeight,
				Bitmap.Config.ARGB_8888);

		needRefresh = true;
		resetReq = true;
		fixPass = true;

		invalidate();

	}

	public void setTextColor(int color) {
		txtPaint.setColor(color);
		txtDisplay = SHOW_DEFAULT;
		invalidate();
	}

	public int[] getColors() {

		return fractal.colorMap;
	}


	public Fractal getFractal() {

		fractal.setComplexAxis(mapWidth, mapHeight, verticalView);
		return fractal;
	}
	
	public void stopRendering() {
		if (render.isAlive())
			render.interrupt();
		invalidate();

	}

	public void switchType() {

		if (fractal.type == Fractal.JULIA)
			fractal.type = Fractal.MANDELBROT;
		else
			fractal.type = Fractal.JULIA;

		reset();
	}
	


}
