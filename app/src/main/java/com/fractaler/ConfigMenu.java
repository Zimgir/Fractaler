package com.fractaler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;



public class ConfigMenu extends Activity {
	
	protected class EnableFocusListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			v.setFocusable(true);
			v.setFocusableInTouchMode(true);
			v.requestFocusFromTouch();
			
		}	
	}

	private Spinner type, value, color;
	private SeekBar itrBar;
	private EditText iterations, radius, focusX, focusY, constX, constY, bail;
	private TextView itrPercent;
	private Button config, exit;
	private Fractal fractal;
	private EnableFocusListener enableFocus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);

		init();
		setSpinners();
		setListeners();
		setTextFields();

	}

	private void init() {

		// Initialize focus listener
		enableFocus = new EnableFocusListener();

		// Get the fractal data
		fractal = (Fractal) getIntent().getExtras().getParcelable("fractal");

		// Sync views with layout
		type = (Spinner) findViewById(R.id.mType);
		value = (Spinner) findViewById(R.id.mValue);
		color = (Spinner) findViewById(R.id.mColor);
		itrBar = (SeekBar) findViewById(R.id.itrBar);
		iterations = (EditText) findViewById(R.id.iterations);
		radius = (EditText) findViewById(R.id.radius);
		focusX = (EditText) findViewById(R.id.focusX);
		focusY = (EditText) findViewById(R.id.focusY);
		constX = (EditText) findViewById(R.id.constX);
		constY = (EditText) findViewById(R.id.constY);
		bail = (EditText) findViewById(R.id.bail);
		itrPercent = (TextView) findViewById(R.id.itrConstPrcnt);
		config = (Button) findViewById(R.id.configOk);
		exit = (Button) findViewById(R.id.exitConfig);
		

		// Fill in the current fractal data
		type.setSelection(fractal.type);
		value.setSelection(fractal.iterationValue);
		color.setSelection(fractal.colorMethod);
		
		itrBar.setProgress((int) (itrBar.getMax() * fractal.iterationsConst / Fractal.MAX_ITERATIONS_CONST));
		itrPercent
				.setText("   "
						+ String.valueOf((int) (100 * fractal.iterationsConst / Fractal.MAX_ITERATIONS_CONST))
						+ " " + "%");
		itrPercent
				.setTextColor((100 * itrBar.getProgress() / itrBar.getMax() > 80) ? Color.RED
						: (100 * itrBar.getProgress() / itrBar.getMax() > 50) ? Color.YELLOW
								: Color.GREEN);
		
		bail.setText(String.valueOf(fractal.bail));
		iterations.setText(String.valueOf(fractal.iterations));
		radius.setText(String.valueOf(fractal.radius));
		focusX.setText(String.valueOf(fractal.focusX));
		focusY.setText(String.valueOf(fractal.focusY));
		constX.setText(String.valueOf(fractal.constX));
		constY.setText(String.valueOf(fractal.constY));

	}

	private void setSpinners() {

		type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				fractal.type = (byte) position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

				// Leave as is...

			}

		});

		value.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				fractal.iterationValue = (byte) position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

				// Leave as is...

			}

		});

		color.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				fractal.colorMethod = (byte) position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

				// Leave as is...

			}

		});

	}
	
	private void setTextFields() {
		
		// Set enbale editing listeners
		
		iterations.setOnClickListener(enableFocus);
		bail.setOnClickListener(enableFocus);
		radius.setOnClickListener(enableFocus);
		focusX.setOnClickListener(enableFocus);
		focusY.setOnClickListener(enableFocus);
		constX.setOnClickListener(enableFocus);
		constY.setOnClickListener(enableFocus);

		// Set info update listeners
		
		iterations.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				short num;

				if (!hasFocus) {

					try {

						num = Short.parseShort(((EditText) v).getText()
								.toString());
						if(num < Fractal.MIN_ITERATIONS || num > Fractal.MAX_ITERATIONS) {
							
							num = fractal.iterations;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.iterations = num;
							
					} catch (NumberFormatException e) {

						num = fractal.iterations;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
		bail.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < Fractal.MIN_BAIL || num > Fractal.MAX_BAIL) {
							
							num = fractal.bail;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.bail = num;
							
					} catch (NumberFormatException e) {

						num = fractal.bail;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
				
		radius.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < Fractal.MIN_RADIUS || num > Fractal.MAX_RADIUS) {
							
							num = fractal.radius;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.radius = num;
							
					} catch (NumberFormatException e) {

						num = fractal.radius;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
		focusX.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < -Fractal.MAX_RADIUS || num > Fractal.MAX_RADIUS) {
							
							num = fractal.focusX;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.focusX = num;
							
					} catch (NumberFormatException e) {

						num = fractal.focusX;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
		focusY.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < -Fractal.MAX_RADIUS || num > Fractal.MAX_RADIUS) {
							
							num = fractal.focusY;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.focusY = num;
							
					} catch (NumberFormatException e) {

						num = fractal.focusY;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
		constX.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < -Fractal.MAX_RADIUS || num > Fractal.MAX_RADIUS) {
							
							num = fractal.constX;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.constX = num;
							
					} catch (NumberFormatException e) {

						num = fractal.constX;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
		constY.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				Double num;

				if (!hasFocus) {

					try {

						num = Double.parseDouble(((EditText) v).getText()
								.toString());
						if(num < -Fractal.MAX_RADIUS || num > Fractal.MAX_RADIUS) {
							
							num = fractal.constY;
							((EditText) v).setText(String.valueOf(num));
						}
						else
							fractal.constY = num;
							
					} catch (NumberFormatException e) {

						num = fractal.constY;
						((EditText) v).setText(String.valueOf(num));
					}

				}

			}
		});
		
	}
	
	private void setListeners() {

		itrBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				fractal.iterationsConst = Fractal.MAX_ITERATIONS_CONST
						* progress / itrBar.getMax();
				itrPercent.setText("   "
						+ String.valueOf((int) (100 * progress / itrBar
								.getMax())) + " " + "%");
				itrPercent.setTextColor((100 * progress / itrBar.getMax() > 80) ? Color.RED
						: (100 * progress / itrBar.getMax() > 50) ? Color.YELLOW
								: Color.GREEN);

			}
		});
		
		config.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				iterations.setFocusable(false);
				bail.setFocusable(false);
				radius.setFocusable(false);
				focusX.setFocusable(false);
				focusY.setFocusable(false);
				constX.setFocusable(false);
				constY.setFocusable(false);
				
					
				Intent configResut = new Intent();
				configResut.putExtra("fractal", fractal);
				setResult(RESULT_OK, configResut);
				finish();
			}
		});
		
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
								
				Intent configResut = new Intent();
				setResult(RESULT_CANCELED, configResut);
				finish();
			}
		});

	

	}

}
