package com.example.openpass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.example.openpass.engine.ActivationCode;
import com.example.openpass.engine.Base32;

public class AddTokenActivity extends AppCompatActivity {
	
	private boolean isValidCode = false;
	private Button saveButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		this.saveButton = (Button)findViewById(R.id.saveButton);
		
		
		EditText activationCode = (EditText)findViewById(R.id.activationCode);
		activationCode.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				ActivationCode code = new ActivationCode(s.toString());
				saveButton.setEnabled(code.isValid());
			}
		});
		activationCode.setText(Base32.generate20());
    }
	
    public void onCancel(View v) {
		setResult(RESULT_CANCELED);
        finish();
    }

	public static void errorDialog(Context context) {
		new AlertDialog.Builder(context)
				.setTitle("Error")
				.setMessage("Tough luck, pal!")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.create().show();
	}
	
    public void onSave(View v) {
		final EditText tokenName = (EditText)findViewById(R.id.tokenName);
		final EditText activationCode = (EditText)findViewById(R.id.activationCode);
		final EditText pinCode = (EditText)findViewById(R.id.pinCode);
		
		String code_encrypted = TokenEncryption.encryptCode(activationCode.getText().toString(),
				pinCode.getText().toString());
		if (code_encrypted == null) {
			errorDialog(this);
			return;
		}

		Intent result = new Intent();
		Bundle extras = new Bundle();
		extras.putString(Schema.COL_NAME, tokenName.getText().toString());
		extras.putString(Schema.COL_ACTIVATION_CODE, code_encrypted);
		result.putExtras(extras);
		setResult(RESULT_OK, result);
		finish();
    }

	@Override
	public void onPause() {
		setResult(RESULT_CANCELED);
		finish();
		super.onPause();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
