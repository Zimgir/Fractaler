package com.fractaler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class ColorsMenu extends Activity {

	protected static final byte RESULT_FRACTAL_COLORS = 'f';
	protected static final byte RESULT_DELAY_COLORS = 'd';
	protected static final byte RESULT_TEXT_COLOR = 't';
	
	private int[] importColors;
	private Colorbar colorbar;
	private SeekBar redBar, greenBar, blueBar, brightBar;
	private Button getColors, getDelay, resetColors, colorText, exit;
	private CheckBox smoothing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.colors);

		init();
		setListeners();

	}

	private void setListeners() {

		redBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue,
					boolean fromUser) {
				colorbar.changeRed((float)progresValue/redBar.getMax());
				colorText.setTextColor(Color.argb(255,colorbar.redChange,colorbar.greenChange,colorbar.blueChange));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				colorbar.updateColors();

			}

		});

		greenBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue,
					boolean fromUser) {
				colorbar.changeGreen((float)progresValue/greenBar.getMax());
				colorText.setTextColor(Color.argb(255,colorbar.redChange,colorbar.greenChange,colorbar.blueChange));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				colorbar.updateColors();

			}

		});

		blueBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue,
					boolean fromUser) {
				colorbar.changeBlue((float)progresValue/blueBar.getMax());
				colorText.setTextColor(Color.argb(255,colorbar.redChange,colorbar.greenChange,colorbar.blueChange));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				colorbar.updateColors();

			}

		});

		brightBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue,
					boolean fromUser) {
				colorbar.changeBright((float)progresValue/brightBar.getMax());
				colorText.setTextColor(Color.argb(255,colorbar.redChange,colorbar.greenChange,colorbar.blueChange));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				colorbar.updateColors();

			}

		});

		smoothing.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (((CheckBox) v).isChecked())
					colorbar.setSmoothing(true);
				else
					colorbar.setSmoothing(false);
			}

		});

		resetColors.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				colorbar.importColors(importColors);
				colorbar.invalidate();

			}

		});
		
		colorText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				Intent colorsResut = new Intent();
				colorsResut.putExtra("textColor", colorbar.getTextColor());
				setResult(RESULT_TEXT_COLOR, colorsResut);
				finish();

			}

		});

		getColors.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				Intent colorsResut = new Intent();
				colorsResut.putExtra("exportColors", colorbar.exportColors());
				setResult(RESULT_FRACTAL_COLORS, colorsResut);
				finish();

			}

		});
		
		getDelay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				Intent colorsResut = new Intent();
				colorsResut.putExtra("exportDelay", colorbar.exportDelay());
				setResult(RESULT_DELAY_COLORS, colorsResut);
				finish();

			}

		});
		
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent colorsResut = new Intent();
				setResult(RESULT_CANCELED, colorsResut);
				finish();
				
				
			}
		});
			
	}
	

	private void init() {

		// Create colorbar view
		colorbar = new Colorbar((Context)this);

		// Sync with layout in xml;
		colorbar = (Colorbar) findViewById(R.id.colorbar);
		redBar = (SeekBar) findViewById(R.id.redBar);
		greenBar = (SeekBar) findViewById(R.id.greenBar);
		blueBar = (SeekBar) findViewById(R.id.blueBar);
		brightBar = (SeekBar) findViewById(R.id.brightBar);
		getColors = (Button) findViewById(R.id.getColors);
		getDelay = (Button) findViewById(R.id.getDelay);
		colorText = (Button) findViewById(R.id.colorText);
		resetColors = (Button) findViewById(R.id.resetColors);
		smoothing = (CheckBox) findViewById(R.id.smoothing);
		exit = (Button) findViewById(R.id.exitColors);

		// Init checkbox;
		smoothing.setChecked(true);

		// Import Colors from fractal and display them in the colorbar
		importColors = getIntent().getExtras().getIntArray("importColors");
		colorbar.importColors(importColors);

	}
	
}
