package com.example.openpass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.openpass.engine.MobilePass;

import java.util.Vector;

public class GeneratorActivity extends AppCompatActivity {

	private String activationCode = null;
	private long sequenceIndex = -1;
	private long tokenIndex = -1;
	private TextView passwordOutput, indexOutput;
	
	private static final int ACTIVITY_SET_INDEX = 0;
	private static final int ACTIVITY_SET_PIN = 1;
	
	private boolean quitOnPause = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generator);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		
		quitOnPause = true;

		activationCode = getIntent().getStringExtra(Schema.COL_ACTIVATION_CODE);
		sequenceIndex = getIntent().getLongExtra(Schema.COL_NEXT_INDEX, -1);
		tokenIndex = getIntent().getLongExtra(Schema.COL_TOKEN_INDEX, -1);

		if (sequenceIndex < 0)
			// let's crash
			activationCode = null;
		
		passwordOutput = (TextView)findViewById(R.id.passwordOutput);
		indexOutput = (TextView)findViewById(R.id.sequenceIndexOutput);
		
		generatePassword(sequenceIndex);
	}
	
	@Override
	protected void onPause() {
		if (quitOnPause) {
			Log.d("OpenPass", "GeneratorActivity onPause");
			finish();
		}
		super.onPause();
	}
	
	private void generatePassword(long index) {
		String pass = MobilePass.generateToken(activationCode, index);
		if (pass == null)
			passwordOutput.setText("Leave me alone!");
		else
			passwordOutput.setText(pass);
		indexOutput.setText(String.valueOf(index));
		
		sequenceIndex = index+1;
		Vector<TokenInfo> tokens = Settings.loadTokens(this);
		tokens.elementAt((int)tokenIndex).nextSequenceIndex = sequenceIndex;
		Settings.saveTokens(this, tokens);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.generator_menu, menu);
		return true;
	}

	public void onNextPassword(View v) {
		generatePassword(sequenceIndex);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.generator_contextmenu_setindex:
				onSetIndex(null);
				break;
			case R.id.generator_contextmenu_setpin:
				onSetPin();
				break;
			case R.id.generator_contextmenu_showcode:
				onShowCode();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onSetIndex(View v) {
		Bundle extras = new Bundle();
		extras.putLong(Schema.COL_NEXT_INDEX, this.sequenceIndex-1);

		Intent i = new Intent(this, SetIndexActivity.class);
		i.putExtras(extras);
		quitOnPause = false;
		startActivityForResult(i, ACTIVITY_SET_INDEX);
	}
	
	public void onSetPin() {
		Bundle extras = new Bundle();
		extras.putInt(PinEnterActivity.CHANGE_PIN_EXTRA, 0);

		Intent i = new Intent(this, PinEnterActivity.class);
		i.putExtras(extras);
		quitOnPause = false;
		startActivityForResult(i, ACTIVITY_SET_PIN);
	}

	public void onShowCode() {
		new AlertDialog.Builder(this)
				.setTitle("Here goes")
				.setMessage(activationCode)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent sender) {
		super.onActivityResult(requestCode, resultCode, sender);
		Log.d("OpenPass", "Generator onActivityResult");
		quitOnPause = true;

		if (requestCode == ACTIVITY_SET_INDEX) {
			if (resultCode == RESULT_OK) {
				Bundle param = sender.getExtras();
				Log.d("OpenPass", "Generator setting index to " + param.getLong(Schema.COL_NEXT_INDEX));
				generatePassword(param.getLong(Schema.COL_NEXT_INDEX));
			} else if (resultCode == SetIndexActivity.RESULT_SUSPENDED)
				finish();
		} else if (requestCode == ACTIVITY_SET_PIN) {
			if (resultCode == RESULT_OK) {
				Bundle param = sender.getExtras();
				String pin = param.getString(PinEnterActivity.CHANGE_PIN_EXTRA);

				String code_encrypted = TokenEncryption.encryptCode(activationCode, pin);
				if (code_encrypted == null) {
					AddTokenActivity.errorDialog(this);
					return;
				}

				Vector<TokenInfo> tokens = Settings.loadTokens(this);
				tokens.elementAt((int) tokenIndex).encryptedCode = code_encrypted;
				Settings.saveTokens(this, tokens);
			} else if (resultCode == PinEnterActivity.RESULT_SUSPENDED)
				finish();
		}
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
