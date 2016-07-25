package com.example.openpass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.openpass.engine.ActivationCode;

public class PinEnterActivity extends AppCompatActivity {
	public static final int RESULT_SUSPENDED = 5;
	
	private String initialCode = null;
	private long sequenceIndex = -1;
	private long tokenIndex = -1;

	public static String CHANGE_PIN_EXTRA = "changepin";
	private boolean is_changing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin_enter);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

		is_changing = getIntent().hasExtra(CHANGE_PIN_EXTRA);
		if (! is_changing) {
			initialCode = getIntent().getStringExtra(Schema.COL_ACTIVATION_CODE);
			sequenceIndex = getIntent().getLongExtra(Schema.COL_NEXT_INDEX, -1);
			tokenIndex = getIntent().getLongExtra(Schema.COL_TOKEN_INDEX, -1);

			if (sequenceIndex < 0)
				// Let's crash
				initialCode = null;
		}
		Log.d("OpenPass", "PinEnterActivity encrypted: " + initialCode);
		
		Button enterButton = (Button)findViewById(R.id.pinEnterButton);
		if (is_changing)
			enterButton.setText("Apply");

	}
	
	public static void errorDialog(Context context) {
		new AlertDialog.Builder(context)
				.setTitle("Error")
				.setMessage("Fatal error, sorry")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.create().show();
	}
	
	public void onEnterPin(View v) {
		EditText input = (EditText) findViewById(R.id.pinEntry);
		String pin = input.getText().toString();
		
		if (! is_changing) {
			String decryptedCode = TokenEncryption.decryptCode(initialCode, pin);

			if (decryptedCode == null) {
				errorDialog(this);
				return;
			}

			Log.d("OpenPass", "PinEnterActivity decryped: " + decryptedCode);
			ActivationCode code = new ActivationCode(decryptedCode);
			if (!code.isValid()) {
				new AlertDialog.Builder(this)
						.setTitle("Error")
						.setMessage("Nope!")
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.create().show();
				return;
			}

			Intent result = new Intent();
			Bundle extras = new Bundle();
			extras.putString(Schema.COL_ACTIVATION_CODE, decryptedCode);
			extras.putLong(Schema.COL_NEXT_INDEX, sequenceIndex);
			extras.putLong(Schema.COL_TOKEN_INDEX, tokenIndex);
			result.putExtras(extras);
			setResult(RESULT_OK, result);
			finish();
		} else {
			Intent result = new Intent();
			result.putExtra(CHANGE_PIN_EXTRA, pin);
			setResult(RESULT_OK, result);
			finish();
		}
	}
	
	@Override
	public void onPause() {
		if (is_changing) {
			setResult(RESULT_SUSPENDED);
			finish();
		}
		super.onPause();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
