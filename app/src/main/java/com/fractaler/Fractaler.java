package com.fractaler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;


public class Fractaler extends Activity {

	protected static final byte COLORS_MENU = 'c';
	protected static final byte CONFIG_MENU = 'o';
	protected static final byte INFO_MENU = 'i';

	private FractalView fview;
	private Button menuButton;
	private ImageButton reset, colors, magic, config, save, info, exit;
	private ScrollView customMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		init();
		setListeners();

	}

	private void setListeners() {

		menuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (customMenu.getVisibility() == ScrollView.VISIBLE) {

					customMenu.setVisibility(ScrollView.GONE);

				} else {

					customMenu.setVisibility(ScrollView.VISIBLE);
				}
			}
		});

		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Toast.makeText(Fractaler.this, "Fractal Reset",
						Toast.LENGTH_SHORT).show();
				fview.reset();
				menuButton.performClick();

			}
		});

		colors.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Lock Orientation to prevent related crash
				lockOrientation();

				// Start colors menu
				Intent colorsMenu = new Intent(Fractaler.this, ColorsMenu.class);
				colorsMenu.putExtra("importColors", fview.getColors());
				startActivityForResult(colorsMenu, COLORS_MENU);
				menuButton.performClick();

			}
		});

		magic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Toast.makeText(Fractaler.this, "Fractal Switch",
						Toast.LENGTH_SHORT).show();
				fview.switchType();
				menuButton.performClick();

			}
		});

		info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Lock Orientation to prevent related crash
				lockOrientation();

				// Start info menu
				Intent infoMenu = new Intent(Fractaler.this, InfoMenu.class);
				startActivityForResult(infoMenu, INFO_MENU);
				menuButton.performClick();

			}
		});

		config.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Lock Orientation to prevent related crash
				lockOrientation();

				// Start config menu
				Intent configMenu = new Intent(Fractaler.this, ConfigMenu.class);
				configMenu.putExtra("fractal", fview.getFractal());
				startActivityForResult(configMenu, CONFIG_MENU);
				menuButton.performClick();

			}
		});

		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				menuButton.performClick();
				fview.stopRendering();
				finish();

			}
		});

	}

	private void init() {

		// Initialize fractal view
		fview = new FractalView((Context) this);
		fview = (FractalView) findViewById(R.id.fview);
		fview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		// Initialize menu
		customMenu = (ScrollView) findViewById(R.id.customMenu);
		customMenu.setVisibility(ScrollView.GONE);

		menuButton = (Button) findViewById(R.id.menuButton);
		reset = (ImageButton) findViewById(R.id.mReset);
		colors = (ImageButton) findViewById(R.id.mColors);
		magic = (ImageButton) findViewById(R.id.mMagic);
		config = (ImageButton) findViewById(R.id.mConfig);
		save = (ImageButton) findViewById(R.id.mSave);
		info = (ImageButton) findViewById(R.id.mInfo);
		exit = (ImageButton) findViewById(R.id.mExit);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case COLORS_MENU: {

			switch (resultCode) {

			case ColorsMenu.RESULT_FRACTAL_COLORS: {

				fview.setColors(data.getExtras().getIntArray("exportColors"));
				break;
			}

			case ColorsMenu.RESULT_DELAY_COLORS: {

				fview.setColorDelay(data.getExtras().getFloatArray(
						"exportDelay"));
				break;
			}

			case ColorsMenu.RESULT_TEXT_COLOR: {

				fview.setTextColor(data.getExtras().getInt("textColor"));
				break;
			}

			}

			break;
		}

		case CONFIG_MENU: {
			
			if(resultCode == RESULT_OK)
				fview.setFractal((Fractal) data.getExtras().getParcelable("fractal"));
	
			break;
		}

		case INFO_MENU: {
			
			break;
		}

		}
	}

	public void lockOrientation() {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public void unlockOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

}